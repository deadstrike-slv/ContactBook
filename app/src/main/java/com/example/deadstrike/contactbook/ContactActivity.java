package com.example.deadstrike.contactbook;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.deadstrike.contactbook.adapters.ContactInfoAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener{

    //handling errors
    private static final int STATE_EMPTY_FIELDS = 10;
    private static final int STATE_ERROR_EMAIL = 20;
    private static final int STATE_ERROR_PHONE_NUMBER = 30;
    //region data holders
    TextInputLayout firstNameInputLayout;
    TextInputLayout lastNameInputLayout;
    RecyclerView phoneNumbersInputList;
    RecyclerView emailsInputList;
    ContactInfoAdapter phoneNumbersAdapter;
    ContactInfoAdapter emailsAdapter;
    //endregion
    DBHelper mDBHelper;
    SQLiteDatabase db;
    private int ERROR_STATE=0;
    // true if adding new row (as default), false if editing existing row
    private boolean mMode = true;
    // dataset for temp holding user data
    private String firstName = "";
    private String lastName = "";
    private List<String> phoneNumbers = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private String mCurrentUser;
    private long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_contact);
        setSupportActionBar(toolbar);

        firstNameInputLayout = (TextInputLayout) findViewById(R.id.input_first_name);
        lastNameInputLayout = (TextInputLayout) findViewById(R.id.input_last_name);
        phoneNumbersInputList = (RecyclerView) findViewById(R.id.numbers_list);
        emailsInputList = (RecyclerView) findViewById(R.id.emails_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSave);
        findViewById(R.id.button_add_phone_number).setOnClickListener(this);
        findViewById(R.id.button_add_email).setOnClickListener(this);
        fab.setOnClickListener(this);

        mDBHelper = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            mCurrentUser = extras.getString(ContactsListActivity.BUNDLE_KEY_USER);
            mRowId = extras.getInt(ContactsListActivity.BUNDLE_KEY_ROW_ID);
            if (mRowId>-1) {
                mMode = false;
                fillViewsWithData();
                this.setTitle(R.string.title_activity_contact_edit);
            }
        } else {
            mCurrentUser = "default";
        }

        phoneNumbersAdapter = new ContactInfoAdapter(phoneNumbers, ContactInfoAdapter.TYPE_PHONE);
        phoneNumbersInputList.setLayoutManager(new LinearLayoutManager(this));
        phoneNumbersInputList.setItemAnimator(new DefaultItemAnimator());
        phoneNumbersInputList.setAdapter(phoneNumbersAdapter);

        emailsAdapter = new ContactInfoAdapter(emails, ContactInfoAdapter.TYPE_EMAIL);
        emailsInputList.setLayoutManager(new LinearLayoutManager(this));
        emailsInputList.setItemAnimator(new DefaultItemAnimator());
        emailsInputList.setAdapter(emailsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            if (mRowId > -1) {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(R.string.delete_dialog_message)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete current row in db
                                removeAt(mRowId);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            } else onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.cancel_dialog_title)
                .setMessage(R.string.cancel_dialog_message)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.fabSave:
                clearEmptyField();
                firstName = firstNameInputLayout.getEditText().getText().toString().trim();
                lastName = lastNameInputLayout.getEditText().getText().toString().trim();
                if (!hasErrors()) {
                    //updating row
                    db = mDBHelper.getWritableDatabase();
                    if (mMode) {
                        insertCurrentDataIntoDb();
                    } else {
                        updateCurrentDataIntoDb(mRowId);
                    }
                    this.finish();

                } else {
                    switch (ERROR_STATE) {
                        case STATE_EMPTY_FIELDS:
                            Toast.makeText(v.getContext(), getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
                            break;

                        case STATE_ERROR_EMAIL:
                            Toast.makeText(v.getContext(), getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                            break;

                        case STATE_ERROR_PHONE_NUMBER:
                            Toast.makeText(v.getContext(), getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(v.getContext(), "UNKNOWN ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.button_add_phone_number:
                phoneNumbers.add("");
                phoneNumbersAdapter.notifyDataSetChanged();
                break;

            case R.id.button_add_email:
                emails.add("");
                emailsAdapter.notifyDataSetChanged();
        }
    }

    private void clearEmptyField() {
        phoneNumbers.removeAll(Arrays.asList("", null));
        emails.removeAll(Arrays.asList("", null));
    }

    private boolean hasErrors() {
        if (phoneNumbersAdapter.hasErrorInput()) {
            ERROR_STATE = STATE_ERROR_PHONE_NUMBER;
            return true;
        }

        if (emailsAdapter.hasErrorInput()) {
            ERROR_STATE = STATE_ERROR_EMAIL;
            return true;
        }

        if (((firstName + lastName).equals("")) && phoneNumbers.isEmpty() && emails.isEmpty()) {
            //all fields are empty
            ERROR_STATE = STATE_EMPTY_FIELDS;
            return true;
        }

        return false;
    }

    private void readDataFromDB() {
        db = mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DBHeaders.Contacts.TABLE_NAME +
                            " WHERE " + DBHeaders.Contacts._ID + " = ?",
                    new String[] {mRowId + ""});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                //read first name and last name
                firstName = cursor.getString(
                        cursor.getColumnIndex(DBHeaders.Contacts.COLUMN_NAME_FIRST_NAME));
                lastName = cursor.getString(
                        cursor.getColumnIndex(DBHeaders.Contacts.COLUMN_NAME_LAST_NAME));

                Cursor additionalCursor = null;
                //read phone numbers associated with current contact entry
                try {
                    additionalCursor = db.rawQuery("SELECT " + DBHeaders.PhoneNumbers.COLUMN_NAME_PHONE_NUMBER +
                                    " FROM " + DBHeaders.PhoneNumbers.TABLE_NAME +
                                    " WHERE " + DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID + " = ?",
                            new String[]{mRowId + ""});
                    // save to local variable
                    for (additionalCursor.moveToFirst(); !additionalCursor.isAfterLast(); additionalCursor.moveToNext()) {
                        phoneNumbers.add(additionalCursor.getString(
                                additionalCursor.getColumnIndex(DBHeaders.PhoneNumbers.COLUMN_NAME_PHONE_NUMBER)));
                    }
                } finally {
                    if (additionalCursor != null)
                        additionalCursor.close();
                }
                //read email addresses associated with current contact entry
                try {
                    additionalCursor = db.rawQuery("SELECT " + DBHeaders.Emails.COLUMN_NAME_EMAIL +
                                    " FROM " + DBHeaders.Emails.TABLE_NAME +
                                    " WHERE " + DBHeaders.Emails.COLUMN_NAME_CONTACT_ID + " = ?",
                            new String[]{mRowId + ""});
                    // save to local variable
                    for (additionalCursor.moveToFirst(); !additionalCursor.isAfterLast(); additionalCursor.moveToNext()) {
                        emails.add(additionalCursor.getString(
                                additionalCursor.getColumnIndex(DBHeaders.Emails.COLUMN_NAME_EMAIL)));
                    }
                } finally {
                    if (additionalCursor != null)
                        additionalCursor.close();
                }
            }
        }finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void insertCurrentDataIntoDb() {
        ContentValues cv = new ContentValues();
        // at first create new contact
        cv.put(DBHeaders.Contacts.COLUMN_NAME_USER_ID, mCurrentUser);
        cv.put(DBHeaders.Contacts.COLUMN_NAME_FIRST_NAME, firstName);
        cv.put(DBHeaders.Contacts.COLUMN_NAME_LAST_NAME, lastName);
        // insert new row in Contacts table
        long newId = db.insertOrThrow(DBHeaders.Contacts.TABLE_NAME, null, cv);

        if (newId > 0) {
            insertPhoneNumbers(newId);
            insertEmails(newId);
        }

    }

    private void updateCurrentDataIntoDb(long contactId) {
        ContentValues cv = new ContentValues();
        // at first create new contact
        cv.put(DBHeaders.Contacts.COLUMN_NAME_USER_ID, mCurrentUser);
        cv.put(DBHeaders.Contacts.COLUMN_NAME_FIRST_NAME, firstName);
        cv.put(DBHeaders.Contacts.COLUMN_NAME_LAST_NAME, lastName);

        db.update(DBHeaders.Contacts.TABLE_NAME, cv,
                DBHeaders.Contacts._ID + " = ?",
                new String[]{contactId + ""});

        //delete old data
        db.delete(DBHeaders.PhoneNumbers.TABLE_NAME,
                DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID + " = ?",
                new String[]{contactId + ""});
        db.delete(DBHeaders.Emails.TABLE_NAME,
                DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID + " = ?",
                new String[]{contactId + ""});

        insertPhoneNumbers(contactId);
        insertEmails(contactId);
    }

    private void insertPhoneNumbers(long contactId) {
        ContentValues cv;
        try {
            for (String number : phoneNumbers) {
                cv = new ContentValues();
                cv.put(DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID, contactId);
                cv.put(DBHeaders.PhoneNumbers.COLUMN_NAME_PHONE_NUMBER, number);
                db.insertOrThrow(DBHeaders.PhoneNumbers.TABLE_NAME, null, cv);
            }
        } catch (SQLException ex) {
            Log.e("Contact insert phones :", ex.getMessage());
        }
    }

    private void insertEmails(long contactId) {
        ContentValues cv;
        try {
            for (String email : emails) {
                cv = new ContentValues();
                cv.put(DBHeaders.Emails.COLUMN_NAME_CONTACT_ID, contactId);
                cv.put(DBHeaders.Emails.COLUMN_NAME_EMAIL, email);
                db.insertOrThrow(DBHeaders.Emails.TABLE_NAME, null, cv);
            }
        } catch (SQLException ex) {
            Log.e("Contact insert emails :", ex.getMessage());
        }
    }

    private void removeAt(long contactId) {
        db.delete(DBHeaders.Contacts.TABLE_NAME,
                DBHeaders.Contacts._ID + " = ?",
                new String[]{contactId + ""});
        db.delete(DBHeaders.PhoneNumbers.TABLE_NAME,
                DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID + " = ?",
                new String[]{contactId + ""});
        db.delete(DBHeaders.Emails.TABLE_NAME,
                DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID + " = ?",
                new String[]{contactId + ""});
    }

    private void fillViewsWithData() {
        readDataFromDB();
        firstNameInputLayout.getEditText().setText(firstName);
        lastNameInputLayout.getEditText().setText(lastName);
    }
}

package com.example.deadstrike.contactbook;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener{

    // true if adding new row (as default), false if editing existing row
    private boolean mMode = true;

    //region data holders
    TextInputLayout firstNameInputLayout;
    TextInputLayout lastNameInputLayout;
    TextInputLayout phoneNumberInputLayout;
    TextInputLayout emailInputLayout;
    //endregion

    DBHelper mDBHelper;
    SQLiteDatabase db;

    private static final int STATE_EMPTY_FIELDS = 10;
    private static final int STATE_ERROR_EMAIL = 20;

    private int ERROR_STATE=0;

    private String mCurrentUser;
    private int mRowId;

    //regexp pattern to validate email address
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_contact);
        setSupportActionBar(toolbar);

        firstNameInputLayout = (TextInputLayout) findViewById(R.id.input_first_name);
        lastNameInputLayout = (TextInputLayout) findViewById(R.id.input_last_name);
        phoneNumberInputLayout = (TextInputLayout) findViewById(R.id.input_phone_number);
        phoneNumberInputLayout.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        emailInputLayout = (TextInputLayout) findViewById(R.id.input_email);
        emailInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!validateEmail(s.toString())) {
                    emailInputLayout.setErrorEnabled(true);
                    emailInputLayout.setError(getString(R.string.invalid_email));
                } else {
                    emailInputLayout.setErrorEnabled(false);
                    emailInputLayout.setError(null);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSave);
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
                                db.delete(DBHeaders.ContactEntry.TABLE_NAME,
                                        DBHeaders.ContactEntry._ID + " = ?",
                                        new String[]{mRowId + ""});
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

                ContentValues data = readValues();
                if (data != null) {
                    //updating row
                    db = mDBHelper.getWritableDatabase();
                    if (mMode) {
                        db.insertOrThrow(DBHeaders.ContactEntry.TABLE_NAME, null, data);
                    } else {
                        db.update(DBHeaders.ContactEntry.TABLE_NAME,
                                data,
                                DBHeaders.ContactEntry._ID + " = ?",
                                new String[]{mRowId + ""});
                    }
                    this.finish();

                } else {
                    switch (ERROR_STATE) {
                        case STATE_EMPTY_FIELDS:
                            Toast.makeText(v.getContext(), getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();

                        case STATE_ERROR_EMAIL:
                            Toast.makeText(v.getContext(), getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();

                        default:
                            Toast.makeText(v.getContext(), "UNKNOWN ERROR", Toast.LENGTH_SHORT).show();
                    }

                }
        }
    }

    public boolean validateEmail(String email) {
        if (email.equals(""))
            return true;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Nullable
    private ContentValues readValues() {
        // read from views
        String firstName = firstNameInputLayout.getEditText().getText().toString().trim();
        String lastName = lastNameInputLayout.getEditText().getText().toString().trim();
        String phoneNumber = phoneNumberInputLayout.getEditText().getText().toString().trim();
        String email = emailInputLayout.getEditText().getText().toString().trim();
        //TODO phone & email checking

        if (!validateEmail(email)) {
            ERROR_STATE = STATE_ERROR_EMAIL;
            return null;
        }

        //if at least ONE field is non empty ...
        if ((firstName + lastName + phoneNumber + email).equals("")) {
            ERROR_STATE = STATE_EMPTY_FIELDS;
            return null;
        }

        ContentValues cv = new ContentValues();
        cv.put(DBHeaders.ContactEntry.COLUMN_NAME_USER_ID, mCurrentUser);
        cv.put(DBHeaders.ContactEntry.COLUMN_NAME_FIRST_NAME, firstName);
        cv.put(DBHeaders.ContactEntry.COLUMN_NAME_LAST_NAME, lastName);
        cv.put(DBHeaders.ContactEntry.COLUMN_NAME_PHONE_NUMBER, phoneNumber);
        cv.put(DBHeaders.ContactEntry.COLUMN_NAME_EMAIL, email);

        return cv;
    }

    private void fillViewsWithData(){
        db = mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DBHeaders.ContactEntry.TABLE_NAME +
                    " WHERE " + DBHeaders.ContactEntry._ID + " = ?",
                    new String[] {mRowId + ""});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                firstNameInputLayout.getEditText().setText(
                        cursor.getString(cursor.getColumnIndex(DBHeaders.ContactEntry.COLUMN_NAME_FIRST_NAME))
                );
                lastNameInputLayout.getEditText().setText(
                        cursor.getString(cursor.getColumnIndex(DBHeaders.ContactEntry.COLUMN_NAME_LAST_NAME))
                );
                phoneNumberInputLayout.getEditText().setText(
                        cursor.getString(cursor.getColumnIndex(DBHeaders.ContactEntry.COLUMN_NAME_PHONE_NUMBER))
                );
                emailInputLayout.getEditText().setText(
                        cursor.getString(cursor.getColumnIndex(DBHeaders.ContactEntry.COLUMN_NAME_EMAIL))
                );
            }
        }finally {
            if (cursor != null)
                cursor.close();
        }
    }
}

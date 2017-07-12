package com.example.deadstrike.contactbook;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deadstrike.contactbook.adapters.ContactsCursorRecyclerAdapter;

public class ContactsListActivity extends AppCompatActivity
        implements ContactsCursorRecyclerAdapter.IViewHolderClickListener{

    private Cursor mCursor;
    DBHelper mDbHelper;
    SQLiteDatabase db;
    ContactsCursorRecyclerAdapter contactsAdapter;

    public static String BUNDLE_KEY_USER = "userId";
    public static String BUNDLE_KEY_ROW_ID = "rowId";

    private String mCurrentUser;
    private RecyclerView recyclerView;
    private TextView emptyView;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ContactActivity.class);
                intent.putExtra(BUNDLE_KEY_USER, mCurrentUser);
                intent.putExtra(BUNDLE_KEY_ROW_ID, -1);
                startActivity(intent);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            mCurrentUser = extras.getString(ContactsListActivity.BUNDLE_KEY_USER);
        } else {
            mCurrentUser = "default";
        }

        //connect to database
        mDbHelper = new DBHelper(this);
        db = mDbHelper.getReadableDatabase();

        //read all rows where user = currentUserId
        mCursor = getCursor();
        mCursor.moveToFirst();

        // initialize adapters and recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.contacts_recyclerView);
        emptyView = (TextView) findViewById(R.id.contacts_list_empty_holder);
        contactsAdapter  = new ContactsCursorRecyclerAdapter(this, mCursor);
        contactsAdapter.setOnViewHolderClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setAdapter(contactsAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        // whether cursor hasn't rows - show Empty view holder
    }

    @Override
    public  void onResume(){
        super.onResume();
        //update data
        mCursor = getCursor();
        contactsAdapter.changeCursor(mCursor);
        updateUI();
    }

    private void updateUI(){
        //show/hide empty list view holder
        if(mCursor.getCount() == 0){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed(){
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_again), Toast.LENGTH_SHORT).show();

        //2 second wait
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCursor.close();
    }

    @Override
    public void onViewHolderClick(int pos){
        Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtra(BUNDLE_KEY_USER, mCurrentUser);
        mCursor.moveToPosition(pos);
        int rowId = mCursor.getInt(mCursor.getColumnIndex(DBHeaders.ContactEntry._ID));
        intent.putExtra(BUNDLE_KEY_ROW_ID, rowId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.az_first_name) {
            mCursor = getCursor(DBHeaders.ContactEntry.COLUMN_NAME_FIRST_NAME, true);
            contactsAdapter.changeCursor(mCursor);
            return true;
        }
        if (id == R.id.za_first_name) {
            mCursor = getCursor(DBHeaders.ContactEntry.COLUMN_NAME_FIRST_NAME, false);
            contactsAdapter.changeCursor(mCursor);
            return true;
        }
        if (id == R.id.az_last_name) {
            mCursor = getCursor(DBHeaders.ContactEntry.COLUMN_NAME_LAST_NAME, true);
            contactsAdapter.changeCursor(mCursor);
            return true;
        }
        if (id == R.id.za_last_name) {
            mCursor = getCursor(DBHeaders.ContactEntry.COLUMN_NAME_LAST_NAME, false);
            contactsAdapter.changeCursor(mCursor);
            return true;
        }
        if (id == R.id.disable_sort) {
            mCursor = getCursor();
            contactsAdapter.changeCursor(mCursor);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Cursor getCursor(){
        return db.query(DBHeaders.ContactEntry.TABLE_NAME, null,
                DBHeaders.ContactEntry.COLUMN_NAME_USER_ID + " = ?",
                new String[] {mCurrentUser},
                null, null, null);
    }

    private Cursor getCursor(String orderBy, boolean ascending){
        String sortOrder = ascending ? " ASC" : " DESC";
        return db.query(DBHeaders.ContactEntry.TABLE_NAME, null,
                DBHeaders.ContactEntry.COLUMN_NAME_USER_ID + " = ?",
                new String[] {mCurrentUser},
                null, null, orderBy + sortOrder);
    }
}
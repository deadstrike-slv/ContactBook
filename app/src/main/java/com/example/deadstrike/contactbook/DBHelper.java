package com.example.deadstrike.contactbook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    // DO NOT FORGET!
    // If the database schema changes, increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MyContactBook.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + DBHeaders.ContactEntry.TABLE_NAME + " (" +
                    DBHeaders.ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DBHeaders.ContactEntry.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    DBHeaders.ContactEntry.COLUMN_NAME_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHeaders.ContactEntry.COLUMN_NAME_LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHeaders.ContactEntry.COLUMN_NAME_PHONE_NUMBER + TEXT_TYPE + COMMA_SEP +
                    DBHeaders.ContactEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + " );";

    private static final String SQL_DELETE_CONTACTS_TABLE =
            "DROP TABLE IF EXISTS " + DBHeaders.ContactEntry.TABLE_NAME;

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this time is unnecessary
        db.execSQL(SQL_DELETE_CONTACTS_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

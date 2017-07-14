package com.example.deadstrike.contactbook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    // DO NOT FORGET!
    // If the database schema changes, increment the database version.
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "MyContactBook.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + DBHeaders.Contacts.TABLE_NAME + " (" +
                    DBHeaders.Contacts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DBHeaders.Contacts.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    DBHeaders.Contacts.COLUMN_NAME_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHeaders.Contacts.COLUMN_NAME_LAST_NAME + TEXT_TYPE + " );";

    private static final String SQL_DELETE_CONTACTS_TABLE =
            "DROP TABLE IF EXISTS " + DBHeaders.Contacts.TABLE_NAME;

    private static final String SQL_CREATE_NUMBERS_TABLE =
            "CREATE TABLE " + DBHeaders.PhoneNumbers.TABLE_NAME + " (" +
                    DBHeaders.PhoneNumbers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DBHeaders.PhoneNumbers.COLUMN_NAME_CONTACT_ID + INT_TYPE + COMMA_SEP +
                    DBHeaders.PhoneNumbers.COLUMN_NAME_PHONE_NUMBER + TEXT_TYPE + " );";

    private static final String SQL_DELETE_NUMBERS_TABLE =
            "DROP TABLE IF EXISTS " + DBHeaders.PhoneNumbers.TABLE_NAME;

    private static final String SQL_CREATE_EMAILS_TABLE =
            "CREATE TABLE " + DBHeaders.Emails.TABLE_NAME + " (" +
                    DBHeaders.Emails._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DBHeaders.Emails.COLUMN_NAME_CONTACT_ID + INT_TYPE + COMMA_SEP +
                    DBHeaders.Emails.COLUMN_NAME_EMAIL + TEXT_TYPE + " );";

    private static final String SQL_DELETE_EMAILS_TABLE =
            "DROP TABLE IF EXISTS " + DBHeaders.Emails.TABLE_NAME;

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
        db.execSQL(SQL_CREATE_NUMBERS_TABLE);
        db.execSQL(SQL_CREATE_EMAILS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this time is unnecessary
        db.execSQL(SQL_DELETE_CONTACTS_TABLE);
        db.execSQL(SQL_DELETE_NUMBERS_TABLE);
        db.execSQL(SQL_DELETE_EMAILS_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

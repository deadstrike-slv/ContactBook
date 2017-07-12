package com.example.deadstrike.contactbook;

import android.provider.BaseColumns;

public final class DBHeaders {
    public abstract  class ContactEntry implements BaseColumns{
        static final String TABLE_NAME = "t_contacts";
        static final String COLUMN_NAME_USER_ID = "user";
        public static final String COLUMN_NAME_FIRST_NAME = "firstname";
        public static final String COLUMN_NAME_LAST_NAME = "lastname";
        public static final String COLUMN_NAME_PHONE_NUMBER = "phone";
        public static final String COLUMN_NAME_EMAIL = "email";
    }
}

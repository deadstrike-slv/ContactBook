package com.example.deadstrike.contactbook;

import android.provider.BaseColumns;

public final class DBHeaders {
    public abstract class Contacts implements BaseColumns {
        public static final String COLUMN_NAME_FIRST_NAME = "firstname";
        public static final String COLUMN_NAME_LAST_NAME = "lastname";
        static final String TABLE_NAME = "t_contacts";
        static final String COLUMN_NAME_USER_ID = "userId";
    }

    public abstract class PhoneNumbers implements BaseColumns {
        public static final String COLUMN_NAME_PHONE_NUMBER = "phone";
        static final String TABLE_NAME = "t_numbers";
        static final String COLUMN_NAME_CONTACT_ID = "contact_id";
    }

    public abstract class Emails implements BaseColumns {
        public static final String COLUMN_NAME_EMAIL = "email";
        static final String TABLE_NAME = "t_emails";
        static final String COLUMN_NAME_CONTACT_ID = "contact_id";
    }
}

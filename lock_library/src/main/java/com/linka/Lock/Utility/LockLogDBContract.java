package com.linka.Lock.Utility;

import android.provider.BaseColumns;

/**
 * Created by Loren-Admin on 12/29/2015.
 */
public class LockLogDBContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public LockLogDBContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class LockLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LOCKID = "lockID";
        public static final String COLUMN_NAME_LOCK_SECONDS = "lock_seconds";
        public static final String COLUMN_NAME_LOCK_STATE = "lock_state";
        public static final String COLUMN_NAME_AUTH_STATE = "auth_state";
        public static final String COLUMN_NAME_BATT_VOLTAGE = "batt_voltage";
        public static final String COLUMN_NAME_BATT_PERCENT = "batt_percentage";
        public static final String COLUMN_NAME_APP_TIMESTAMP = "app_timestamp_millis";
        public static final String COLUMN_NAME_LOCK_STATUS_FLAGS = "state_flags";
        public static final String COLUMN_NAME_LOCK_STATE_FLAGS = "status_flags";
        public static final String COLUMN_NAME_ACCEL_X = "accel_x";
        public static final String COLUMN_NAME_ACCEL_Y = "accel_y";
        public static final String COLUMN_NAME_ACCEL_Z = "accel_z";
        public static final String COLUMN_NAME_CURRENT = "current_mA";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_NULLABLE = COLUMN_NAME_NOTES;

    }
}

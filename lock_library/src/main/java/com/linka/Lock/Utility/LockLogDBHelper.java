package com.linka.Lock.Utility;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.linka.Lock.FirmwareAPI.Comms.LockAdV1;
import com.linka.Lock.FirmwareAPI.Comms.LockInfoPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by Loren-Admin on 12/29/2015.
 */
public class LockLogDBHelper extends SQLiteOpenHelper {
    private final static String TAG = LockLogDBHelper.class.getSimpleName();
    private long m_lastRowID = 0;
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LockLogDBContract.LockLogEntry.TABLE_NAME + " (" +
                    LockLogDBContract.LockLogEntry._ID + " INTEGER PRIMARY KEY," +                          // 0
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCKID + TEXT_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_APP_TIMESTAMP + INTEGER_TYPE + COMMA_SEP +   // 3
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_SECONDS + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_AUTH_STATE + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_BATT_PERCENT + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_BATT_VOLTAGE + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE_FLAGS + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATUS_FLAGS + INTEGER_TYPE + COMMA_SEP +   // 10
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_X + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_Y + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_Z + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_CURRENT + INTEGER_TYPE + COMMA_SEP +
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_NOTES + TEXT_TYPE + // COMMA_SEP +               // 14

                    // Any other options for the CREATE command
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LockLogDBContract.LockLogEntry.TABLE_NAME;

    private static final String SQL_GET_COLUMN_NAMES =
            "PRAGMA table_info(" + LockLogDBContract.LockLogEntry.TABLE_NAME + ")";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "LockDataLog.db";

    public LockLogDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.i(TAG, "Log db " + getDatabaseName() + " ver " + getReadableDatabase().getVersion());
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void deleteLog()
    {
        deleteDatabase(getWritableDatabase());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        deleteDatabase(db);
    }

    public void deleteDatabase (SQLiteDatabase db) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void addLogEntry(String DeviceAddress, LockAdV1 pkt)
    {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ENTRY_ID, id);
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_TITLE, title);
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_CONTENT, content);
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCKID, DeviceAddress);
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_APP_TIMESTAMP, System.currentTimeMillis());
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_BATT_PERCENT, pkt.GetBatteryPercent());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_BATT_VOLTAGE, pkt.GetBatteryVoltage());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_AUTH_STATE, pkt.GetAuthState().GetValue());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE, (int)pkt.GetLockState().GetValue());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATUS_FLAGS, pkt.GetStatusFlags());
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE_FLAGS, pkt.GetStateFlags());

        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_X, pkt.GetAccelX());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_Y, pkt.GetAccelY());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_Z, pkt.GetAccelZ());
//        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_CURRENT, p);

        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_AUTH_STATE, pkt.);

// Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                LockLogDBContract.LockLogEntry.TABLE_NAME,
                LockLogDBContract.LockLogEntry.COLUMN_NAME_NULLABLE,
                values);
        m_lastRowID = newRowId;
    }

    public void addLogEntry(String DeviceAddress, LockInfoPacket pkt)
    {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCKID, DeviceAddress);
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE, (int) pkt.GetLockState().GetValue());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_X, pkt.GetAccelX());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_Y, pkt.GetAccelY());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ACCEL_Z, pkt.GetAccelZ());
    }

    public void addLogEntry(String DeviceAddress, LockStatusPacket pkt) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_ENTRY_ID, id);
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_TITLE, title);
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_CONTENT, content);
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCKID, DeviceAddress);
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_APP_TIMESTAMP, System.currentTimeMillis());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_BATT_PERCENT, pkt.GetBatteryPercent());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_BATT_VOLTAGE, pkt.GetBatteryVoltage());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_AUTH_STATE, pkt.GetAuthState().GetValue());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE, (int) pkt.GetLockState().GetValue());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATUS_FLAGS, pkt.GetStatusFlags());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_LOCK_STATE_FLAGS, pkt.GetStateFlags());
        values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_CURRENT, pkt.GetCurrent_mA());
        //values.put(LockLogDBContract.LockLogEntry.COLUMN_NAME_AUTH_STATE, pkt.);

// Insert the new row, returning the primary key value of the new row
        try {
            long newRowId;
            newRowId = db.insert(
                    LockLogDBContract.LockLogEntry.TABLE_NAME,
                    LockLogDBContract.LockLogEntry.COLUMN_NAME_NULLABLE,
                    values);
            m_lastRowID = newRowId;
        } catch (android.database.sqlite.SQLiteException e) {
            // If we get here, we've changed the schema probably and we need to delete the table and start over.
            Log.d(TAG, "Can't insert, ensure database version rolled when revising schema." + e.toString());
        }
    }

    public long getDBSizeBytes()
    {
        return getWritableDatabase().getPageSize();
    }

    public long getDBLastRowID()
    {
        return m_lastRowID;
    }

    public String toString ()
    {
        return String.format("DB %d entries max %d bytes.", getDBLastRowID(), getDBSizeBytes());
    }

    public String getColumnNames ()
    {
        SQLiteDatabase db = getReadableDatabase();

       // Cursor cursor_2 = db.query(LockLogDBContract.LockLogEntry.TABLE_NAME,
       //         new String[] {"_id", "TOPIC", "TITLE"}, "TOPIC = ?",
       //         new String[]{ ""+SEARCH_TERM+"" }, null, null, null, null);
        Cursor cursor_2 = db.rawQuery(SQL_GET_COLUMN_NAMES, null);
        //db.execSQL(SQL_GET_COLUMN_NAMES);

        if (cursor_2 == null)
            return null;

        try{
            if (cursor_2.moveToFirst()) // Here we try to move to the first record
                return cursor_2.getString(2); // Only assign string value if we moved to first record
        }finally {
            cursor_2.close();
        }
        return null;
    }

    private File getExportFile ()
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        final String CSVFileName = "LockDataLog.csv";
        File file = new File(exportDir, CSVFileName);
        Log.d(TAG, "CSV file path is " + exportDir + CSVFileName);
        return file;
    }

    public void exportCSV() {
        //File dbFile=getDatabasePath("MyDBName.db");
        //DBHelper dbhelper = new DBHelper(getApplicationContext());
        File file = getExportFile();
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM "+ LockLogDBContract.LockLogEntry.TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort -- \note this must not exceed actual DB columns \todo make this automatic
                String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8),
                        curCSV.getString(9), curCSV.getString(10), curCSV.getString(11), curCSV.getString(12), curCSV.getString(13), curCSV.getString(14)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }
    }

    public void sendCSVEmail(Activity activity, String email) {
        sendCSVEmail (activity, email, "");
    }

    public void sendCSVEmail(Activity activity, String email, String emailBody) {
        final String MAIL_SUBJECT = "Linka log file";
        final String MAIL_BODY = "Attached is a csv Log file from Linka lock xyz.\r\n" + emailBody;
        //File file = new File(Environment.getExternalStorageState()+"/folderName/" + fileName+ ".xml");
        File file = getExportFile();
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, MAIL_SUBJECT);
        String to[] = { email };
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_TEXT, MAIL_BODY);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        activity.startActivityForResult(Intent.createChooser(intent, "Send mail..."), 1222);
    }

}

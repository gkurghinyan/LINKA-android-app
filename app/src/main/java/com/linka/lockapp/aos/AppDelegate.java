package com.linka.lockapp.aos;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.facebook.FacebookSdk;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceManager;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.AppLocationService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.helpers.ImageHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.helpers.SleepNotificationService;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.model.User;
import com.pixplicity.easyprefs.library.Prefs;

import net.danlew.android.joda.JodaTimeAndroid;

import org.acra.ACRA;

import java.lang.reflect.Field;
import java.util.Collection;

import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by van on 13/7/15.
 */
//@ReportsCrashes(
//        mailTo = "vanson@vanportdev.com",
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.crash_toast_text
//)
public class AppDelegate extends MultiDexApplication {


    public static AppDelegate instance;

    public static AppDelegate getInstance() {
        return instance;
    }

    //---------------------------------------------//
    //Initialize Constants used throughout the app:

    public static boolean isForeground;

    public static boolean shouldAllowTapOnLockWidgetToLock = false;
    public static boolean shouldAllowTapOnLockWidgetToUnlock = true;
    public static boolean shouldAllowMultipleAutoConnect = false;
    public static int min_rssi_autobackinbound = -99;
    public static int min_rssi_autooutofbound = -1000;
    //public static int min_rssi_autoconnect = -90;
    public static boolean shouldAllowAutoUnlockCriteriaFixed = true;
    //public static boolean shouldAllowAutoUnlockCriteriaInc = true;
    public static int rssi_initial = -50;
    public static int min_rssi_autounlock_0 = -70;
    //public static int min_rssi_autounlock_inc_1 = -75;
    //public static int min_rssi_autounlock_inc_2 = -70;
    //public static int min_rssi_autounlock_inc_3 = -65;
    //public static int min_rssi_autounlock_to_standby_0 = -90;
    public static int battery_mid = 74;
    public static int battery_low_below = 30;
    public static int battery_critically_low_below = 10;
    public static int default_lock_sleep_time = 10800; // 3 hours
    public static int default_unlock_sleep_time = 7200; //30 mins
    //public static int battery_full_life_by_days = 270;
    public static boolean shouldEnableLocationScanning = true;
    public static int locationScanningInterval = 10000;
    public static boolean shouldShowCustomOpenInMapsButton = false;
    public static boolean shouldLimitLinkaAccessToUserID = true;
    public static boolean shouldOnlyAllowSingleLockConnectionAtTheSameTime = true;
    public static String GCM_SENDER_ID = "680869370414";
    public static String linkaMinRequiredFirmwareVersion = "1.4.3";
    public static boolean linkaMinRequiredFirmwareVersionIsCriticalUpdate = true;
    //public static boolean shouldShowDebugLogOnScreen = false;
    public static boolean shouldShowSelectLanguage = true;
    public static boolean shouldAlwaysEnableFwUpgradeButton = false;

    //---------------------------------------------//


    public Location getCurLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        /*
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        */

        //Use App Location Service instead of the above
        return AppLocationService.getInstance().getLocation();
    }


    //---------------------------------------------//
    //ANDROID MIGRATION

    //Everytime we add data to the Phone's database, we need to migrate here
    //We need to keep all migrations from previous app versions, in case they update from older versions
    //Phone Databases are all located in the Model Folder

    // Having some Active Android Migration issues
    // Scripts don't appear to be running
    // For now let's use this function to handle migrations
    public static boolean createIfNeedColumn(Class<? extends Model> type, String column) {
        boolean isFound = false;
        TableInfo tableInfo = new TableInfo(type);

        Cursor cursor = ActiveAndroid.getDatabase().query(tableInfo.getTableName(),null,null,null,null,null,null);
        String columnNames[] = cursor.getColumnNames();
        for (String f : columnNames) {
            if (column.equals(f)) {
                LogHelper.e("Active Android","Column " + column + " FOUND");
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            ActiveAndroid.execSQL("ALTER TABLE " + tableInfo.getTableName() + " ADD COLUMN " + column + " TEXT;");
            LogHelper.e("Active Android","Column " + column + " CREATED");
        }
        return isFound;
    }

    public static void createTable(Class<? extends Model> type) {

        TableInfo tableInfo = new TableInfo(type);

        Collection<Field> fields = tableInfo.getFields();

        //Get the column names from the table
        String tableColumns = "";
        for(Field thisField : fields){

            String fieldName = tableInfo.getColumnName(thisField);
            if(tableColumns.isEmpty()){
                tableColumns = fieldName;
            }else{
                tableColumns = tableColumns + ", " + fieldName;
            }
        }
        LogHelper.e("Table" , tableColumns);

        //Create the table!!
        ActiveAndroid.execSQL("CREATE TABLE IF NOT EXISTS " + tableInfo.getTableName() + " (" + tableColumns + ")");
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Set Log Level for App
        LogHelper.logLevel = LogHelper.LogLevel.DEBUG;

//        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        // Normal app init code...

        instance = this;


        // DEBUG ONLY Stetho.initializeWithDefaults(this);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Helpers.load_device_token();
        Helpers.loadDeviceInfo();

        ACRA.init(this);
        JodaTimeAndroid.init(this);
        FacebookSdk.sdkInitialize(this);
        ImageHelpers.PicassoCache.setPicassoSingleton(this); //TODO: Verify if this is even needed or not
        EasyImage.configuration(this);

        // Use this as a last resort for migration if db versions get out of whack -> this.deleteDatabase("Linka.db");
        //this.deleteDatabase("Linka.db");
        Configuration config = new Configuration.Builder(this)
                .setDatabaseName("Linka.db")
                .setDatabaseVersion(14)
                .create();
        ActiveAndroid.initialize(config);

        // Handle Active Android Migrations here for now:
        // 11.sql

        //Updates from 1.6.1
        createIfNeedColumn(Linka.class, "lock_uuid");
        createIfNeedColumn(Linka.class, "actuations");
        createIfNeedColumn(Linka.class, "fw_version");
        createIfNeedColumn(Linka.class, "pac");
        createIfNeedColumn(Linka.class, "updateLockSettingsProfile");
        createIfNeedColumn(LinkaAccessKey.class, "v2_access_key_admin");
        createIfNeedColumn(LinkaAccessKey.class, "v2_access_key_admin_2");
        createIfNeedColumn(LinkaAccessKey.class, "v2_access_key_user");
        createIfNeedColumn(LinkaAccessKey.class, "v2_access_key_user_2");

        //Updating app from 1.7
        createIfNeedColumn(Linka.class, "settings_pulse_tap");
        createIfNeedColumn(Linka.class, "settings_alarm_delay");
        createIfNeedColumn(Linka.class, "settings_alarm_time");
        createIfNeedColumn(Linka.class, "settings_jostle_ms");
        createIfNeedColumn(Linka.class, "settings_roll_alrm_deg");
        createIfNeedColumn(Linka.class, "settings_pitch_alrm_deg");
        createIfNeedColumn(Linka.class, "settings_accel_datarate");
        createIfNeedColumn(Linka.class, "settings_bump_threshold");
        createIfNeedColumn(Linka.class, "settings_stall_delay");
        createIfNeedColumn(Linka.class, "settings_unlocked_sleep");
        createIfNeedColumn(Linka.class, "settings_locked_sleep");
        createIfNeedColumn(Linka.class, "pacIsSet");

        createIfNeedColumn(LinkaNotificationSettings.class, "settings_sleep_notification");

        //Updating to 2.0
        createIfNeedColumn(LinkaAccessKey.class, "key_id");
        createIfNeedColumn(Linka.class, "isUnlocked");

        //Updating to 2.1
        createIfNeedColumn(Linka.class, "is_auto_unlocked");
        createIfNeedColumn(Linka.class, "settings_quick_lock");
        createIfNeedColumn(Linka.class, "settings_sleep_performance");
        createIfNeedColumn(Linka.class, "awaits_for_auto_unlock");
        createIfNeedColumn(Linka.class, "waiting_until_settled_to_auto_lock");
        createIfNeedColumn(Linka.class, "auto_unlock_radius");
        createIfNeedColumn(Linka.class, "rssi_out_bound");

        createTable(User.class);

        BLEHelpers.initialize(this);
        AppBluetoothService.init(this);
        SleepNotificationService.init(this);
        LinkaAPIServiceManager.getInstance();

        AppLocationService.init(this);

        createShortcutIcon();
//        runUnitTests();
    }

    // END OF ANDROID MIGRATION
    //---------------------------------------------//

/*
    void runUnitTests() {
        Date oldDate = new Date();
        Date newDate = new Date();

        MutableDateTime dateTime = new MutableDateTime(newDate);
        dateTime.addDays(0);
        dateTime.addHours(6);
        dateTime.addMinutes(30);
        dateTime.addSeconds(30);
        newDate = dateTime.toDate();

        long diffMs = newDate.getTime() - oldDate.getTime();
        long diffSec = TimeUnit.MILLISECONDS.toSeconds(diffMs);



        long after_sec = diffSec;
        long after_min = (after_sec / 60) % 60;
        long after_hours = ((after_sec / 60) / 60) % 24;
        long after_days = (((after_sec / 60) / 60) / 24);

        String after = "";
        long after_depth = 0;

        if (after_days > 0 && after_depth < 2) {
            after += "" + after_days + " days ";
            after_depth += 1;
        }
        if (after_hours > 0 && after_depth < 2) {
            after += "" + after_hours + " hours ";
            after_depth += 1;
        }
        if (after_min > 0 && after_depth < 2) {
            after += "" + after_min + " mins ";
            after_depth += 1;
        }
        if (after_sec > 0 && after_depth < 3) {
            after += "" + after_sec + " secs ";
            after_depth += 1;
        }


        String str = "Unlocked after " + after;

        Toast.makeText(this, "PrettyDate Test: " + str, Toast.LENGTH_LONG).show();
    }
*/

    void createShortcutIcon(){

        // Checking if ShortCut was already added
        SharedPreferences sharedPreferences = Prefs.getPreferences();
        boolean shortCutWasAlreadyAdded = sharedPreferences.getBoolean("SHORTCUT_ADDED", false);
        if (shortCutWasAlreadyAdded) return;

        Intent shortcutIntent = new Intent(getApplicationContext(), AppMainActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);

        // Remembering that ShortCut was already added
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SHORTCUT_ADDED", true);
        editor.commit();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }


// VISIBILITY

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
        isForeground = true;
    }

    public static void activityPaused() {
        activityVisible = false;
        isForeground = false;
    }

    private static boolean activityVisible;
}

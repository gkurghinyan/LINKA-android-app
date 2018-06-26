package com.linka.lockapp.aos.module.model;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.linka.Lock.BLE.BluetoothLEDevice;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;
import com.linka.Lock.FirmwareAPI.Types.AuthState;
import com.linka.Lock.FirmwareAPI.Types.LockState;
import com.linka.Lock.FirmwareAPI.Types.StateTransitionReason;
import com.linka.Lock.FirmwareAPI.Types.SystemFlags;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.helpers.SleepNotificationService;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import br.com.goncalves.pugnotification.notification.PugNotification;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 18/2/16.
 */
@Table(name = "Linkas", id = BaseColumns._ID)
public class Linka extends Model implements Serializable {
    public boolean isConnected = false;
    public boolean isLockSettled = false; // "Settled" means lock is connected, and we have read a setting.  Light should be blinking green

    @Column(name = "lock_address", index = true)
    public String lock_address = "";

    @Column(name = "lock_mac_address", index = true)
    public String lock_mac_address = "";

    @Column(name = "lock_name")
    public String lock_name = "Linka Lock"; // TODO: lock + serial no

    @Column(name = "lock_uuid")
    public UUID lock_uuid = null;

    @Column(name = "fw_version")
    public String fw_version = "";

    @Column(name = "pac")
    public int pac = 0;

    @Column(name = "pacIsSet")
    public boolean pacIsSet = false;

    @Column(name = "actuations")
    public long actuations = -1;

    @Column(name = "isLocked")
    public boolean isLocked = false;

    @Column(name = "isUnlocked")
    public boolean isUnlocked = false;

    public boolean isLocking = false;
    public boolean isUnlocking = false;

    public boolean isCharging;

    @Column(name = "batteryPercent")
    public int batteryPercent;

    //Battery percent displayed will be different from the % received from firmware
    //because when charging, battery % jumps. Therefore, must take this into account.
    //Also need to take into account LED shows green at 98%
    public int origBatteryPercent;

    public int lastChargingPercent = 100;
    public int lastUnpluggedPercent = 0;

    @Column(name = "lockState")
    public int lockState;

    @Column(name = "authState")
    public int authState;

    @Column(name = "isRecorded")
    public boolean isRecorded = false;


    @Column(name = "latitude")
    public String latitude;
    @Column(name = "longitude")
    public String longitude;


    @Column(name = "rssi")
    public int rssi = -1000;

    public boolean tamperStatus = false;

    public double temperature = -100;


    @Column(name = "timestamp_locked")
    public String timestamp_locked = "";
    public String timestamp_unlocked = "";


    @Column(name = "settings_audible_locking_unlocking")
    public boolean settings_audible_locking_unlocking = false;
    @Column(name = "settings_tamper_siren")
    public boolean settings_tamper_siren = true;
    @Column(name = "settings_auto_unlocking")
    public boolean settings_auto_unlocking = false;
    @Column(name = "settings_stall_override")
    public boolean settings_stall_override = false;


    //alarm delay
    @Column(name = "settings_alarm_delay")
    public int settings_alarm_delay = 0;

    //alarm time
    @Column(name = "settings_alarm_time")
    public int settings_alarm_time = 0;

    //bump threshold
    @Column(name = "settings_bump_threshold")
    public int settings_bump_threshold = 0;

    //pulse tap
    @Column(name = "settings_pulse_tap")
    public int settings_pulse_tap = 0;

    //jostle
    @Column(name = "settings_jostle_ms")
    public int settings_jostle_ms = 0;

    //roll
    @Column(name = "settings_roll_alrm_deg")
    public int settings_roll_alrm_deg = 0;

    //tilt
    @Column(name = "settings_pitch_alrm_deg")
    public int settings_pitch_alrm_deg = 0;

    //accel datarate
    @Column(name = "settings_accel_datarate")
    public int settings_accel_datarate = 0;

    //Stall Delay
    @Column(name = "settings_stall_delay")
    public int settings_stall_delay = 30;

    //locked sleep sec
    @Column(name = "settings_unlocked_sleep")
    public int settings_unlocked_sleep = 0;

    //unlocked sleep sec
    @Column(name = "settings_locked_sleep")
    public int settings_locked_sleep = 0;

    @Column(name = "api_user_id")
    public String api_user_id = "";

    @Column(name = "updateLockSettingsProfile")
    public boolean updateLockSettingsProfile = false; // After FW update or initial pairing set this to true
    public boolean updateAppSettingsProfile = true; // After App update or initial pairing this is true

    @Column(name = "awaits_for_auto_unlock")
    public boolean awaitsForAutoUnlocking = false;

    @Column(name = "waiting_until_settled_to_auto_lock")
    private boolean waitingUntilSettledtoAutoUnlock = false;

    public boolean canRecordStall = true;
    public boolean canRecordBatteryLow = true;
    public boolean canRecordBatteryCriticallyLow = true;
    public boolean canRecordTamperAlert = true;

    @Column(name = "rssi_out_bound")
    private boolean rssi_outOfBounds = false; // Prevents auto unlocking unless user has walked out of bounds


    public boolean canAlertCriticalFirmwareUpdate = true;


    public Linka() {
        super();
    }


    public String getMACAddress() {
        return lock_mac_address;
    }

    public String getUUIDAddress() {
        return lock_address;
    }

    public void setLock_mac_address(String lock_mac_address){
        this.lock_mac_address = lock_mac_address;
    }


    public String getName() {
        String name = lock_name;
        LinkaName linkaName = LinkaName.getLinkaNameForMACAddress(getMACAddress());
        if (linkaName != null)
        {
            name = linkaName.name;
        }
        return name;
    }


    public static String getName(String lock_mac_address, BluetoothLEDevice item)
    {
        String name = item.getName();
        LinkaName linkaName = LinkaName.getLinkaNameForMACAddress(lock_mac_address);
        if (linkaName != null)
        {
            name = linkaName.name;
        }
        return name;
    }




    public boolean isBonded() {
        return authState == AuthState.AUTH_COMPLETE;
    }

    public boolean isUnlocked() {
        return !isLocking && !isUnlocking && !isLocked;
    }

    public static List<Linka> getLinkas() {
        From from = new Select().from(Linka.class);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("api_user_id = ?", "UNDEFINED");
            } else {
                from = from.where("api_user_id = ?", userID);
            }
        }
        List<Linka> linkas = from.execute();

        return linkas;
    }

    public static Linka getLinkaById(long id) {
        From from = new Select().from(Linka.class).where("_id = ?", id);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("api_user_id = ?", "UNDEFINED");
            } else {
                from = from.where("api_user_id = ?", userID);
            }
        }
        Linka linka = from.executeSingle();
        return linka;
    }

    public static Linka getLinkaByAddress(String address) {
        From from = new Select().from(Linka.class).where("lock_address = ?", address);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("api_user_id = ?", "UNDEFINED");
            } else {
                from = from.where("api_user_id = ?", userID);
            }
        }
        Linka linka = from.executeSingle();
        return linka;
    }


    public static Linka getLinkaFromLockController(Linka linka) {
        LockController lockController = LocksController.getInstance().getLockController();
        if (lockController != null) {
            Linka _linka = lockController.getLinka();
            return _linka;
        }
        return null;
    }


    public Date getDateTimestampLocked() {
        if (timestamp_locked != null && !timestamp_locked.equals("")) {
            Date date = new Date(Long.parseLong(timestamp_locked));
            return date;
        }
        return null;
    }


    public String getBatteryRemainingRepresentation(int unlock_time, int lock_time) {
        //new implement

        //time
        int one_day_sec = 24*60*60;
//        int unlocked_connected_sec = settings_unlocked_sleep;//E7
//        int locked_connected_sec = Math.round(settings_locked_sleep*0.5f);//E8
//        int locked_awake_sec = Math.round(settings_locked_sleep*0.5f);//E9

        int unlocked_connected_sec;
        //if between 0 and 30 min:
        if(unlock_time < 1800){
            unlocked_connected_sec = unlock_time*2;
        }else if(unlock_time < 3600){ //if between 30 min and 1 hr
            unlocked_connected_sec = (int)((unlock_time-1800)*0.8) + 3600;
        }else if (unlock_time <  18000) { // if between 1 hour and 5 hours
            unlocked_connected_sec = (int)((unlock_time-3600)*0.25) + 5040;
        }else { //Up to 10 hours
            unlocked_connected_sec = (int)((unlock_time - 18000)*0.13) + 8640;
        }

        int locked_connected_sec = 10*60;//10 min
        int locked_awake_sec = lock_time*2;//E9
        int unlocking_motor_on = 3*4;//3sec * 4 times //E12
        int locking_motor_on = 3*4;//3sec * 4 times //E13
        int siren_on_sec = 10;//E14
        int non_sleep_total_time = unlocked_connected_sec + locked_connected_sec + locked_awake_sec + unlocking_motor_on + locking_motor_on + siren_on_sec;
        int locked_sleep_sec = one_day_sec - non_sleep_total_time;
        int unlocked_sleep_sec = 0;

        //mah
        double unlocked_connected_draw_mah = 0.91;
        double locked_connected_draw_mah = 0.91;
        double locked_awake_draw_mah = 0.475;
        double locked_sleep_draw_mah = 0.012;
        double unlocked_sleep_draw_mah = 0.012;
        double unlocking_motor_on_draw_mah = 80;
        double locking_motor_on_draw_mah = 80;
        double siren_on_draw_mah = 80;

        double  unlocked_connected_draw_mah_pre_day = ((unlocked_connected_draw_mah/60)/60)*unlocked_connected_sec;
        double  locked_connected_draw_mah_pre_day = ((locked_connected_draw_mah/60)/60)*locked_connected_sec;
        double  locked_awake_draw_mah_pre_day = ((locked_awake_draw_mah/60)/60)*locked_awake_sec;
        double  locked_sleep_draw_mah_pre_day = ((locked_sleep_draw_mah/60)/60)*locked_sleep_sec;
        double  unlocked_sleep_draw_mah_pre_day = ((unlocked_sleep_draw_mah/60)/60)*unlocked_sleep_sec;
        double  unlocking_motor_draw_mah_pre_day = ((unlocking_motor_on_draw_mah/60)/60)*unlocking_motor_on;
        double  locking_motor_draw_mah_pre_day = ((locking_motor_on_draw_mah/60)/60)*locking_motor_on;
        double  siren_on_draw_mah_pre_day = ((siren_on_draw_mah/60)/60)*siren_on_sec;


        double total_power_consume_mah = unlocked_connected_draw_mah_pre_day+locked_connected_draw_mah_pre_day
                +locked_awake_draw_mah_pre_day+locked_sleep_draw_mah_pre_day+unlocked_sleep_draw_mah_pre_day
                +unlocking_motor_draw_mah_pre_day+locking_motor_draw_mah_pre_day+siren_on_draw_mah_pre_day;


        //Calculate remaining battery capacity
        double percent = (double)batteryPercent / 100;

        //Make it value very conservative: Display 0 days at 9%
        if (percent <0.09){
            percent = 0;
        }else{
            percent -= 0.09;
        }

        double total_full_power_link = 700;
        double total_remain_power_link = total_full_power_link* percent;


        double remaining_days = total_remain_power_link/total_power_consume_mah;
        //double remaining_days = AppDelegate.battery_full_life_by_days * percent;

        int days = (int)Math.floor(remaining_days);
        int hours = (int)Math.floor((remaining_days * 24) % 24);
        int mins = (int)Math.floor((remaining_days * 24 * 60) % 60);
        int secs = (int)Math.floor((remaining_days * 24 * 60 * 60) % 60);

        String after = "";
        long after_depth = 0;

        if (days >= 0 && after_depth < 1) {
            after += "" + days + " " + (days != 1 ? _.i(R.string.days) : _.i(R.string.day)) + "";
            after_depth += 1;
        }
        /*
        if (hours > 0 && after_depth < 1) {
            after += "" + hours + " " + (hours != 1 ? _.i(R.string.hrs) : _.i(R.string.hr)) + "";
            after_depth += 1;
        }
        if (mins > 0 && after_depth < 1) {
            after += "" + mins + " " + (mins != 1 ? _.i(R.string.mins) : _.i(R.string.min)) + "";
            after_depth += 1;
        }
        if (secs > 0 && after_depth < 1) {
            after += "" + secs + " " + (secs != 1 ? _.i(R.string.secs) : _.i(R.string.sec)) + "";
            after_depth += 1;
        }*/

        after = after.trim();
        return after;
    }

    public String getEstimatedBatteryRemaining(int unlock_time, int lock_time){
        String[] raw = getBatteryRemainingRepresentation(unlock_time, lock_time).split(" ");
        return raw[0]+" "+_.i(R.string.estimated)+" "+raw[1]+" "+_.i(R.string.remaining);
    }





    public static Linka makeLinka(BluetoothLEDevice item) {
        String lock_address = item.getAddress();
        String lock_mac_address = item.getAddress();
        if (item.getAdvData() != null && item.getAdvData().GetMACAddr_str() != null) {
            lock_mac_address = item.getAdvData().GetMACAddr_str();
        }

        String lock_name = Linka.getName(lock_mac_address, item);

        return makeLinka(lock_address, lock_mac_address, lock_name);
    }

    public static Linka makeLinka(String lock_address, String lock_mac_address, String lock_name) {

        Linka linka = new Linka();
        linka.lock_name = lock_name;
        linka.lock_address = lock_address;
        linka.lock_mac_address = lock_mac_address;
        linka.rssi = AppDelegate.rssi_initial;
        linka.api_user_id = LinkaAPIServiceImpl.getUserID();
        return linka;
    }




    public static Linka saveLinka(BluetoothLEDevice item, boolean allow_override) {
        long linka_id = -1;
        String lock_address = item.getAddress();
        String lock_mac_address = item.getAddress();
        if (item.getAdvData() != null && item.getAdvData().GetMACAddr_str() != null) {
            lock_mac_address = item.getAdvData().GetMACAddr_str();
        }

        String lock_name = Linka.getName(lock_mac_address, item);

        Linka existing_linka = Linka.getLinkaByAddress(lock_address);
        if (existing_linka != null) {
            if (allow_override) {
                Linka linka = existing_linka;
//                linka.lock_name = lock_name;
                linka_id = linka.save();
            }
            return existing_linka;
        }

        Linka linka = makeLinka(lock_address, lock_mac_address, lock_name);
        linka_id = linka.save();

        if (linka_id != -1) {
            Linka _linka = new Select().from(Linka.class).where("_id = ?", linka_id).executeSingle();
            return _linka;
        }
        return null;
    }






    void tryRecordStall() {
        if (canRecordStall) {
            LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isStalled);
            canRecordStall = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    canRecordStall = true;
                }
            }, 6000);
        }
    }

    void tryRecordBatteryLow(LockStatusPacket lockStatusData) {
        if (canRecordStall) {
            LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isBatteryLow, lockStatusData.GetBatteryPercent());
            canRecordBatteryLow = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    canRecordBatteryLow = true;
                }
            }, 1000 * 180);
        }
    }

    void tryRecordBatteryCriticallyLow(LockStatusPacket lockStatusData) {
        if (canRecordStall) {
            LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isBatteryCriticallyLow, lockStatusData.GetBatteryPercent());
            canRecordBatteryCriticallyLow = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    canRecordBatteryCriticallyLow = true;
                }
            }, 1000 * 180);
        }
    }

    void tryRecordTamperAlert(LockStatusPacket lockStatusData) {
        if (canRecordTamperAlert) {
            if (this.settings_tamper_siren)
            {
                LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isTamperAlert);
                canRecordTamperAlert = false;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        canRecordTamperAlert = true;
                    }
                }, 1000 * 30);
            }
        }
    }





    public boolean updateFromStatusData(boolean isConnected, LockStatusPacket lockStatusData) {
        if (lockStatusData != null) {
            if (this.isConnected != isConnected || this.authState != lockStatusData.GetAuthState().GetValue())
            {
                LogHelper.e("LINKA", "isConnected: " + (isConnected ? "true" : "false") + ", " + "state: " + lockStatusData.GetLockState() + "auth: " + lockStatusData.GetAuthState());
            }
        }

        boolean isChanged = false;

        //Make sure it's listed as connected
        if(isConnected == true){   //Only connected if it is not currently disconnecting.

            //Check to make sure bluetooth is turned on
            //On some phones, it remains connected even when bluetooth is turned off.
            //We need to explicitly disconnect when we detect that bluetooth is turned off
            BluetoothAdapter bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(AppBluetoothService.getInstance().getContext());
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    if(this.isConnected == false) {
                        this.isConnected = true;
                        isChanged = true;
                    }
                }
            }

        }else if(isConnected == false){

            if(this.isConnected == true) {
                this.isConnected = false;
                isChanged = true;
            }
        }


        if (lockStatusData != null) {
            if (lockStatusData.mTransitionReason == StateTransitionReason.REASON_STALL) {
                if (lockStatusData.mTransitionReasonPrev != lockStatusData.mTransitionReason) {
                    tryRecordStall();
                }
            }
        }


        if (lockStatusData != null) {

            //Check auth state
            if(this.authState != lockStatusData.GetAuthState().GetValue()) {
                this.authState = lockStatusData.GetAuthState().GetValue();
                isChanged = true;
            }

            //Check existing state
            if(this.lockState != lockStatusData.GetLockState().GetValue()) {
                this.lockState = lockStatusData.GetLockState().GetValue();
                isChanged = true;
            }

            int state = this.lockState;

            if (state == LockState.LOCK_LOCKED) {
                if (isLocking) {

                    isChanged = true;
                    LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isLocked);

                    //Lock is now locked, so reset sleep timer
                    SleepNotificationService.getInstance().restartTimer();

                }
                isLocked = true;
                isUnlocked = false;
                isLocking = false;
                isUnlocking = false;
            } else if (state == LockState.LOCK_LOCKING) {
                isLocking = true;
                isUnlocking = false;
                isLocked = false;
                isUnlocked = false;
                canRecordTamperAlert = true;
                awaitsForAutoUnlocking = false;
            } else if (state == LockState.LOCK_UNLOCKING){
                isUnlocking = true;
                isLocked = false;
                isUnlocked = false;
                isLocking = false;
                canRecordTamperAlert = true;
            } else if (state == LockState.LOCK_UNLOCKED){
                if (isUnlocking) {

                    isChanged = true;
                    LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isUnlocked);

                    //Lock is now unlocked, so reset sleep timer
                    SleepNotificationService.getInstance().restartTimer();

                }
                isLocked = false;
                isUnlocked = true;
                isUnlocking = false;
                isLocking = false;
            }

            int stateFlags = lockStatusData.GetStateFlags();
            if(stateFlags != 0) {
                boolean isCurrentlyCharging = SystemFlags.flagsToString(stateFlags).contains("Charging");

                //Set lastUnpluggedPercent and lastChargingPercent when charging state changes
                //If variables are unused, set to 100 and 0.
                if(isCurrentlyCharging) {
                    if (!isCharging) {
                        lastUnpluggedPercent = batteryPercent;
                    }
                    isCharging = true;
                    lastChargingPercent = 100;

                }else {     //If not currently charging
                    if (isCharging) {
                        lastChargingPercent = batteryPercent;
                    }
                    isCharging = false;
                    lastUnpluggedPercent = 0;

                }
            }

            if(origBatteryPercent != lockStatusData.GetBatteryPercent()) {
                if (origBatteryPercent > 0 && lockStatusData.GetBatteryPercent() > 0) {
                    if (origBatteryPercent >= AppDelegate.battery_low_below && lockStatusData.GetBatteryPercent() < AppDelegate.battery_low_below) {
                        tryRecordBatteryLow(lockStatusData);
                    } else if (origBatteryPercent >= AppDelegate.battery_critically_low_below && lockStatusData.GetBatteryPercent() < AppDelegate.battery_critically_low_below) {
                        tryRecordBatteryCriticallyLow(lockStatusData);
                    }
                }
                origBatteryPercent = lockStatusData.GetBatteryPercent();
                isChanged = true;

                //If it is charging, decrease the value by an algorithm.
                //If it is not charging, display the actual value.
                if(!isCharging){
                    batteryPercent = origBatteryPercent;
                }
                else{
                    //Subtract ~15-30% from the charging value
                    double calc1 = 0.471*origBatteryPercent;
                    double calc2 = 0.015*origBatteryPercent*origBatteryPercent;
                    batteryPercent = (int)(0.9 - calc1 + calc2);
                }

                //Make sure it's says charged if it's charged & there's a green light
                if(SystemFlags.flagsToString(stateFlags).contains("Charged")){
                    batteryPercent = 100;
                }else if (batteryPercent == 100){
                    batteryPercent = 99;
                }

                //Make sure that if charge reaches 100% when plugged in, if unplugging, the
                // battery indeed stays at 100% for at least some time
                if(!isCharging){
                    if(batteryPercent > 80){
                        double calc3 = (batteryPercent-80)*1.4;
                        batteryPercent = 80 + (int)(calc3); //Anything above 95% -> 100%
                    }
                }
                
                //Make sure lastChargingPercent never goes to 0
                if(lastChargingPercent == 0){
                    lastChargingPercent = 100;
                }
                
                //Make sure when unplugged, battery never goes above when charged
                //Also make sure when plugging in, battery never goes below when unplugged
                if(batteryPercent > lastChargingPercent){
                    batteryPercent = lastChargingPercent;
                }else if (batteryPercent < lastUnpluggedPercent){
                    batteryPercent = lastUnpluggedPercent;
                }

                //Make sure when not charging, battery % never goes up
                if(!isCharging){
                    if(batteryPercent < lastChargingPercent){
                        lastChargingPercent = batteryPercent;
                    }
                }

                //Make sure battery percent never exceeds 100%
                if(batteryPercent >100){
                    batteryPercent = 100;
                }
                LogHelper.i("BATTERY CALC" , "Original percent: " + Integer.toString(origBatteryPercent) + "New Percent:" + Integer.toString(batteryPercent));
            }


            boolean isCurrentTamper = SystemFlags.flagsToString(stateFlags).contains("Siren active");
            if (tamperStatus != isCurrentTamper) {
                tamperStatus = isCurrentTamper;
                isChanged = true;
                if (isCurrentTamper) {
                    tryRecordTamperAlert(lockStatusData);
                }
            }

            this.isRecorded = true;
        }


        if (isChanged) {
            this.save();

            //Update Settings Page
            EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);

            return true;
        }

        return false;
    }


    /*public boolean isTamperAlert() {
        return tamperStatus == LockAdV1.VLS_FLAG_ALARM_TIP;
    }*/


    public boolean saveName(String name) {
        this.lock_name = name;
        this.save();

        LinkaName.saveLinkaNameForMacAddress(getMACAddress(), lock_name);

        LinkaAPIServiceImpl.upsert_lock(
                AppMainActivity.getInstance(),
                this,
                new Callback<LinkaAPIServiceResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

                    }
                });

        return true;
    }

    public void saveLatLng(Double latitude, Double longitude)
    {
        this.latitude = latitude + "";
        this.longitude = longitude + "";
        this.save();
    }


    public boolean saveSettings() {
        this.save();

        if (!this.settings_auto_unlocking) {
            this.awaitsForAutoUnlocking = false;
            LogHelper.e("Started ", "Auto Unlocking! " + lock_name);
        }

        return true;
    }

    public boolean updateRSSI(boolean discoverable, int rssi) {

        boolean alreadyOutOfBounds = false;
        boolean justOutOfBounds = false;
        boolean justInBounds = false;

        if (!discoverable || rssi == 0) {
            rssi = -1000;
        }

        //Only autounlock if we have disconnected
        if ((rssi <= -100 && !isConnected)) {
            rssi = -1000;

            RSSIs.clear();

            // Only track out of bounds if the lock is locked
            // Prevents a scenario where the lock is unlocked
            // You leave bounds, come back, lock it
            // Then it immediately auto unlocks
            if (isLocked) {
                rssi_outOfBounds = true;
                if (this.settings_auto_unlocking) {
                    awaitsForAutoUnlocking = true;
                }
                LogHelper.i("Linka", "RSSI Out of Bounds");
            }
        }

        //If RSSI less than -100
        if (this.rssi <= AppDelegate.min_rssi_autooutofbound) {
            alreadyOutOfBounds = true;
        }

        //If it's already out of bounds, and RSSI > 100, then it's back in bounds
        if (alreadyOutOfBounds) {
            if (rssi > AppDelegate.min_rssi_autobackinbound) {
                justInBounds = true;
            }
        } else { //If it's currenty in bounds, and then we go out of bounds
            if (rssi <= AppDelegate.min_rssi_autooutofbound) {
                justOutOfBounds = true;
            }
        }

        LogHelper.d("Linka Update RSSI - Name: ", this.lock_name + ", RSSI: " + rssi);

        if (justInBounds) {
            LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isBackInRange);
        }

        if (justOutOfBounds) {
            LinkaActivity.saveLinkaActivity(this, LinkaActivity.LinkaActivityType.isOutOfRange);
        }

        this.rssi = rssi;
        this.save();



        /*
        Accumulate and deduce average RSSI over the last 3 trials
         */

        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(this);
        if (accessKey != null) {
            if (accessKey.isAdmin()) {
                accumulateAndDeduceAverageRSSI();
            }
        }

        return true;
    }




    //Sep 2017 - Changing autounlocking Architecture
    //Testing and statistics here: https://docs.google.com/spreadsheets/d/1PtnVTJFL3tsznObbE9rtCvSaUXV0dT0G3oXY1VJEfgE
    // Observations: We should only focus on the maximal RSSI value for a given # of seconds. All the noise is in the negative direction.
    // i.e. It is common to see an RSSI blip that is 20 points lower, but we will never see a blip that is 20 points higher. Therefore, we should filter out lower RSSI values, but keep the local maxima

    // We want the lock to start ot unlock right when they get 1 meter away from the bike. But we must be able to tolerate it unlocking up to 5 meters away, or letting the user wait up to 3 seconds before it unlocks. Even if it
    // unlocks 5 meters away, the user will be walking up and will arrive to the bike in 2-3 seconds. But we must avoid situations where it has already finished unlocking before the user has arrived, because they will think it was unlocked.

    // Now, autounlocking works in two scenarios:
    // 1) When we receive two packets out of seven that have RSSI > -60
    // 2) When we receive two packets out of five that have RSSI > -65. In the second scenario, we wait two seconds before unlocking, in case the user is still walking towards his bike, so it's unlocking when he gets to his bike.
    // Two things I noticed: Some phones have much stronger bluetooth antennas. Even standing 7-8 meters away.
    // Also, the way we hold the phone significantly affects the RSSI strength. When gripping it tightly near the top of the phone, it will reduce the RSSI by around 15-20 points
    // TODO: Need to make a notification that the lock was unlocked via autounlocking.
    void accumulateAndDeduceAverageRSSI() {

        Integer new_rssi = Integer.valueOf(this.rssi);
        RSSIs.add(new_rssi);

        while (RSSIs.size() > 7) {
            RSSIs.remove(0);
        }

        if (!this.awaitsForAutoUnlocking || !rssi_outOfBounds) {
            return;
        }

        int numHighRssiCandidates = 0;
        int numMediumRssiCandidate = 0;
        boolean shouldAutounlockNow = false;
        boolean shouldAutounlockAfterDelay = false;

        for (int i = 0; i < RSSIs.size(); i++) {
            Integer item = RSSIs.get(i);
            int _rssi = item.intValue();

            if (_rssi > -66){ //Check two that exceed -58 in the last 7 rssi values
                numHighRssiCandidates++;
            }
            if (_rssi > -73 && i >= 2){ //Check two that exceed -63 in the last 5 rssi values
                numMediumRssiCandidate++;
            }

            if(numHighRssiCandidates >= 2){
                shouldAutounlockNow = true;

            }else if (numMediumRssiCandidate >= 2){
                shouldAutounlockAfterDelay = true;
            }
        }


        if(!awaitsForAutoUnlocking){
            waitingUntilSettledtoAutoUnlock = false;
        }

        if(shouldAutounlockNow && awaitsForAutoUnlocking || waitingUntilSettledtoAutoUnlock){
            doAutounlock();

        } else if (shouldAutounlockAfterDelay && awaitsForAutoUnlocking){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    doAutounlock();
                }
            }, 2000); //Wait two seconds until the user can walk closer
        }

        /* AUTO UNLOCK END */
    }



    private void doAutounlock() {
        if(Prefs.getString(Constants.LINKA_ADDRESS_FOR_AUTO_UNLOCK,"").equals(lock_mac_address)) {
            PugNotification.with(AppDelegate.getInstance()).cancel(LinkaActivity.LinkaActivityType.isOutOfRange.getValue());
            if (isConnected && isLockSettled && isLocked && !isLocking && !isUnlocking) {
                LogHelper.e("AutoUnlock", "UnLock");
                LogHelper.e("RSSI", "Started Unlocking");

                LocksController.getInstance().getLockController().doUnlock();
                awaitsForAutoUnlocking = false;
                waitingUntilSettledtoAutoUnlock = false;
                rssi_outOfBounds = false;
            } else if (isConnected && (isUnlocking || isLocking)) {
                LogHelper.e("AutoUnlock", "set AwaitsForAutounlocking = false");
                awaitsForAutoUnlocking = false;
            } else if (isConnected && !isLockSettled) {
                waitingUntilSettledtoAutoUnlock = true;
            }
        }

    }


    public List<Integer> RSSIs = new ArrayList<>();
}

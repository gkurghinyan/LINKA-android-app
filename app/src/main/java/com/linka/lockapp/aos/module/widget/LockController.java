package com.linka.lockapp.aos.module.widget;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.linka.Lock.BLE.BluetoothLeQueuedService;
import com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes;
import com.linka.Lock.FirmwareAPI.Comms.LockAckNakPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockContextPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockEncV1;
import com.linka.Lock.FirmwareAPI.Comms.LockInfoPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;
import com.linka.Lock.FirmwareAPI.LINKA_BLE_Service;
import com.linka.Lock.FirmwareAPI.Types.AuthState;
import com.linka.Lock.FirmwareAPI.Types.LockState;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.helpers.GeofenceService;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.helpers.SleepNotificationService;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.pages.settings.RevocationControllerV2;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benedict on 6/2/17.
 */


//Lock Controller Class
//This is the main class for interacting with the lock - Receiving and sending data

public class LockController implements Serializable {

    public interface LockControllerPacketCallback
    {
        void onUpdateCounter();
        void onTimeout();
    }
    public LockController.LockControllerPacketCallback lockControllerPacketCallback;

    boolean is_ble_ready;
    boolean is_device_connecting;
    boolean is_device_disconnecting;

    boolean is_device_is_just_connected = false;
    boolean is_device_is_just_auth_none = false;
    boolean is_device_is_just_auth_paired = false;
    boolean is_device_is_just_auth_complete = false;
    boolean is_device_is_just_auth_complete_can_respond = false;
    boolean is_device_is_just_auth_complete_responded = false;
    int is_device_is_just_auth_none_count = 0;
    int is_device_is_just_auth_paired_count = 0;
    boolean is_device_is_just_auth_none_warning_shown = false;

    int hashCode = new Random().nextInt();

    //If this is true, then we will display a BLOD popup
    public boolean shouldDisplayBLODPopup = false;

    //boolean receivedStallDelay = false;
    public boolean hasReadPac = false;
    public boolean repeatConnectionUntilSuccessful = false;

    boolean isInitContext = false;

    byte lockState = LockState.LOCK_UNKNOWN_STATE;
    byte authState = AuthState.AUTH_NONE;
    BluetoothManager bluetoothManager;
    boolean canAdjustRssiSetting = false;
    private boolean generatingV2Keys = false;


    /* UPDATE RSSI */
    Handler updateRSSIHandler = new Handler();
    Runnable updateRSSIRunnable = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null && bluetoothManager != null && !getIsDeviceDisconnected(bluetoothManager))
            {
                bluetoothGatt.readRemoteRssi();
            }
        }
    };

    void startUpdateRSSIRunnable() {
        stopUpdateRSSIRunnable();
        updateRSSIHandler.postDelayed(updateRSSIRunnable, 1000);
    }

    void stopUpdateRSSIRunnable() {
        updateRSSIHandler.removeCallbacks(updateRSSIRunnable);
    }
        /* UPDATE RSSI END */

    Context context;
    Linka linka;
    LockGattUpdateReceiver lockGattUpdateReceiver;
    LockBLEGenericListener lockBLEGenericListener;
    LocksController.OnRefreshListener onRefreshListener;
    BluetoothGatt bluetoothGatt;
    LINKA_BLE_Service.BluetoothGattCharacteristicBundle bundle = null;
    BluetoothLeQueuedService.BluetoothGattQueuedActions actions = null;

    boolean isDeinitialized = false;
    boolean should_send_connected_notification = false;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            doDisconnectDevice();
        }
    };

    LockBLEServiceProxy lockBLEServiceProxy;
    public LockControllerBundle lockControllerBundle = new LockControllerBundle();
    public LockControllerSetEncryptionKeyLogic lockControllerSetEncryptionKeyLogic = null;


    //---------------------------------------------//
    //SECTION 1: METHODS TO CONNECT AND DISCONNECT LINKA
    //---------------------------------------------//


    //Do Connect Device
    //Call this function to connect to a LINKA
    //Note this function can be called at any time, whether the bluetooth scan has found a LINKA or not

    public void doConnectDevice() {

        if(linka == null ){return;}
        // We seem to be getting MULTIPLE connect attempts
        // Put a semaphore in place
        if (!getIsDeviceConnecting() && !linka.isConnected) {
            LogHelper.e("CONNECT", "Connecting to LINKA Device");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            //---------------------------------------------//
            //Unpair all LINKA bonds if there are any
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice _device : pairedDevices) {
                    if (_device.getAddress().equals(linka.lock_address)) {
                        if (_device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            if (getIsDeviceDisconnected(bluetoothManager, _device)) {
                                if (_device.getBondState() == BluetoothDevice.BOND_BONDED) {

                                    //Do device unpair
                                    lockBLEServiceProxy.unpairDevice(_device);
                                }

                                actions = new BluetoothLeQueuedService.BluetoothGattQueuedActions();
                                if (bluetoothManager != null) {
                                    handler.removeCallbacks(runnable);
                                    handler.postDelayed(runnable, 20000);
                                    is_device_connecting = true;
                                    bluetoothGatt = lockBLEServiceProxy.connect(linka.lock_address, null, actions);
                                }
                                return;
                            } else {
                                return;
                            }
                        }
                    }
                }
            } else {
                // Bluetooth likely isn't enabled, leave it alone for now
                // TODO: add popup for Bluetooth enable
                return;
            }

            //---------------------------------------------//
            //Start connection with LINKA

            if (!getIsDeviceDisconnected(bluetoothManager)) {

                actions = new BluetoothLeQueuedService.BluetoothGattQueuedActions();
                is_device_connecting = true;
                bluetoothGatt = lockBLEServiceProxy.connect(linka.lock_address, null, actions);
                return;
            } else {
                if (!getIsDeviceConnecting()) {
                    actions = new BluetoothLeQueuedService.BluetoothGattQueuedActions();
                    if (bluetoothManager != null) {
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 20000); //Stop connection attempt after 20 seconds
                        is_device_connecting = true;
                        bluetoothGatt = lockBLEServiceProxy.connect(linka.lock_address, null, actions);
                    }
                }
            }
        }
    }

    //Do Disconnect Device
    //Handles the bluetooth disconnection
    public void doDisconnectDevice() {
        LogHelper.e("== ACTIVATE == ", "DISCONNECT ... !");

        if(linka == null){return;}

        clearSettingsQueue();
        doDisconnectedState();

        if (lockBLEServiceProxy != null && getBluetoothGatt() != null) {
            lockBLEServiceProxy.disconnect(getBluetoothGatt());
            lockBLEServiceProxy.close(getBluetoothGatt());
            is_device_connecting = false;
            if (linka != null)
            {
                linka.updateFromStatusData(false, null);
            }
            if (onRefreshListener != null) {
                onRefreshListener.onRefresh(LockController.this);
            }
            handler.removeCallbacks(runnable);
        }

        //Do a final RSSI Check so that out-of-range notification can be shown
        linka.updateRSSI(false, -1000);
    }

    //Turns off all flags so that the state is disconnected
    public void doDisconnectedState(){

        is_device_connecting = false;
        linka.isLockSettled = false;
        is_device_disconnecting = false;
        //linka.updateFromStatusData(false, null); //This will be run in doDisconnectDevice() below, so no need to run twice

        //Immediatly set as disconnected - when we turn off bluetooth, this is how it becomes disconnected
        linka.isConnected = false;


    }


    //
//Only runs if bonding is required (if version 0.83 and earlier)
    public boolean doBond() {
        if (lockBLEServiceProxy != null && getBluetoothGatt() != null) {
            return lockBLEServiceProxy.doAction_Bond(getBluetoothGatt());
        }
        return false;
    }

    public void doUnbond() {
        LogHelper.e(" == WARNING == ", "do UNBOND .... !");
        lockBLEServiceProxy.doAction_Unbond(getBluetoothGatt());
    }

    public void doDeletePhoneBond(BluetoothDevice device) {
        lockBLEServiceProxy.unpairDevice(device);
    }
    public boolean doDeleteAllBonds(LockController.LockControllerPacketCallback callback) {
        boolean ret = lockBLEServiceProxy.doAction_deleteAllBonds(lockControllerBundle);
        if (ret)
        {
            lockControllerPacketCallback = callback;
            is_device_is_just_auth_complete_can_respond = false;
            tryRespondOnPacketSentCounterTimeoutSchedule();
        }
        return ret;
    }


    //---------------------------------------------//
    //SECTION 2: METHODS TO THAT CALL LINKA COMMANDS
    //---------------------------------------------//


    public void doSleep() {
        lockBLEServiceProxy.doAction_Sleep(lockControllerBundle);
    }

    public void doStop() {
        lockBLEServiceProxy.doAction_stop(lockControllerBundle);
    }

    public boolean doLock() {
        boolean isSuccess = lockBLEServiceProxy.doAction_Lock(lockControllerBundle);
        if(isSuccess && linka.settings_auto_unlocking){
            Intent intent = new Intent(AppDelegate.getInstance(), GeofenceService.class);
            intent.putExtra(GeofenceService.GEOFENCE_ACTION,GeofenceService.GEOFENCE_ADD_ACTION);
            AppDelegate.getInstance().startService(intent);
        }
        return isSuccess;
    }

    public boolean doUnlock() {
        if(linka.settings_auto_unlocking && Prefs.getString(Constants.LINKA_ADDRESS_FOR_AUTO_UNLOCK,"").equals("")){
            Intent intent = new Intent(AppDelegate.getInstance(),GeofenceService.class);
            intent.putExtra(GeofenceService.GEOFENCE_ACTION,GeofenceService.GEOFENCE_REMOVE_ACTION);
            AppDelegate.getInstance().startService(intent);
        }
//        PugNotification.with(AppDelegate.getInstance()).cancel(LinkaActivity.LinkaActivityType.isOutOfRange.getValue());
        SharedPreferences.Editor editor = Prefs.edit();
        editor.putString(Constants.LINKA_ADDRESS_FOR_AUTO_UNLOCK,"");
        editor.apply();
        return lockBLEServiceProxy.doAction_Unlock(lockControllerBundle);
    }

    public boolean doReadPAC(){
        return lockBLEServiceProxy.doAction_ReadPAC(lockControllerBundle);
    }

    public boolean doReadActuations() { return lockBLEServiceProxy.doAction_ReadActuations(lockControllerBundle); }

    public boolean doTune() { return lockBLEServiceProxy.doAction_tune(lockControllerBundle); }

    public boolean doSetPasscode(String passcode) {
        return lockBLEServiceProxy.doAction_SetPasscode(Integer.parseInt(passcode), lockControllerBundle);
    }

    public boolean doSetStall(int stall) {
        return lockBLEServiceProxy.doAction_SetStall(stall, lockControllerBundle);
    }

    public boolean doActionSiren(){
        return lockBLEServiceProxy.doAction_siren(lockControllerBundle);
    }

    public boolean doSetAudibility(boolean enabled) {

        boolean audible = enabled;
        boolean tamperAlert = linka.settings_tamper_siren;
        int value = 0;

        if (audible && tamperAlert) {
            value = 3;
        } else if (audible) {
            value = 2;
        } else if (tamperAlert) {
            value = 1;
        } else {
            value = 0;
        }

        boolean isOK = lockBLEServiceProxy.doAction_WriteSetting("LockController->doSetAudibility" + hashCode, LockSettingPacket.VLSO_SETTING_AUDIO, value, lockControllerBundle);
        return isOK;
    }

    public boolean doSetTamperAlert(boolean enabled) {
        boolean audible = linka.settings_audible_locking_unlocking;
        boolean tamperAlert = enabled;
        int value = 0;

        if (audible && tamperAlert) {
            value = 3;
        } else if (audible) {
            value = 2;
        } else if (tamperAlert) {
            value = 1;
        } else {
            value = 0;
        }

        boolean isOK = lockBLEServiceProxy.doAction_WriteSetting("LockController->doSetTamperAlert" + hashCode, LockSettingPacket.VLSO_SETTING_AUDIO, value, lockControllerBundle);
        return isOK;
    }

    //set alarm delay
    public boolean doAction_SetAlarmDelay(int sec){
        boolean isOK = lockBLEServiceProxy.doAction_SetAlarmDelay(sec, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Alarm Delay = " + Integer.toString(sec));
        return isOK;
    }

    //set alarm time
    public boolean doAction_SetAlarmTime(int sec) {
        boolean isOK = lockBLEServiceProxy.doAction_SetAlarmTime(sec, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Alarm Time = " + Integer.toString(sec));
        return isOK;
    }

    //set Bump Threshold
    public boolean doAction_SetBumpThreshold(int mg) {
        boolean isOK = lockBLEServiceProxy.doAction_SetBumpThreshold(mg, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Bump Threshold = " + Integer.toString(mg));
        return isOK;
    }

    //set Pulse Tap
    public boolean doAction_SetPulseTap(int mg) {
        boolean isOK = lockBLEServiceProxy.doAction_SetPulseTap(mg, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Pulse Tap = " + Integer.toString(mg));
        return isOK;
    }

    //set Jostle
    public boolean doAction_SetJostle(int ms) {
        boolean isOK = lockBLEServiceProxy.doAction_SetJostle(ms, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Set Jostle = " + Integer.toString(ms));
        return isOK;
    }

    //set roll
    public boolean doAction_SetRoll(int deg) {
        boolean isOK = lockBLEServiceProxy.doAction_SetRoll(deg, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Set Roll = " + Integer.toString(deg));
        return isOK;
    }

    //set tilt
    public boolean doAction_SetTilt(int deg) {
        boolean isOK = lockBLEServiceProxy.doAction_SetTilt(deg, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Set Tilt = " + Integer.toString(deg));
        return isOK;

    }

    //set accel
    public boolean doAction_SetAccelDataRate(int dataRate) {
        boolean isOK = lockBLEServiceProxy.doAction_SetTilt(dataRate, lockControllerBundle);
        LogHelper.e("TAMPER SETTINGS: ","Accel Datarate = " + Integer.toString(dataRate));
        return isOK;
    }

    //set unlock sleep time
    public boolean doAction_SetUnlockSleep(int sec) {
        boolean isOK = lockBLEServiceProxy.doAction_SetUnlockSleep(sec, lockControllerBundle);
        LogHelper.e("SLEEP SETTINGS: ","Unlock Sleep = " + Integer.toString(sec));
        return isOK;
    }

    //set lock sleep time
    public boolean doAction_SetLockSleep(int sec) {
        boolean isOK = lockBLEServiceProxy.doAction_SetLockSleep(sec, lockControllerBundle);
        LogHelper.e("SLEEP SETTINGS: ","Locked Sleep = " + Integer.toString(sec));
        return isOK;
    }

    public boolean doFwUpg() {
        return lockBLEServiceProxy.doAction_FwUpg(lockControllerBundle);
    }



    public boolean doTryWriteSettings(int settingIndex, int settingValue, LockController.LockControllerPacketCallback callback) {
        if (lockBLEServiceProxy != null)
        {
            LogHelper.e("doAction", "TryWriteSettings");
            boolean ret = lockBLEServiceProxy.doAction_WriteSetting("LockController->doTryWriteSettings", settingIndex, settingValue, lockControllerBundle);
            if (ret)
            {
                lockControllerPacketCallback = callback;
                is_device_is_just_auth_complete_can_respond = false;
                tryShortRespondOnPacketSentCounterTimeoutSchedule();
            }
            return ret;
        }
        return false;
    }

    public boolean doTryReadSettings(LockController.LockControllerPacketCallback callback) {
        if (lockBLEServiceProxy != null)
        {
            LogHelper.e("doAction", "TryReadSettings");
            boolean ret = lockBLEServiceProxy.doAction_ReadSetting("LockController->doTryReadSettings", LockSettingPacket.VLSO_SETTING_AUDIO, lockControllerBundle);
            if (ret)
            {
                lockControllerPacketCallback = callback;
                is_device_is_just_auth_complete_can_respond = false;
                tryShortRespondOnPacketSentCounterTimeoutSchedule();
            }
            return ret;
        }
        return false;
    }

//Do activate is called after connection to read 
    public void doActivate(){
        LogHelper.e("doAction", "READING SETTING" + hashCode);
        // Keep it simple, send a read command to the queue

        //Read stall delay first to set settings
        //Read PAC second to enter settled
        //Then read unlocked and locked sleep to start sleep timer
        //
        //IMPORTANT: The first setting read has to call doAction_readInitialSetting(),
        // and all subsequent settings call doAction_ReadSetting()

        //Read pac for telemetry and to see if we should ask to set pac
        lockBLEServiceProxy.doAction_readInitialSetting(LockSettingPacket.VLSO_SETTING_LOCK_PAC_CODE, lockControllerBundle);
        //Read STALL DELAY to see if new Settings Profiles have been set
        lockBLEServiceProxy.doAction_ReadSetting("LockController->doActivate", LockSettingPacket.VLSO_SET_STALL_DELAY_100MS, lockControllerBundle);
        lockBLEServiceProxy.doAction_ReadSetting("LockController->doActivate", LockSettingPacket.VLSO_SETTING_STALL_MA, lockControllerBundle);
        lockBLEServiceProxy.doAction_ReadSetting("LockController->doActivate", LockSettingPacket.VLSO_SETTING_UNLOCKED_SLEEP_S, lockControllerBundle);
        lockBLEServiceProxy.doAction_ReadSetting("LockController->doActivate", LockSettingPacket.VLSO_SETTING_LOCKED_SLEEP_S, lockControllerBundle);

        //Read audio to set into settings page
        lockBLEServiceProxy.doAction_ReadSetting("LockController->doActivate", LockSettingPacket.VLSO_SETTING_AUDIO, lockControllerBundle);

        // Useful for testing the queue by initiating multiple sequential reads
/*
            // Test read of Sleep Settings
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_UNLOCKED_SLEEP_S, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_LOCKED_SLEEP_S, lockControllerBundle);

            // Test read of Tamper Settings
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_ALARM_DELAY_S, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_ALARM_TIMEOUT_S, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_PULSE_TH_MG, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_JOSTLE_100MS, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_ROLL_ALRM_DEG, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_PITCH_ALRM_DEG, lockControllerBundle);
            lockBLEServiceProxy.doAction_ReadSetting(LockSettingPacket.VLSO_SETTING_ACCEL_DATARATE, lockControllerBundle);
*/
    }

    //DO DEFAULT SETTINGS
    //Resets LINKA to factory reset settings
    public boolean doDefaultSettings()
    {
        boolean ret = lockBLEServiceProxy.doAction_defaultSettings(lockControllerBundle);
        return ret;
    }

    public boolean doAction_SetEncryptionKey(byte[] keyToSet, LockControllerSetEncryptionKeyLogic.LockControllerSetEncryptionKeyCallback callback) {
        lockControllerSetEncryptionKeyLogic = new LockControllerSetEncryptionKeyLogic(lockControllerBundle, callback, lockBLEServiceProxy);
        return lockControllerSetEncryptionKeyLogic.doAction_SetEncryptionKey(keyToSet);
    }



    //---------------------------------------------//
    //SECTION 3: METHODS TO DETERMINE BLUETOOTH STATE
    //---------------------------------------------//



    public boolean getIsBLEReady() {
        return is_ble_ready;
    }

    public boolean getIsDeviceConnecting() {
        return is_device_connecting;
    }

    public boolean getIsDeviceDisconnecting() {
        return is_device_disconnecting;
    }

    public boolean getIsDeviceDisconnected() {
        if (this.bluetoothManager == null)
        {
            this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (this.bluetoothManager == null)
        {
            return false;
        }
        BluetoothGatt gatt = getBluetoothGatt();
        if (gatt != null) {
            return bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT) == 0;
        }
        return true;
    }


    public boolean getIsDeviceDisconnected(BluetoothManager bluetoothManager) {
        if (this.bluetoothManager == null)
        {
            this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (bluetoothManager == null)
        {
            bluetoothManager = this.bluetoothManager;
        }
        if (bluetoothManager == null)
        {
            return false;
        }
        BluetoothGatt gatt = getBluetoothGatt();
        if (gatt != null) {
            return bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT) == 0;
        }
        is_device_connecting = false;
        return true;
    }

    public boolean getIsDeviceDisconnected(BluetoothManager bluetoothManager, BluetoothDevice device) {
        if (this.bluetoothManager == null)
        {
            this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (bluetoothManager == null)
        {
            bluetoothManager = this.bluetoothManager;
        }
        if (bluetoothManager == null)
        {
            return false;
        }
        return bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == 0;
    }

    public Linka getLinka() {
        return linka;
    }

    BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
    LINKA_BLE_Service.BluetoothGattCharacteristicBundle getBluetoothGattBundle() {
        return bundle;
    }
    BluetoothLeQueuedService.BluetoothGattQueuedActions getBluetoothGattQueuedActions() {
        return actions;
    }

    public LockController(Context context, Linka linka, LocksController.OnRefreshListener onRefreshListener, LockBLEServiceProxy lockBLEServiceProxy) {
        this.context = context;
        this.linka = linka;
        this.onRefreshListener = onRefreshListener;
        this.lockBLEServiceProxy = lockBLEServiceProxy;
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void changeLinkaForThisLockController(Context context, Linka linka, LocksController.OnRefreshListener onRefreshListener, LockBLEServiceProxy lockBLEServiceProxy){
        this.context = context;
        this.linka = linka;
        this.onRefreshListener = onRefreshListener;
        this.lockBLEServiceProxy = lockBLEServiceProxy;
    }

    public void initialize(boolean autoconnect, final boolean _should_send_connected_notification) {
        if (lockBLEGenericListener == null)
        {
            lockBLEGenericListener = new LockBLEGenericListener() {
                @Override
                public void onGattUpdateConnected(LockGattUpdateReceiver lockGattUpdateReceiver) {

                    //Final check to make sure that it's connected
                    //Needed because of asynchronous behaviour (may clean up in future)
                    if(getIsDeviceDisconnected(bluetoothManager)){
                        return;
                    }

                    //Make sure bluetooth is indeed on
                    BluetoothAdapter bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(context);
                    if (bluetoothAdapter != null) {
                        if (!bluetoothAdapter.isEnabled()) {
                            return;
                        }
                    }

                    LogHelper.e("== CONNECT! ==", "CONNECTED ... ");
                    is_device_connecting = false;
                    is_device_is_just_connected = true;
                    should_send_connected_notification = false;
                    is_device_is_just_auth_none_count = 0;
                    is_device_is_just_auth_none = false;
                    linka.isConnected = true;
                    isInitContext = false;
                    repeatConnectionUntilSuccessful = false;
                    linka.updateFromStatusData(true, null);
                    onRefreshListener.onRefresh(LockController.this);
                    handler.removeCallbacks(runnable);
                    startUpdateRSSIRunnable();
                }

                @Override
                public void onGattUpdateDisconnected(LockGattUpdateReceiver lockGattUpdateReceiver, int status) {

                    //Final check to make sure it's disconnected
                    //Needed because of asynchronous behaviour (may clean up in future)
                    if(!getIsDeviceDisconnected(bluetoothManager)){
                        return;
                    }
                    LogHelper.e("== WARNING! ==", "DISCONNECTED ... ");


//                    doUnbond();

                    onRefreshListener.onRefresh(LockController.this);

                    bluetoothGatt = null;
                    bundle = null;
                    handler.removeCallbacks(runnable);
                    stopUpdateRSSIRunnable();
                    doDisconnectDevice(); // Force BTLE disconnection immediately

                    //Update Settings Page
                    EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);

                    //Send sleep notification if it is overdue to sleep
                    if(SleepNotificationService.getInstance().overdueSleep){
                        SleepNotificationService.getInstance().sendNotification();
                    }

                    //If the status is 22 or 133, it means that our connection was unsuccessful, so we must try to reconnect immediately
                    //Repeat this for 30 seconds, or until we get a successful connection
                    //LogHelper.e("CONNECT","status = " + status + ", repeatUntilSuccessful = " + repeatConnectionUntilSuccessful);
                    if(status == 22 || status == 133){
                        if(repeatConnectionUntilSuccessful) {
                            LogHelper.e("CONNECT", "Failure to connect, trying again");
                            doConnectDevice();
                        }

                        Handler stopConnectingHandler = new Handler();
                        stopConnectingHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                repeatConnectionUntilSuccessful = false;
                            }
                        }, 30000);

                    }


                }

                @Override
                public void onGattUpdateBonded(LockGattUpdateReceiver lockGattUpdateReceiver) {

                    LogHelper.e("SUCCESSFUL", "BONDED, SHOULD FETCH SETTINGS");
                }

                @Override
                public void onGattUpdateReadRemoteRSSI(LockGattUpdateReceiver lockGattUpdateReceiver, int rssi) {
                    if (isDeinitialized) {
                        return;
                    }
                    linka.updateRSSI(true, rssi);
                    startUpdateRSSIRunnable();
                }

                @Override
                public void onGattBadEncPkt(LockGattUpdateReceiver lockGattUpdateReceiver, String szLogText) {
                    //LogHelper.e("BADENCPKT", "" + szLogText);
                }

                @Override
                public void onGattUpdateDiscovered(LockGattUpdateReceiver lockGattUpdateReceiver) {
                    if (lockBLEServiceProxy != null && lockBLEServiceProxy.mLINKA_BLE_Service != null) {
                        bundle = lockBLEServiceProxy.mLINKA_BLE_Service.PopulateGattCharacteristics(getBluetoothGatt(), getBluetoothGattQueuedActions());
                        if (bundle != null)
                        {
                            if (lockBLEServiceProxy.mLINKA_BLE_Service.SearchAndReadCharacteristic(LINKAGattAttributes.UUID_VLSO_DATA_TX, getBluetoothGatt(), getBluetoothGattBundle(), getBluetoothGattQueuedActions()))
                            {
                                LogHelper.e("Lock Controller"," Updating Bundle for lock controller " + hashCode);
                                // VALID , CONNECTED!
                                lockControllerBundle.gatt = getBluetoothGatt();
                                lockControllerBundle.bundle = getBluetoothGattBundle();
                                lockControllerBundle.actions = getBluetoothGattQueuedActions();
                                lockControllerBundle.setLockMACAddress(linka.getMACAddress());
                            }
                            lockBLEServiceProxy.mLINKA_BLE_Service.SearchAndReadCharacteristic(LINKAGattAttributes.UUID_FIRMWARE_VER, getBluetoothGatt(), getBluetoothGattBundle(), getBluetoothGattQueuedActions());
                        }
                    }
                }

                @Override
                public void onGattUpdateStatusPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockStatusPacket lockStatusPacket) {

                    if (is_device_is_just_connected)
                    {
                        is_device_is_just_connected = false;
                        is_device_is_just_auth_none = false;
                        is_device_is_just_auth_paired = false;
                        is_device_is_just_auth_complete = false;
                    }


                    //On some phones, it remains connected even when bluetooth is turned off.
                    //We need to explicitly disconnect when we detect that bluetooth is turned off
                    BluetoothAdapter bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(context);
                    if (bluetoothAdapter != null) {
                        if (!bluetoothAdapter.isEnabled()) {
                            doDisconnectDevice();
                        }
                    }


                    lockState = lockStatusPacket.GetLockState().GetValue();
                    authState = lockStatusPacket.GetAuthState().GetValue();

                    if (authState == AuthState.AUTH_NONE)
                    {

                        //Make sure that we're not connected
                        if(linka.isConnected){
                            linka.isConnected = false;

                            //Update Settings Page
                            EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);

                        }

                        // auth none, try sending an activate packet to trigger Bonding
                        if (!is_device_is_just_auth_none)
                        {
                            is_device_is_just_auth_none = true;
                        }
                        else
                        {
                            LogHelper.e("WARNING!", "AUTH_NONE, count: " + is_device_is_just_auth_none_count);
                            is_device_is_just_auth_none_count += 1;
                            if (is_device_is_just_auth_none_count == 1)
                            {
                                if(getBluetoothGatt()!= null) {
                                    if (getBluetoothGatt().getDevice().getBondState() == BluetoothDevice.BOND_NONE && lockControllerBundle.bondingRequired) {
                                        doBond();
                                    }
                                }
                            }
                            if (is_device_is_just_auth_none_count >= 9)
                            {
                                is_device_is_just_auth_none = false;
                                is_device_is_just_auth_none_count = 0;

                                if (!is_device_is_just_auth_none_warning_shown)
                                {
//                                    is_device_is_just_auth_none_warning_shown = true;
//                                    new AlertDialog.Builder(context)
//                                            .setTitle("Warning")
//                                            .setMessage("Unable to pair with the LINKA Lock. Please go to Settings & ")
//                                    doDeleteAllBonds(new LockControllerPacketCallback() {
//                                        @Override
//                                        public void onUpdateCounter() {
//                                        }
//                                    });
                                    doUnbond();
                                }
                            }
                        }
                    }

                    if (authState == AuthState.AUTH_PAIRED)
                    {
                        // auth paired, try sending an activate packet to trigger Activation
                        if (!is_device_is_just_auth_paired)
                        {
                            is_device_is_just_auth_paired = true;
                            should_send_connected_notification = true;
                        }
                        else
                        {
                            is_device_is_just_auth_paired_count += 1;
                            if (is_device_is_just_auth_paired_count >= 5)
                            {
                                is_device_is_just_auth_paired_count = 0;
                                is_device_is_just_auth_paired = false;
                            }
                        }

                        is_device_is_just_auth_none_count = 0;
                    }

                    if (authState == AuthState.AUTH_COMPLETE)
                    {

                        // nothing to do
                        if (!is_device_is_just_auth_complete)
                        {
                            is_device_is_just_auth_complete = true;
                            is_device_is_just_auth_complete_responded = false;
                            is_device_is_just_auth_complete_can_respond = false;

                        }

                        is_device_is_just_auth_none_count = 0;
                        is_device_is_just_auth_paired_count = 0;

                    }



                    if (authState == AuthState.AUTH_PAIRED
                            || authState == AuthState.AUTH_COMPLETE)
                    {
                        if (lockState == LockState.LOCK_ERROR)
                        {
                            // STOP THE ERROR!
                            doStop();
                        }

                        // Below section for telemetry
                        else if (lockState == LockState.LOCK_UNLOCKING) {
                            canAdjustRssiSetting = true;
                        }else if (lockState == LockState.LOCK_UNLOCKED && canAdjustRssiSetting) {
                            doReadActuations(); // Read Actuations for Telemetry
                            canAdjustRssiSetting = false;

                            //PATCH FIX: Set RSSI to 100 so we can lock regardless of distance.
                            LogHelper.e("RSSI","100");
                            lockBLEServiceProxy.doAction_WriteSetting("LockController->doSetAudibility", LockSettingPacket.VLSO_SETTING_RSSI_UNLOCK_MIN, 100, lockControllerBundle);

                        }else if (lockState == LockState.LOCK_LOCKING) {
                            canAdjustRssiSetting = true;
                        }else if (lockState == LockState.LOCK_LOCKED && canAdjustRssiSetting) {
                            canAdjustRssiSetting = false;
                            //PATCH FIX: Set RSSI to 75 so we can unlock only within 1 meter.
                            LogHelper.e("RSSI","75");
                            lockBLEServiceProxy.doAction_WriteSetting("LockController->doSetAudibility", LockSettingPacket.VLSO_SETTING_RSSI_UNLOCK_MIN, 75, lockControllerBundle);

                        }
                    }


                    linka.updateFromStatusData(true, lockStatusPacket);
                    onRefreshListener.onRefresh(LockController.this);
                }

                @Override
                public void onGattUpdateSettingPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockSettingPacket lockSettingPacket) {

                    int index = lockSettingPacket.settingIndex();
                    int value = lockSettingPacket.value();

                    LogHelper.e("SETTINGS RECV", index + " - " + value);

                    boolean audible = false;
                    boolean tamperAlert = false;

                    // Lock is now settled, as this indicates doActivate() was called successfully
                    LogHelper.e("LockController" + hashCode, "Lock is now settled");
                    linka.isConnected = true;
                    linka.isLockSettled = true;
                    //receivedStallDelay = true; //No stall delay setting in 0.83

                    //Turn off BLOD Popup
                    AppBluetoothService.getInstance().dfuCompleteTimestamp = 0;

                    //Reset the blod popup. We have successfully connected, so the next time we encounter LinkaFu,
                    // we can again show the popup to ask if the lock is solid blue
                    //This code is needed because we decided that we can only show the solid blue popup once.
                    //After it is shown once, then we show the message to contact LINKA Support
                    if (Prefs.contains("numberTimesDetectLinkaFuBlod")){
                        SharedPreferences.Editor edit = Prefs.edit();
                        edit.remove("numberTimesDetectLinkaFuBlod").commit();
                    }

                    // Store the PAC and Actuations for Telemetry
                    if (index == LockSettingPacket.VLSO_SETTING_LOCK_PAC_CODE){
                        LogHelper.e("ON GATT SETTING", "Pac received:" + Integer.toString(value));


                        // Check if the PAC has been set
                        if (value == 0 || value == 1234) {
                            linka.pacIsSet = false;
                        }else{
                            linka.pacIsSet = true;
                        }

                        linka.pac = value;
                        linka.saveSettings();
                        hasReadPac = true;

                        //Update Settings Page
                        EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);
                    }

                    if (index == LockSettingPacket.VLSO_SETTING_LOCK_ACTUATIONS){

                        //When we do factory reset, we usually read actuations, and when it is successfully returned, then we can start
                        EventBus.getDefault().post(RevocationControllerV2.CAN_START_FACTORY_RESET);

                        linka.actuations = value;
                        linka.saveSettings();
                    }

                    if (index == LockSettingPacket.VLSO_SETTING_AUDIO) {
                        if (value == 3) {
                            audible = true;
                            tamperAlert = true;
                        } else if (value == 2) {
                            audible = true;
                        } else if (value == 1) {
                            tamperAlert = true;
                        }

                        linka.settings_audible_locking_unlocking = audible;
                        linka.settings_tamper_siren = tamperAlert;
                        linka.saveSettings();

                        //Update Settings Page
                        EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);
                    }

                    //alarm delay
                    if(index == LockSettingPacket.VLSO_SETTING_ALARM_DELAY_S){
                        linka.settings_alarm_delay = value;
                        linka.saveSettings();

                    }
                    //alarm time
                    if(index == LockSettingPacket.VLSO_SETTING_ALARM_DURATION_S){
                        linka.settings_alarm_time = value;
                        linka.saveSettings();

                    }
                    //setting bump
                    if(index == LockSettingPacket.VLSO_SETTING_BUMP_TH_MG){
                        linka.settings_bump_threshold = value;
                        linka.saveSettings();

                    }

                    //pulse tap
                    if(index == LockSettingPacket.VLSO_SETTING_PULSE_TH_MG){
                        linka.settings_pulse_tap = value;
                        linka.saveSettings();

                    }

                    //jostle
                    if(index == LockSettingPacket.VLSO_SETTING_JOSTLE_100MS){
                        linka.settings_jostle_ms = value;
                        linka.saveSettings();

                    }
                    //roll
                    if(index == LockSettingPacket.VLSO_SETTING_ROLL_ALRM_DEG){
                        linka.settings_roll_alrm_deg = value;
                        linka.saveSettings();
                    }

                    //tilt
                    if(index == LockSettingPacket.VLSO_SETTING_PITCH_ALRM_DEG){
                        linka.settings_pitch_alrm_deg = value;
                        linka.saveSettings();
                    }

                    //tilt
                    if(index == LockSettingPacket.VLSO_SETTING_ACCEL_DATARATE){
                        linka.settings_accel_datarate = value;
                        linka.saveSettings();
                    }
                    //unlock sleeping
                    if(index == LockSettingPacket.VLSO_SETTING_UNLOCKED_SLEEP_S){

                        linka.settingsSleepPerformance = value;

                        linka.saveSettings();
                    }

                    //lock sleeping
                    if(index == LockSettingPacket.VLSO_SETTING_LOCKED_SLEEP_S){
                        linka.settingsSleepPerformance = value;

                        //Start sleep timer locked sleep is read
                        SleepNotificationService.getInstance().restartTimer();

                        linka.saveSettings();
                    }

                    if(index == LockSettingPacket.VLSO_SETTING_STALL_MA){
                        if (value != 60 && lockControllerBundle.getFwVersionNumber().equals("1.5.9")) {

                            LockSettingsProfileManager.updateLockSettingsProfile(LockController.this, lockControllerBundle.getFwVersionNumber());

                            lockBLEServiceProxy.doAction_ReadSetting("LockController->onGattSettingPacketUpdated", LockSettingPacket.VLSO_SETTING_STALL_MA, lockControllerBundle);
                        }
                    }


                    //STALL Delay
                    if(index == LockSettingPacket.VLSO_SET_STALL_DELAY_100MS) {
                        //receivedStallDelay = true;
                        linka.settings_stall_delay = value;
                        linka.saveSettings();
                        LogHelper.e("ON GATT SETTING", "Stall Delay received:" + Integer.toString(value));
                        if (value != 0 && lockControllerBundle.getFwVersionNumber().equals("1.4.3")) { //30 is default firmware value
                            //Else, Stall Delay is probably 30, so need to update settings
                            LockSettingsProfileManager.updateLockSettingsProfile(LockController.this, lockControllerBundle.getFwVersionNumber());
                            //Then, read stall delay again
                            lockBLEServiceProxy.doAction_ReadSetting("LockController->onGattSettingPacketUpdated", LockSettingPacket.VLSO_SET_STALL_DELAY_100MS, lockControllerBundle);
                            //receivedStallDelay = false;
                        }

                        if(linka.updateAppSettingsProfile){
                            LockSettingsProfileManager.updateAppSettingsProfile(LockController.this);
                        }

                    } /*else if(!receivedStallDelay) { //If we've received a setting packet, but stall delay is still not received, then read stall delay again
                        //This code may run more than once if we have a queue of settings that are not read, but that's ok for now.
                        lockBLEServiceProxy.doAction_ReadSetting("LockController->onGattSettingPacketUpdated", LockSettingPacket.VLSO_SET_STALL_DELAY_100MS, lockControllerBundle);
                    }*/


                    onRefreshListener.onRefreshSettings(LockController.this);

                    //DEBUG Code to test settings - LOCK & STALL linka to activate
                    /*
                    if(debug%5 == 0) {
                        debug = 0;
                        debug ++;}
                    else{
                        switch (debug) {
                            case 1:
                                lockBLEServiceProxy.doAction_ReadSetting("LockController->doTryReadSettings", LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, lockControllerBundle);
                                LogHelper.e("TAMPER SETTING: Bump", Integer.toString(linka.settings_bump_threshold));
                                break;
                            case 2:
                                lockBLEServiceProxy.doAction_ReadSetting("LockController->doTryReadSettings", LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, lockControllerBundle);
                                LogHelper.e("TAMPER SETTING: JOSTLE", Integer.toString(linka.settings_jostle_ms));
                                break;
                            case 3:
                                lockBLEServiceProxy.doAction_ReadSetting("LockController->doTryReadSettings", LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, lockControllerBundle);
                                LogHelper.e("TAMPER SETTING: ROLL", Integer.toString(linka.settings_roll_alrm_deg));
                                break;
                            case 4:
                                lockBLEServiceProxy.doAction_ReadSetting("LockController->doTryReadSettings", LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, lockControllerBundle);
                                LogHelper.e("TAMPER SETTING: Alm Duration", Integer.toString(linka.settings_alarm_time));
                                break;
                        }
                        debug++;
                    }*/

                }

                @Override
                public void onGattUpdateContextPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockContextPacket lockContextPacket) {

                    // Support for v2 Lock
                    // The Master Keys have changed
                    // And Bonding is now optional
                    switch (lockContextPacket.getEncVer())
                    {
                        case LockContextPacket.ENCVER_1:
                            lockControllerBundle.isV2Lock = false;
                            lockControllerBundle.bondingRequired = true;
                            break;
                        case LockContextPacket.ENCVER_2:
                            lockControllerBundle.isV2Lock = true;
                            lockControllerBundle.bondingRequired = false;
                            break;
                        case LockContextPacket.ENCVER_2B:
                            lockControllerBundle.isV2Lock = true;
                            lockControllerBundle.bondingRequired = true;
                            break;
                        default:
                            lockControllerBundle.isV2Lock = false;
                            lockControllerBundle.bondingRequired = true;
                            break;
                    }

                    // Now that we know the Lock Version
                    // Set the admin_2 key
                    if (linka != null) {
                        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(linka);

                        String subKey = "";
                        LockEncV1.PRIV_LEVEL privLevel = LockEncV1.PRIV_LEVEL.PRIV_ADMIN ;

                        if (lockControllerBundle.isV2Lock) {
                            if (accessKey != null && !accessKey.v2_access_key_admin_2.equals("")) {
                                subKey = accessKey.v2_access_key_admin_2;
                                privLevel = LockEncV1.PRIV_LEVEL.PRIV_ADMIN;
                            } else if (accessKey != null && !accessKey.v2_access_key_user_2.equals("")) {
                                subKey = accessKey.v2_access_key_user_2;
                                privLevel = LockEncV1.PRIV_LEVEL.PRIV_USER;
                            } else if (accessKey != null && !generatingV2Keys) {
                                generatingV2Keys = true; // Prevent additional calls per packet
                                LogHelper.e("LockController", "V2 Keys Missing, Generating NOW");
                                // The keys are missing, user just updated firmware to 1.4.3 / V2 Lock
                                // Get them right now otherwise we will not be able to talk to the lock
                                // We can do this by leveraging the check_key_status_for_user API call
                                // As it already autogenerates V2 keys in the event they're missing on the server
                                LinkaAPIServiceImpl.check_key_status_for_user(
                                        context,
                                        linka.lock_mac_address,
                                        new Callback<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse>() {
                                            @Override
                                            public void onResponse(Call<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> call, Response<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> response) {
                                                // If the call was successful update the lock
                                                // The next Context Packet that comes through
                                                // should have the proper keys
                                                if (response != null && response.body() != null) {
                                                    if (response.body().data != null) {

                                                        LinkaAccessKey.createNewOrReplaceKey(
                                                                linka,
                                                                response.body().data.key,
                                                                null);
                                                    }
                                                }
                                                generatingV2Keys = false;
                                            }

                                            @Override
                                            public void onFailure(Call<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> call, Throwable t) {
                                                generatingV2Keys = false;
                                            }

                                        });
                            }
                        } else {
                            // V1 Lock
                            if (accessKey != null && !accessKey.access_key_admin_2.equals("")) {
                                subKey = accessKey.access_key_admin_2;
                                privLevel = LockEncV1.PRIV_LEVEL.PRIV_ADMIN;
                            } else if (accessKey != null && !accessKey.access_key_user_2.equals("")) {
                                subKey = accessKey.access_key_user_2;
                                privLevel = LockEncV1.PRIV_LEVEL.PRIV_USER;
                            }
                        }

                        if (!subKey.equals("")) {
                            lockControllerBundle.setSubkey(LockEncV1.dataWithHexString(subKey), 1, privLevel);
                        }
                    }

                    if (lockControllerBundle.setLockContextData(lockContextPacket))
                    {
                        is_device_is_just_auth_complete_can_respond = true;
                        LogHelper.i("ContextPacket " + hashCode, "Counter: " + lockContextPacket.getCounter() + ", Can Respond: " + (is_device_is_just_auth_complete_can_respond ? "true" : "false") + ", Responded: " + (is_device_is_just_auth_complete_responded ? "true" : "false"));
                        lockBLEServiceProxy.processEncryptionSettingsQueue(lockControllerBundle, "onGattUpdateContextPacketUpdated"); // Update packet counter and process queue
                    }

                    // If we just did a FW update, or this is a newly added lock
                    // Update FW/Version specific settings in case this lock is in factory settings mode
                    if (linka.updateLockSettingsProfile) {
                        LockSettingsProfileManager.updateLockSettingsProfile(LockController.this, lockControllerBundle.getFwVersionNumber());
                    }

                    onRefreshListener.onRefresh(LockController.this);

                    if (should_send_connected_notification && authState != AuthState.AUTH_NONE)
                    {

                        //Stop timer from sending sleep notification
                        SleepNotificationService.getInstance().stopTimer();

                        should_send_connected_notification = false;
                        //Now, read settings so that it can enter settled state:
                        doActivate();
                    }
                    else
                    {
                        tryRespondOnAuthComplete();
                        tryRespondOnPacketSentCounterUpdated();
                    }

                }

                @Override
                public void onGattUpdateInfoPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockInfoPacket lockInfoPacket) {

                }

                @Override
                public void onGattUpdateNak(LockGattUpdateReceiver lockGattUpdateReceiver, LockAckNakPacket nak) {

                    if (lockControllerBundle != null && lockControllerBundle.mLockContextData != null)
                    {
                        if (lockControllerBundle.mLockContextData.updateCounter(nak.getCounter()))
                        {
                            is_device_is_just_auth_complete_can_respond = true;
                            LogHelper.e("Nak", "Counter: " + nak.getCounter() + ", Can Respond: " + (is_device_is_just_auth_complete_can_respond ? "true" : "false") + ", Responded: " + (is_device_is_just_auth_complete_responded ? "true" : "false"));
                            lockBLEServiceProxy.processEncryptionSettingsQueue(lockControllerBundle, "onGattUpdateNak"); // Update packet counter and process queue

                        }

                        //If not administrator, they will receive a NAK packet, so enter settled state
                        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(linka);
                        if(!accessKey.isAdmin()){
                            LogHelper.e("CONNECT", "LOCK SETTLED");
                            linka.isLockSettled = true;
                        }
                    }

                    // For PRIV_USER access, we can't use the normal activation method
                    // But we DO get a Nak packet back, which is enough to know that we're settled
                    // Lock is now settled, as this indicates doActivate() was called successfully
                    // Now we can have user intereaction, i.e. PAC update/FW update
                    //LogHelper.e("LockController", "Lock is now settled");
                    //isLockSettled = true;
                    //linka.isConnected = true;
                    tryRespondOnPacketSentCounterUpdated();
                }

                @Override
                public void onGattUpdateAck(LockGattUpdateReceiver lockGattUpdateReceiver, LockAckNakPacket ack) {

                    LogHelper.e("Ack Received ", "Counter: " + ack.getCounter() + "Command = " + ack.getmLockCommand() + " Orig Command = " + ack.getAckNakedCommand());

                    // Update the counter value
                    if (lockControllerBundle != null && lockControllerBundle.mLockContextData != null)
                    {
                        if (lockControllerBundle.mLockContextData.updateCounter(ack.getCounter()))
                        {
                            is_device_is_just_auth_complete_can_respond = true;
                            LogHelper.e("Ack", "Counter: " + ack.getCounter() + ", Can Respond: " + (is_device_is_just_auth_complete_can_respond ? "true" : "false") + ", Responded: " + (is_device_is_just_auth_complete_responded ? "true" : "false"));
                            lockBLEServiceProxy.processEncryptionSettingsQueue(lockControllerBundle, "onGattUpdateAck"); // Update packet counter and process queue
                        }
                    }

                    // If we are awaiting a key operation, do it now
                    if (lockControllerSetEncryptionKeyLogic != null && lockControllerSetEncryptionKeyLogic.m_bPendingKeyOperation)
                    {
                        lockControllerSetEncryptionKeyLogic.tryToSetEncryptionKey(
                                lockControllerSetEncryptionKeyLogic.m_PendingKeyToSet,
                                lockControllerSetEncryptionKeyLogic.m_PendingSlotToSet,
                                LockEncV1.KEY_PART.UPPER
                        );
                    }
                    else if (lockControllerSetEncryptionKeyLogic != null)
                    {
                        lockControllerSetEncryptionKeyLogic.tryAction_SetEncryptionKeyRunCallback(true);
                    }

                    if (should_send_connected_notification)
                    {
                        should_send_connected_notification = false;
//                        doActivate();
                    }
                    else
                    {
                        tryRespondOnAuthComplete();
                        tryRespondOnPacketSentCounterUpdated();
                    }
                }

                @Override
                public void onGattUpdateFirmwareVersionInfo(LockGattUpdateReceiver lockGattUpdateReceiver, String szFirmwareVersion) {
                    lockControllerBundle.setFwVersion(szFirmwareVersion);
                    linka.fw_version = szFirmwareVersion;  // Tracking for telemetry
                    linka.saveSettings();
                    if (onRefreshListener != null)
                    {
                        onRefreshListener.onRefreshSettings(LockController.this);
                    }
                }

                @Override
                public void onGattUpdateFirmwareDebugInfo(LockGattUpdateReceiver lockGattUpdateReceiver, String szLogText) {
                }
            };
        }

        if (lockGattUpdateReceiver == null)
        {
            lockGattUpdateReceiver = new LockGattUpdateReceiver(context, lockBLEServiceProxy, lockBLEGenericListener, linka);
            lockGattUpdateReceiver.onResume();
        }else if(!lockGattUpdateReceiver.linka.lock_address.equals(linka.lock_address)){
            lockGattUpdateReceiver.updateLockGattUpdateReceiver(context, lockBLEServiceProxy, lockBLEGenericListener, linka);
            lockGattUpdateReceiver.onResume();
        }

        if (autoconnect) {
            Log.e("LockController", "DoConnectDevice");
            doConnectDevice();
        }
    }

    public void clearSettingsQueue(){
        lockBLEServiceProxy.clearEncryptedSettingsQueue();
    }

    void tryRespondOnAuthComplete()
    {
        if (is_device_is_just_auth_complete && !is_device_is_just_auth_complete_responded && is_device_is_just_auth_complete_can_respond)
        {
            is_device_is_just_auth_complete_responded = true;

//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        doTryReadSettings();
//                    }
//                }, 2000);
        }
    }


    void tryRespondOnPacketSentCounterUpdated()
    {
        if (lockControllerPacketCallback != null)
        {
            if (is_device_is_just_auth_complete && is_device_is_just_auth_complete_can_respond)
            {
                is_device_is_just_auth_complete_can_respond = false;
                lockControllerPacketCallback.onUpdateCounter();
                lockControllerPacketCallback = null;
                tryRespondOnPacketSentCounterTimeoutScheduleCancel();
            }
            else
            {
                is_device_is_just_auth_complete_can_respond = false;
            }
        }
    }


    void tryRespondOnPacketSentCounterTimeoutSchedule()
    {
        tryRespondOnPacketSentCounterTimeoutHandler.removeCallbacks(tryRespondOnPacketSentCounterTimeoutRunnable);
        tryRespondOnPacketSentCounterTimeoutHandler.postDelayed(tryRespondOnPacketSentCounterTimeoutRunnable, 6000);
    }

    void tryShortRespondOnPacketSentCounterTimeoutSchedule()
    {
        tryRespondOnPacketSentCounterTimeoutHandler.removeCallbacks(tryRespondOnPacketSentCounterTimeoutRunnable);
        tryRespondOnPacketSentCounterTimeoutHandler.postDelayed(tryRespondOnPacketSentCounterTimeoutRunnable, 1000);
    }

    void tryRespondOnPacketSentCounterTimeoutScheduleCancel()
    {
        tryRespondOnPacketSentCounterTimeoutHandler.removeCallbacks(tryRespondOnPacketSentCounterTimeoutRunnable);
    }


    Handler tryRespondOnPacketSentCounterTimeoutHandler = new Handler();
    Runnable tryRespondOnPacketSentCounterTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            is_device_is_just_auth_complete_can_respond = false;
            if (lockControllerPacketCallback != null)
            {
                lockControllerPacketCallback.onTimeout();
                lockControllerPacketCallback = null;
            }
        }
    };


    public void deinitialize() {
        if (lockGattUpdateReceiver != null) {
            lockGattUpdateReceiver.onPause();
        }
        if (lockBLEServiceProxy != null) {
            lockBLEServiceProxy.disconnect(getBluetoothGatt());
            lockBLEServiceProxy.close(getBluetoothGatt());
        }
        lockBLEGenericListener = null;
        lockGattUpdateReceiver = null;
        bluetoothGatt = null;

        stopUpdateRSSIRunnable();
        isDeinitialized = true;
    }
}



package com.linka.lockapp.aos.module.widget;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.linka.Lock.BLE.BluetoothLeQueuedService.BluetoothGattQueuedActions;
import com.linka.Lock.FirmwareAPI.Comms.LockCommand;
import com.linka.Lock.FirmwareAPI.Comms.LockEncV1;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.Lock.FirmwareAPI.LINKA_BLE_Service;
import com.linka.lockapp.aos.module.helpers.LogHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Vanson on 22/2/16.
 */
public class LockBLEServiceProxy {

    private final static String TAG = LockBLEServiceProxy.class.getSimpleName();

    public LINKA_BLE_Service mLINKA_BLE_Service;
    Context context;
    LockBLEServiceListener lockBLEServiceListener;
    boolean isCreated = false;
    boolean isEncryptionCounterValid = false; // semaphore for state of packet counter

    ArrayList<SettingValue> encryptedPacketQueue = new ArrayList<>();  // Queue for communicating to the lock

    public LockBLEServiceProxy(Context context, LockBLEServiceListener lockBLEServiceListener) {
        this.context = context;
        this.lockBLEServiceListener = lockBLEServiceListener;
    }

    public void onCreate() {
        if (isCreated) return;
        Intent gattServiceIntent = new Intent(context, LINKA_BLE_Service.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        isCreated = true;
    }

    public void onDestroy() {
        try {
            context.unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLINKA_BLE_Service = null;
        isCreated = false;
    }






    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mLINKA_BLE_Service = ((LINKA_BLE_Service.LocalBinder) service).getService();
            if (!mLINKA_BLE_Service.initialize()) {
                LogHelper.i(TAG, "Unable to initialize LINKA BLE Service");
                return;
            }

            LogHelper.i("mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");

            if (lockBLEServiceListener != null) {
                lockBLEServiceListener.onServiceConnected(componentName, service, LockBLEServiceProxy.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mLINKA_BLE_Service = null;

            if (lockBLEServiceListener != null) {
                lockBLEServiceListener.onServiceDisconnected(componentName, LockBLEServiceProxy.this);
            }
        }
    };




    boolean is_mLINKA_BLE_Service_Valid() {
        if (mLINKA_BLE_Service == null) {
            LogHelper.i(TAG, "mLINKA_BLE_Service not initialized");
            return false;
        }
        return true;
    }



    /* BASIC MOVES */

    public BluetoothGatt connect(String deviceAddress, BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
        if (mLINKA_BLE_Service == null) {
            return null;
        }
        Log.e("LockBLEServiceProxy", "DoConnectDevice->Initiate");
        LogHelper.i("LockBLEServiceProxy", "Connect... " + deviceAddress);
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        BluetoothGatt _gatt = mLINKA_BLE_Service.connect(deviceAddress, gatt, actions);

        return _gatt;
    }

    public void disconnect(BluetoothGatt gatt) {
        if (gatt != null && gatt.getDevice() != null) {
            LogHelper.i("LockBLEServiceProxy", "Disconnect... " + gatt.getDevice().getAddress());
            LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
            mLINKA_BLE_Service.disconnect(gatt);
        }
    }

    public void close(BluetoothGatt gatt) {
        if (gatt != null) {
            mLINKA_BLE_Service.close(gatt);
        }
    }


    public String getDeviceAddress(BluetoothGatt gatt) {
        return gatt != null && gatt.getDevice() != null ? gatt.getDevice().getAddress() : null;
    }

    // Actuations
    protected boolean doAction_ReadActuations(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_readActuations... ");
        return doAction_ReadSetting("LockBLEServiceProxy->doAction_ReadActuations", LockSettingPacket.VLSO_SETTING_LOCK_ACTUATIONS, bundle);
    }
    // TODO: Refactor this process
    // Likely will be redundant with bonding removal
    // Have to read a setting post bonding to initiate proper connection with lock
    public boolean doAction_readInitialSetting(int settingIndex, LockControllerBundle bundle){
        isEncryptionCounterValid = true;
        boolean returnVal = doAction_ReadSetting("LockBLEServiceProxy->doAction_readInitialSetting", settingIndex, bundle);
        return returnVal;
    }

    public boolean doAction_Bond(BluetoothGatt gatt) {
        if (gatt != null)
        {
            LogHelper.e("LockBLEServiceProxy", "doAction_Bond... ");
            LogHelper.e("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
            return mLINKA_BLE_Service.createBond(gatt);
        }
        return false;
    }

    public void doAction_Unbond(BluetoothGatt gatt) {
        if (gatt != null)
        {
            LogHelper.e("LockBLEServiceProxy", "doAction_Unbond... ");
            LogHelper.e("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
            unpairDevice(gatt.getDevice());
        }
    }


    public void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            LogHelper.e("unpairDevice", e.getMessage());
        }
    }

    /* LOCK SPECIFIC ACTIONS */

    public boolean doAction_WriteCommandPacket(String callingFunction, int lockCommand, int lockSubCommand, LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_WriteCommandPacket... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return queueEncryptedSettingsPacket(callingFunction, lockCommand, lockSubCommand, bundle, SettingValue.PACKET_TYPE.COMMAND);
    }

    public boolean doAction_WriteSetting(String callingFunction, int settingIndex, int settingValue, LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_WriteSetting... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return queueEncryptedSettingsPacket(callingFunction, settingIndex, settingValue, bundle, SettingValue.PACKET_TYPE.WRITE);
    }

    public boolean doAction_ReadSetting(String callingFunction, int settingIndex, LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_ReadSetting... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return queueEncryptedSettingsPacket(callingFunction, settingIndex, null, bundle, SettingValue.PACKET_TYPE.READ);
    }

    public void clearEncryptedSettingsQueue(){
        encryptedPacketQueue.clear();
    }

    // Solves an issue where multiple lock actions are called at once
    // Without waiting for the updated counter value, which causes a bad packet error
    // And in some cases disconnects the lock
    // We queue each incoming request, then send at the appropriate time once the packet is updated with ack/nak
    private Boolean queueEncryptedSettingsPacket(String callingFunction, Integer settingIndex, Integer settingValue, LockControllerBundle bundle, SettingValue.PACKET_TYPE packetType){

        switch (packetType) {

            case READ:
                LogHelper.e("LockBLEServiceProxy","[SETTINGS QUEUE][READ][" + settingIndex + "]->" + callingFunction);
                break;

            case WRITE:
                LogHelper.e("LockBLEServiceProxy","[SETTINGS QUEUE][WRITE][" + settingIndex + "][" + settingValue + "]->" + callingFunction);
                break;

            case COMMAND:
                LogHelper.e("LockBLEServiceProxy","[SETTINGS QUEUE][CMD][" + settingIndex + "][" + settingValue + "]->" + callingFunction);
                break;

        }

        SettingValue value = new SettingValue(settingIndex, settingValue, packetType);

        if(packetType == SettingValue.PACKET_TYPE.COMMAND){
            encryptedPacketQueue.add(0, value);
        }else {
            encryptedPacketQueue.add(value);
        }

        // If the counter is valid send process it immediately, otherwise wait
        if (isEncryptionCounterValid){
            processEncryptionSettingsQueue(bundle, "queueEncryptedSettingsPacket");
        }
        return true;
    }

    // Checks if there are any queued items to send to the lock
    // Once the counter is updated and valid send one
    public synchronized void processEncryptionSettingsQueue(LockControllerBundle bundle, String callingFunction) {
        isEncryptionCounterValid = false; // disable the queue while we're processing

//        LogHelper.i("[QUEUE]", callingFunction + " [Counter]" + Integer.toString(bundle.mLockContextData.getCounter()) + "Size = " + encryptedPacketQueue.size());  // Let's us know which callback is initiating the process

        // Let's first check to make sure there is an existing access key set
        // It's possible that upon first connecting to the lock we haven't set it yet
        if (bundle.mLockEnc.isKeySet() && encryptedPacketQueue.size() > 0) {
            byte[] encPkt = null;

            SettingValue value = encryptedPacketQueue.remove(0);

            // check to see if this is a read or write/command
            // since read omits settingValue
            switch (value.getPacketType()) {

                case READ:
                    LogHelper.i("LockBLEServiceProxy", "[QUEUE] Processing Read Packet");
                    if (value.getSettingIndex() != null) {
                        encPkt = bundle.mLockEnc.CreateEncryptedGetSettingPacket(
                                bundle.getMACAddressByte(),
                                value.getSettingIndex(),
                                bundle.mLockContextData);
                    }
                    break;

                case WRITE:
                    LogHelper.i("LockBLEServiceProxy", "[QUEUE] Processing Write Packet");
                    if (value.getSettingIndex() != null && value.getSettingValue() != null) {
                        encPkt = bundle.mLockEnc.CreateEncryptedSetSettingPacket(
                                bundle.getMACAddressByte(),
                                value.getSettingIndex(),
                                value.getSettingValue(),
                                bundle.mLockContextData);
                    }
                    break;

                case COMMAND:
                    LogHelper.i("LockBLEServiceProxy", "[QUEUE] Processing Write Packet");
                    if (value.getSettingIndex() != null && value.getSettingValue() != null) {
                        encPkt = bundle.mLockEnc.CreateEncryptedPacket(
                                bundle.getMACAddressByte(),
                                value.getSettingIndex(),
                                value.getSettingValue(),
                                bundle.mLockContextData);
                    }
                    break;

            }

            if (encPkt != null) {
//                LogHelper.i("Counter", new Integer(bundle.mLockContextData.getCounter()).toString());
                mLINKA_BLE_Service.WriteDataPacket(encPkt, bundle.gatt, bundle.bundle, bundle.actions);
            }
        } else {
            isEncryptionCounterValid = true;  // Queue was empty so ready to process
        }
    }


    public boolean doAction_Lock(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_Lock... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_Lock", LockCommand.VCMD_LOCK, 0, bundle);
    }

    public boolean doAction_Unlock(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_Unlock... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_Unlock", LockCommand.VCMD_UNLOCK, 0, bundle);
    }


    public boolean doAction_FwUpg(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_FwUpg... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_FwUpg", LockCommand.VCMD_FWUPG, 0, bundle);
    }

    public boolean doAction_Sleep(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_Sleep... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_Sleep", LockCommand.VCMD_SLEEP, 0, bundle);
    }

    // Personal Access Code
    protected boolean doAction_ReadPAC(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_readPAC... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_ReadSetting("LockBLEServiceProxy->doAction_ReadPAC", LockSettingPacket.VLSO_SETTING_LOCK_PAC_CODE, bundle);
    }

    public boolean doAction_SetPasscode(int passcode, LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetPasscode... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetPasscode", LockSettingPacket.VLSO_SETTING_LOCK_PAC_CODE, passcode, bundle);
    }

    public boolean doAction_defaultSettings(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_defaultSettings... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_defaultSettings", LockCommand.VCMD_DEFAULT_SETTINGS, 0, bundle);
    }

    public boolean doAction_deleteAllBonds(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_deleteAllBonds... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_deleteAllBonds", LockCommand.VCMD_FORGET_BONDS, 0, bundle);
    }

    public boolean doAction_stop(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_stop... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_stop", LockCommand.VCMD_HALT, 0, bundle);
    }

    public boolean doAction_siren(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_siren... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_siren", LockCommand.VCMD_ACTIVATE_SIREN, 3, bundle);
    }

    public boolean doAction_stop_siren(LockControllerBundle bundle){
        LogHelper.i("LockBLEServiceProxy", "doAction_stop_siren... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_stop_siren", LockCommand.VCMD_STOP_ALARM, 0, bundle);
    }


    public boolean doAction_activate(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_activate... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_ReadSetting("LockBLEServiceProxy->doAction_activate", LockSettingPacket.VLSO_SETTING_BL_ENB_FLAGS, bundle);
    }

    public boolean doAction_tune(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_tune... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteCommandPacket("LockBLEServiceProxy->doAction_tune", LockCommand.VCMD_PLAY_TUNE, 0, bundle);
    }

    public boolean doAction_SetStall(int stall, LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetStall... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetStall", LockSettingPacket.VLSO_SETTING_STALL_MA, stall, bundle);
    }



    public boolean doAction_testMode(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_testMode... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_testMode", LockSettingPacket.VLSO_SETTING_LOCK_MODE, LockSettingPacket.LOCK_MODE_TEST, bundle);
    }

    public boolean doAction_normalMode(LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_normalMode... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_normalMode", LockSettingPacket.VLSO_SETTING_LOCK_MODE, LockSettingPacket.LOCK_MODE_NORMAL, bundle);
    }

    //set alarm delay
    public boolean doAction_SetAlarmDelay(int sec,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetAlarmDelay... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetAlarmDelay", LockSettingPacket.VLSO_SETTING_ALARM_DELAY_S, sec, bundle);
    }

    //set alarm time
    public boolean doAction_SetAlarmTime(int sec,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetAlarmTime... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetAlarmTime", LockSettingPacket.VLSO_SETTING_ALARM_DURATION_S, sec, bundle);
    }

    //set Bump Threshold
    public boolean doAction_SetBumpThreshold(int mg,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetBumpThreshold... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetBumpThreshold", LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, mg, bundle);
    }

    //set Pulse Tap
    public boolean doAction_SetPulseTap(int mg,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetPulseTap... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetPulseTap", LockSettingPacket.VLSO_SETTING_PULSE_TH_MG, mg, bundle);
    }

    //set Jostle
    public boolean doAction_SetJostle(int ms,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetJostle... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetJostle", LockSettingPacket.VLSO_SETTING_JOSTLE_100MS, ms, bundle);
    }

    //set roll
    public boolean doAction_SetRoll(int deg,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetRoll... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetRoll", LockSettingPacket.VLSO_SETTING_ROLL_ALRM_DEG, deg, bundle);
    }

    //set tilt
    public boolean doAction_SetTilt(int deg,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetTilt... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetTilt", LockSettingPacket.VLSO_SETTING_PITCH_ALRM_DEG, deg, bundle);
    }

    //set accel
    public boolean doAction_SetAccelDataRate(int dataRate,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetAccelDataRate... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SeetAccelDataRate", LockSettingPacket.VLSO_SETTING_ACCEL_DATARATE, dataRate, bundle);
    }

    //set unlock sleep time
    public boolean doAction_SetUnlockSleep(int sec,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetUnlockSleep... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("LockBLEServiceProxy->doAction_SetUnlockSleep", LockSettingPacket.VLSO_SETTING_UNLOCKED_SLEEP_S, sec, bundle);
    }

    //set lock sleep time
    public boolean doAction_SetLockSleep(int sec,LockControllerBundle bundle) {
        LogHelper.i("LockBLEServiceProxy", "doAction_SetLockSleep... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("doAction_SetLockSleep", LockSettingPacket.VLSO_SETTING_LOCKED_SLEEP_S, sec, bundle);
    }

    public boolean doAction_SetQuickLock(int enable,LockControllerBundle bundle){
        LogHelper.i("LockBLEServiceProxy", "doAction_SetQuickLock... ");
        LogHelper.i("With mLINKA_BLE_Service", mLINKA_BLE_Service.toString() + " ");
        return doAction_WriteSetting("doAction_SetQuickLock", LockSettingPacket.VLSO_SETTING_ALLOW_UNCONN_LOCK, enable, bundle);
    }


    public boolean tryToSetEncryptionKey(byte[] keyToSet, LockEncV1.KEY_SLOT slotToSet, LockEncV1.KEY_PART part, LockControllerBundle bundle)
    {
        byte[] encPkt = bundle.mLockEnc.CreateEncryptedSetKeySettingPacket(
                bundle.getMACAddressByte(),
                slotToSet,
                keyToSet,
                part,
                bundle.mLockContextData
        );

        boolean ret = mLINKA_BLE_Service.WriteDataPacket(encPkt,
                bundle.gatt, bundle.bundle, bundle.actions);

        return ret;
    }


}
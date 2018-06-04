package com.linka.lockapp.aos.module.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes;
import com.linka.Lock.FirmwareAPI.Comms.LockAckNakPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockCommand;
import com.linka.Lock.FirmwareAPI.Comms.LockContextPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockDataPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockInfoPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;
import com.linka.Lock.FirmwareAPI.Debug.NrfUartService;
import com.linka.Lock.FirmwareAPI.LINKA_BLE_Service;
import com.linka.Lock.Utility.DebugHelper;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Vanson on 22/2/16.
 */
public class LockGattUpdateReceiver {

    private final static String TAG = LockGattUpdateReceiver.class.getSimpleName();
    public static final String GATT_UPDATE_RECEIVER_NOTIFY_DISCONNECTED = "[gatt_update_receiver_notify_disconnected]";

    Linka linka;
    Context context;
    LockBLEServiceProxy lockBLEServiceProxy;
    LockBLEGenericListener lockBLEGenericListener;

    public LockGattUpdateReceiver(Context context, LockBLEServiceProxy lockBLEServiceProxy, LockBLEGenericListener lockBLEGenericListener, Linka linka) {
        this.context = context;
        this.lockBLEServiceProxy = lockBLEServiceProxy;
        this.lockBLEGenericListener = lockBLEGenericListener;
        this.linka = linka;
    }

    public void updateLockGattUpdateReceiver(Context context, LockBLEServiceProxy lockBLEServiceProxy, LockBLEGenericListener lockBLEGenericListener, Linka linka) {
        this.context = context;
        this.lockBLEServiceProxy = lockBLEServiceProxy;
        this.lockBLEGenericListener = lockBLEGenericListener;
        this.linka = linka;
    }

    public void onResume() {
//        if (mGattUpdateReceiver != null) {
//            context.unregisterReceiver(mGattUpdateReceiver);
//            mGattUpdateReceiver = null;
//        }
        mGattUpdateReceiver = makeGattUpdateReceiver();
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void onPause() {
//        if (mGattUpdateReceiver != null) {
            context.unregisterReceiver(mGattUpdateReceiver);
//            mGattUpdateReceiver = null;
//        }
    }





    public String getVersionInfo ()
    {
        //String szAppVersion = BuildConfig.VERSION_NAME; // Supposed to work with Gradle
        String szAppVersion = "[Not found]";
        try {
            szAppVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return szAppVersion + " firmware " + m_szFirmwareVersion;
    }


    public String getDeviceAddress() {
        return linka.lock_address;
    }



    BroadcastReceiver mGattUpdateReceiver;
    LockStatusPacket mLockStatusData;
    LockSettingPacket mLockSettingData;
    LockContextPacket mLockContextData;
    LockInfoPacket mLockInfoData;
    String m_szFirmwareVersion = "";



    public LockStatusPacket getLockStatusData() {
        return mLockStatusData;
    }

    public LockSettingPacket getLockSettingData() {
        return mLockSettingData;
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.

    BroadcastReceiver makeGattUpdateReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final String action = intent.getAction();

                Bundle extras = intent.getExtras();
                String deviceAddress = extras != null ? extras.getString("deviceAddress", " ") : " ";

                if (LINKA_BLE_Service.ACTION_GATT_CONNECTED.equals(action)) {
                    if (!deviceAddress.equals(getDeviceAddress())) return;
                    if (lockBLEGenericListener != null) {
                        lockBLEGenericListener.onGattUpdateConnected(LockGattUpdateReceiver.this);
                    }
                } else if (LINKA_BLE_Service.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    LogHelper.e(TAG, "Bond state changed. Bonded!");
                    if (lockBLEGenericListener != null) {
                        lockBLEGenericListener.onGattUpdateBonded(LockGattUpdateReceiver.this);
                    }
                } else if (LINKA_BLE_Service.ACTION_GATT_DISCONNECTED.equals(action)) {
                    //LogHelper.e("CONNECT", "Send Disconnected");
                    if (!deviceAddress.equals(getDeviceAddress())) return;
                    int status = extras != null ? extras.getInt("status", 0) : 0; //Get the status the disconnected call
                    if (lockBLEGenericListener != null) {
                        lockBLEGenericListener.onGattUpdateDisconnected(LockGattUpdateReceiver.this, status);
                    }
                    EventBus.getDefault().post(GATT_UPDATE_RECEIVER_NOTIFY_DISCONNECTED);
                } else if (LINKA_BLE_Service.ACTION_REMOTE_RSSI_UPDATED.equals(action)) {
                    int rssi = extras != null ? extras.getInt("rssi", -1000) : -1000;
                    if (!deviceAddress.equals(getDeviceAddress())) return;
                    if (lockBLEGenericListener != null) {
                        lockBLEGenericListener.onGattUpdateReadRemoteRSSI(LockGattUpdateReceiver.this, rssi);
                    }
                } else if (LINKA_BLE_Service.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    if (!deviceAddress.equals(getDeviceAddress())) return;
                    if (lockBLEGenericListener != null) {
                        lockBLEGenericListener.onGattUpdateDiscovered(LockGattUpdateReceiver.this);
                    }

                } else if (LINKA_BLE_Service.ACTION_DATA_AVAILABLE.equals(action)) {
                    if (!deviceAddress.equals(getDeviceAddress())) return;
                    // TODO: how do we know which data this is? if (intent.)
                    // We don't -- add to BluetoothLeService class, stuff into intent as EXTRA_CHARACTERISTIC
                    // LogHelper.i (TAG, "DATA_AVAILABLE, " + intent.getStringArrayExtra(BluetoothLeService.EXTRA_CHARACTERISTIC));
                    // Track via queue
//                // Incoming data - handle here depending on what the characteristic is

                    if (intent.getStringExtra(LINKA_BLE_Service.EXTRA_CHARACTERISTIC) == null
                            && intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA) == null) {
                        return;
                    }

                    if (intent.getStringExtra(LINKA_BLE_Service.EXTRA_CHARACTERISTIC).equalsIgnoreCase(NrfUartService.NRF_UART_RX_CHAR_UUID)) {
                        // Todo: show in UI, this is debug info from the firmware
                        String szLogText = new String(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                        LogHelper.i(TAG, "Debug UART: " + szLogText);

                        if (lockBLEServiceProxy != null && lockBLEServiceProxy.mLINKA_BLE_Service != null) {
                            lockBLEServiceProxy.mLINKA_BLE_Service.TimestampedLogInfo(TAG, "UART" + szLogText);
                        }

                        if (szLogText.equals("Bad enc pkt."))
                        {
                            lockBLEGenericListener.onGattBadEncPkt(LockGattUpdateReceiver.this, szLogText);
                        }

                        else if (lockBLEGenericListener != null) {
                            lockBLEGenericListener.onGattUpdateFirmwareDebugInfo(LockGattUpdateReceiver.this, szLogText);
                        }

                    } else if (intent.getStringExtra(LINKA_BLE_Service.EXTRA_CHARACTERISTIC).equalsIgnoreCase(LINKAGattAttributes.VLSO_MAIN_DATA_TX)) {
                        if (!deviceAddress.equals(getDeviceAddress())) return;
                        // This is where we receive data packets (the UUID name is from the perspective of the firmware
                        LogHelper.i(TAG, "VLSO TX data: " + DebugHelper.dumpByteArray(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA)));
                        LockDataPacket packet = new LockDataPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                        LogHelper.i(TAG, packet.toString());
                        switch (packet.getCmdType().GetValue()) {
                            case LockCommand.VCMD_STATUS:
                                mLockStatusData = new LockStatusPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                                LogHelper.i(TAG, mLockStatusData.toString());

                                if (lockBLEGenericListener != null) {
                                    lockBLEGenericListener.onGattUpdateStatusPacketUpdated(LockGattUpdateReceiver.this, mLockStatusData);
                                }
                                break;
                            case LockCommand.VCMD_GET_SETTING:
                            case LockCommand.VCMD_SET_SETTING:
                                // A requested setting value is being returned by the lock
                                mLockSettingData = new LockSettingPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                                LogHelper.e(TAG, "Got setting packet from fw: " + mLockSettingData);

                                if (lockBLEGenericListener != null) {
                                    lockBLEGenericListener.onGattUpdateSettingPacketUpdated(LockGattUpdateReceiver.this, mLockSettingData);
                                }

                                /*
                                // Example how to use:
                                if (mLockSettingData.settingIndex() == LockSettingPacket.VLSO_SETTING_LOCK_ACTUATIONS) {
                                    String actuations = String.format("%d", mLockSettingData.value());
                                    LogHelper.i(TAG, "Actuations: " + actuations);

                                    if (lockBLEGenericListener != null) {
                                        lockBLEGenericListener.onGattUpdateSettingPacketActuationsUpdated(LockGattUpdateReceiver.this, mLockSettingData, actuations);
                                    }
                                }
                                */

                                break;
                            case LockCommand.VCMD_CONTEXT:
                                mLockContextData = new LockContextPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                                LogHelper.i(TAG, "Got context packet from fw: " + mLockContextData);

                                if (lockBLEGenericListener != null) {
                                    lockBLEGenericListener.onGattUpdateContextPacketUpdated(LockGattUpdateReceiver.this, mLockContextData);
                                }

                                break;
                            case LockCommand.VCMD_INFO:
                                mLockInfoData = new LockInfoPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                                LogHelper.i(TAG, "Got info packet from fw: " + mLockInfoData);

                                if (lockBLEGenericListener != null) {
                                    lockBLEGenericListener.onGattUpdateInfoPacketUpdated(LockGattUpdateReceiver.this, mLockInfoData);
                                }

                                break;
                            case LockCommand.VCMD_NAK:
                                LockAckNakPacket nak = new LockAckNakPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                                LogHelper.i(TAG, "Got NAK to command " + nak.toString());

                                if (lockBLEGenericListener != null) {
                                    lockBLEGenericListener.onGattUpdateNak(LockGattUpdateReceiver.this, nak);
                                }
                                break;
                            case LockCommand.VCMD_ACK:
                                LockAckNakPacket ack = new LockAckNakPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                                LogHelper.i(TAG, "Got ACK to command " + ack.toString());

                                if (lockBLEGenericListener != null) {
                                    lockBLEGenericListener.onGattUpdateAck(LockGattUpdateReceiver.this, ack);
                                }
                                break;
                            default:
                                LogHelper.i(TAG, "Unhandled packet of type " + packet.getCmdType().toString());
                                break;
                        }
                    } else if (intent.getStringExtra(LINKA_BLE_Service.EXTRA_CHARACTERISTIC).equalsIgnoreCase(LINKAGattAttributes.VLSO_MAIN_DATA_RX)) {
                        if (!deviceAddress.equals(getDeviceAddress())) return;
                        // Show the lock status
//                    mLockStatusData = new LockAdV1(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA), 2); // Data starts 2 bytes in to the data
//                    mLockStatus.setText(mLockStatusData.toString());
                        LogHelper.i(TAG, "VLSO RX data: " + DebugHelper.dumpByteArray(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA)));
                        LockDataPacket packet = new LockDataPacket(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                        LogHelper.i(TAG, packet.toString());
                    } else if (intent.getStringExtra(LINKA_BLE_Service.EXTRA_CHARACTERISTIC).equalsIgnoreCase(LINKAGattAttributes.FIRMWARE_VER_CHAR)) {
                        if (!deviceAddress.equals(getDeviceAddress())) return;
                        // This is the string firmware version info
                        m_szFirmwareVersion = new String(intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA));
                        LogHelper.i(TAG, "Firmware version: " + m_szFirmwareVersion);

                        if (lockBLEGenericListener != null) {
                            lockBLEGenericListener.onGattUpdateFirmwareVersionInfo(LockGattUpdateReceiver.this, m_szFirmwareVersion);
                        }
                    } else {
                        if (!deviceAddress.equals(getDeviceAddress())) return;
                        // For all other data, pass to fn that just shows the raw data
                        LogHelper.i(TAG, "Other data....." + intent.getByteArrayExtra(LINKA_BLE_Service.EXTRA_DATA).toString());

                    }
                }
            }
        };
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LINKA_BLE_Service.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LINKA_BLE_Service.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LINKA_BLE_Service.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(LINKA_BLE_Service.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(LINKA_BLE_Service.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(LINKA_BLE_Service.ACTION_REMOTE_RSSI_UPDATED);
        return intentFilter;
    }
}

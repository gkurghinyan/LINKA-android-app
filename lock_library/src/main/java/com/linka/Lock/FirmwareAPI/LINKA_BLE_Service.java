package com.linka.Lock.FirmwareAPI;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.linka.Lock.BLE.BluetoothLeQueuedService;
import com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes;
import com.linka.Lock.FirmwareAPI.Comms.LockCommand;
import com.linka.Lock.FirmwareAPI.Comms.LockDataPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.Lock.FirmwareAPI.Debug.NrfUartService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Loren-Admin on 8/27/2015.
 */
public class LINKA_BLE_Service extends BluetoothLeQueuedService {
    private final static String TAG = LINKA_BLE_Service.class.getSimpleName();

    private boolean m_bSetStatusIndication = false; // Have we successfully subscribed to indications?
    long m_dwTicksAtStart;




    /**
     * Write a simple command packet that has no additional data
     * @param cmd
     * @return
     */
    public boolean WriteLockCommandPacket (byte cmd, BluetoothGatt gatt, BluetoothGattCharacteristicBundle bundle, BluetoothGattQueuedActions actions)
    {
        // For now we always send a 16b packet; in the future we will pass additional data for some commands.
        byte placeholder_data[] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        return WriteLockPacket(cmd, placeholder_data, gatt, bundle, actions);
    }

    public boolean WriteLockPacket (byte cmd, byte[] data, BluetoothGatt gatt, BluetoothGattCharacteristicBundle bundle, BluetoothGattQueuedActions actions)
    {
        boolean ret;
        LockDataPacket pkt = new LockDataPacket(cmd, data);
        ret = WriteDataPacket(pkt.getData(), gatt, bundle, actions);
        TimestampedLogInfo(TAG, "TX " + pkt.toString());
        return ret;
    }

    public boolean WriteDataPacket (byte[] data, BluetoothGatt gatt, BluetoothGattCharacteristicBundle bundle, BluetoothGattQueuedActions actions)
    {
        boolean retval = false;
        //BluetoothGattCharacteristic characteristic = mLINKA_BLE_Service.getCharacteristicByUUID(LINKAGattAttributes.UUID_SCOOT_DATA_CHAR);
//        characteristic.setValue(Integer.parseInt(szText) * (int)scaling, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
//        characteristic.setValue(data);
//        retval = mLINKA_BLE_Service.writeCharacteristic(characteristic);   // TODO: queue this in LINKA_BLE_Service
/* 4.4.4 issue
        QueueCharacteristicRead(characteristic);    // Read back to make sure our write was successful.
        //QueueCharacteristicRead(characteristic);    // Read back to make sure our write was successful.
        SearchAndReadCharacteristic(LINKAGattAttributes.UUID_SCOOT_DATA_CHAR);  // Shouldn't need this, \todo resolve
    4.4.4 issue    */

        if (bundle == null) {
            Log.e("Warning:", "Device is disconnected, cannot write data packet");
            return false;
        }
        if (bundle.m_CharLockData != null) {
            bundle.m_CharLockData.setValue(data);
            retval = writeCharacteristic(bundle.m_CharLockData, gatt, actions);
            if (retval == false)
                Log.e(TAG, "Failed to write lock data packet to " + bundle.m_CharLockData.getUuid().toString());
            else {
//                Log.i(TAG, String.format("Wrote lock data packet of length %d, %s", data.length, DebugHelper.dumpByteArray(data)));
                if (data == null)
                {
                    Log.i(TAG, String.format("Wrote lock data packet"));
                }
                else
                {
                    Log.i(TAG, String.format("Wrote lock data packet of length %d", data.length));
                }
            }
        }
        else
        {
            Log.e(TAG, "Data characteristic null.");
        }
        return retval;
    }

    // \todo This needs to be reimplemented based on the newer command method of communications.
    public boolean WriteSetting (int settingIndex, int settingValue, BluetoothGatt gatt, BluetoothGattCharacteristicBundle bundle, BluetoothGattQueuedActions actions) {
        boolean retval = false;
        LockSettingPacket packet = new LockSettingPacket (LockCommand.VCMD_SET_SETTING, settingIndex, settingValue, 0);
        retval = WriteDataPacket(packet.getData(), gatt, bundle, actions);
        return retval;
    }

    public boolean ReadSetting (int settingIndex, BluetoothGatt gatt, BluetoothGattCharacteristicBundle bundle, BluetoothGattQueuedActions actions)
    {
        LockSettingPacket packet = new LockSettingPacket (LockCommand.VCMD_GET_SETTING, settingIndex, 0, 0);
        return WriteDataPacket(packet.getData(), gatt, bundle, actions);
    }

    public class LocalBinder extends Binder {
        public LINKA_BLE_Service getService() {
            return LINKA_BLE_Service.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public void TimestampedLogInfo(String tag, String message)
    {
        long dwElapsedTime = System.currentTimeMillis() - m_dwTicksAtStart;
        String szMessage = String.format("%.2f: %s%s", (dwElapsedTime / 1000.0), message, System.getProperty("line.separator"));
        //mLogInfo.append(szMessage);
        Log.d(tag, szMessage);

    }

    public ArrayList<ArrayList<BluetoothGattCharacteristic>> getGattCharacteristics (BluetoothGattCharacteristicBundle bundle) {
        return bundle.mGattCharacteristics;
    }

    /**
     * Create a list of the connected BLE device's GATT characteristics, and
     * subscribe to notifications for the proper communications characteristics.
     * \todo refactor to move to connect code, shouldn't need to be called explicitly.
     * @return
     */

    public static class BluetoothGattCharacteristicBundle {
        public ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        public BluetoothGattCharacteristic m_CharLockStatus = null;
        public BluetoothGattCharacteristic mNotifyCharacteristic = null;
        public BluetoothGattCharacteristic m_CharLockData = null;
        public BluetoothGattCharacteristic m_uartCharacteristic = null;
    }


    public BluetoothGattCharacteristicBundle PopulateGattCharacteristics (BluetoothGatt gatt, BluetoothGattQueuedActions actions)
    {
        BluetoothGattCharacteristicBundle bundle = new BluetoothGattCharacteristicBundle();

        boolean bFoundExpectedCharacteristics = false;
        String uuid = null;
        List<BluetoothGattService> gattServices = getSupportedGattServices(gatt);
        bundle.mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        if (gattServices == null)
        {
            Log.d(TAG, "gattServices is NULL");
            return null;
        }

        Log.d(TAG, "GattService Size " + gattServices.size());
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            //gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.d(TAG, "Characteristic: " + gattCharacteristic.getUuid());
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                gattCharacteristicGroupData.add(currentCharaData);

                // Look for particular characteristics of interest
                if (LINKAGattAttributes.UUID_VLSO_DATA_TX.equals((gattCharacteristic.getUuid())))
                {
                    bFoundExpectedCharacteristics = true;   // \todo: look for all characteristics
                    // Subscribe to notifications for this characteristic
                    if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)
                    {
                        bundle.m_CharLockStatus = gattCharacteristic;
                        bundle.mNotifyCharacteristic = gattCharacteristic; // TODO: remove, replaced with above var m_CharLockStatus
                        if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            m_bSetStatusIndication = setCharacteristicNotification(gattCharacteristic, true, gatt, actions);
                            if (!m_bSetStatusIndication) {
                                Log.w(TAG, "Failed to set indications for Lock data TX on first try.");
                            } else {
                                Log.i(TAG, "Set indications for Lock data TX on first try.");
                            }
                        }
                        else
                            Log.e(TAG, "Lock data TX does not support indications.");
                    }
                    else
                    {
                        Log.e(TAG, "Status does not support indications.");
                    }


//                    QueueCharacteristicRead(gattCharacteristic);
                    //mLINKA_BLE_Service.readCharacteristic(gattCharacteristic);

                }
                else if (LINKAGattAttributes.UUID_VLSO_DATA_RX.equals(gattCharacteristic.getUuid()))
                {
                    bundle.m_CharLockData = gattCharacteristic; // this is the char. that we write command packets to
                }
                else if (NrfUartService.UUID_NRF_UART_RX.equals(gattCharacteristic.getUuid()))
                {
                    //m_ButtonInfo.setEnabled(true);
		// Temporary fix for https://bugzilla.linkalock.com/bugzilla/show_bug.cgi?id=164
                /*    bundle.m_uartCharacteristic = gattCharacteristic;
                    // Subscribe to data indications from the (debug) "UART"
                    if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        if (!setCharacteristicNotification(gattCharacteristic, true, gatt, actions))
                            Log.d(TAG, "Set indications for UART Rx");
                        else
                            Log.e(TAG, "Failed to set indications for UART Rx");
                    }
                    else
                        Log.e(TAG, "UART Rx does not support notifications.");
                    */
                }
                else if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0 )
                {
                    // Read once to populate the value
//                    QueueCharacteristicRead(gattCharacteristic);
                    readCharacteristic(gattCharacteristic, gatt, actions);

                }
            }
            bundle.mGattCharacteristics.add(charas);
            //gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        if (bFoundExpectedCharacteristics) {
            return bundle;
        }
        return null;
    }

    public boolean SearchAndReadCharacteristic (UUID uuid, BluetoothGatt gatt, BluetoothGattCharacteristicBundle bundle, BluetoothGattQueuedActions actions)
    {
        BluetoothGattCharacteristic characteristic = null;
        boolean bFound = false;

        if (bundle.mGattCharacteristics != null) {

            for (int index = 0; index < bundle.mGattCharacteristics.size(); index++) {
                bundle.mGattCharacteristics.get(index).size();
                for (int subindex = 0; subindex < bundle.mGattCharacteristics.get(index).size(); subindex++) {
                    if (uuid.equals(bundle.mGattCharacteristics.get(index).get(subindex).getUuid())) {
                        // We found the characteristic
                        bFound = true;
                        characteristic = bundle.mGattCharacteristics.get(index).get(subindex);
                    }
                }
            }

            if (bFound) {
                Log.d(TAG, "Found characteristic...");

                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    readCharacteristic(characteristic, gatt, actions); // Queued in BluetoothLeQueuedService
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    Log.d(TAG, "Ignoring notify property...");
                }
            } else {
                Log.w(TAG, "Characteristic not found...");

            }

        }
        return bFound;
    }


}

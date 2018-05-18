package com.linka.Lock.BLE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes;
import com.linka.Lock.Utility.LockLogDBHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Loren-Admin on 4/29/2015.
 */
public class BluetoothLeQueuedService extends Service {
    private final static String TAG = BluetoothLeQueuedService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    protected BluetoothAdapter mBluetoothAdapter;
    private int mConnectionState = STATE_DISCONNECTED;

    public static String BLE_DFU_FW_CHARACTERISTIC = "00001530-1212-efde-1523-785feabcd123";

    public boolean allowReconnect = false;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_CHARACTERISTIC =
            "com.example.bluetooth.le.EXTRA_CHARACTERISTIC";
    public final static String ACTION_BOND_STATE_CHANGED =
            "android.bluetooth.device.action.BOND_STATE_CHANGED";
    public final static String ACTION_REMOTE_RSSI_UPDATED =
            "com.example.bluetooth.le.ACTION_REMOTE_RSSI_UPDATED";

    public static class BluetoothGattQueuedActions {
        public Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
        public Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();
        public Queue<BluetoothGattCharacteristic> characteristicWriteQueue = new LinkedList<BluetoothGattCharacteristic>();
    }


        private void handlePendingQueuedActions(BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
            if (gatt == null) return;
            if (actions.descriptorWriteQueue.size() > 0)
                gatt.writeDescriptor(actions.descriptorWriteQueue.poll());
            else if (actions.characteristicWriteQueue.size() > 0)
                gatt.writeCharacteristic(actions.characteristicWriteQueue.poll());
            else if (actions.characteristicReadQueue.size() > 0)
                gatt.readCharacteristic(actions.characteristicReadQueue.poll());
        }


        // Implements callback methods for GATT events that the app cares about.  For example,
        // connection change and services discovered.
        private class GenericBluetoothGattCallback extends BluetoothGattCallback {

            public BluetoothGattQueuedActions actions;

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
                } else {
                    Log.d(TAG, "Callback: Error writing GATT Descriptor: " + status);
                }

                handlePendingQueuedActions(gatt, actions);
            }

            //After doConnect() in LockController, this returns in 5 seconds (or less if found)
            //Status codes here: http://allmydroids.blogspot.hk/2015/06/android-ble-error-status-codes-explained.html
            //It returns if unsuccessful: 133 (not found), 22 (connection error) or if successful status 0 or 8 (successful disconnect)
            //These are very different, and should be treated differently
            //If get status 22 or 133, immediately send another connect attempt (do this for 30 seconds)
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction, status, gatt);
                    Log.i(TAG, "E/LK-Connected to GATT server. Status: " + status);
                    // Attempts to discover services after successful connection.
                    if (gatt != null) {
                        Log.i(TAG, "Attempting to start service discovery:" +
                                gatt.discoverServices());
                    } else {
                        // we have lost connection?
                        Log.e(TAG, "Null gatt...");
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i("E/LK-CONNECT", "Disconnected from GATT server. Status: " + status);

                    //send a broadcast so that we can disconnect
                    broadcastUpdate(intentAction, status, gatt);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    for(BluetoothGattService service : gatt.getServices()){
                        Log.e("E/LK", "LINKA Gatt Service UUID = " + service.getUuid().toString());

                        if(service.getUuid().toString().equals(BLE_DFU_FW_CHARACTERISTIC)){
                            Log.e("E/LK", "REFRESHING CHARACTERISTICS !!!!");
                            refreshDeviceCache(gatt);
                        }
                        for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                            //If we check that the service matches the firmware dfu mode service, then we need to refresh the cache.
                            Log.e("E/LK", "LINKA Gatt Characteristic UUID = " + characteristic.getUuid().toString());
                        }
                    }

                    Log.e("E/LK", "Discovered 0");
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, status, gatt);
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            /*
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }
            */

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt);
                } else {
                    Log.d(TAG, "onCharacteristicRead error: " + status);
                }

                //if(characteristicReadQueue.size() > 0)
                //    gatt.readCharacteristic(characteristicReadQueue.element());
                //if there is more to write, do it!
                handlePendingQueuedActions(gatt, actions);
            }


            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt);
                }

                //if there is more to write, do it!
                handlePendingQueuedActions(gatt, actions);

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt);

                //if there is more to write, do it!
                handlePendingQueuedActions(gatt, actions);

            }


            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_REMOTE_RSSI_UPDATED, gatt, rssi);
                }
            }
        };

        private void broadcastUpdate(final String action, int status, BluetoothGatt gatt) {
            final Intent intent = new Intent(action);
            intent.putExtra("deviceAddress", gatt.getDevice().getAddress());
            intent.putExtra("status", status);

            //Sends a connected/disconnected broadcast, will be received by LockGattUpdateReceiver
            sendBroadcast(intent);
        }

        protected void broadcastUpdate(final String action,
                                       final BluetoothGattCharacteristic characteristic,
                                       BluetoothGatt gatt) {
            final Intent intent = new Intent(action);
            intent.putExtra("deviceAddress", gatt.getDevice().getAddress());

            // This is special handling for the Heart Rate Measurement profile.  Data parsing is
            // carried out as per profile specifications:
            // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        /*
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else
         */
            {
                // For all other profiles, writes the raw data followed by the data formatted as a hex string.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    //final StringBuilder stringBuilder = new StringBuilder(data.length);
                    //for(byte byteChar : data)
                    //    stringBuilder.append(String.format("%02X ", byteChar));
                    //intent.putExtra(EXTRA_DATA, new String(data, 0, data.length, StandardCharsets.UTF_8) + "\n" + stringBuilder.toString());
                    intent.putExtra(EXTRA_DATA, data);
                    intent.putExtra(EXTRA_CHARACTERISTIC, characteristic.getUuid().toString());
                }
            }

            sendBroadcast(intent);

        }


        /**
         * Refreshes the device's cache, especially after a firmware update, the characteristics values may be wrong
         * Copied from DfuBaseService.java in DFU code
         */
        protected void refreshDeviceCache(final BluetoothGatt gatt) {
                try {
                    final Method refresh = gatt.getClass().getMethod("refresh");
                    if (refresh != null) {
                        final boolean success = (Boolean) refresh.invoke(gatt);
                    }
                } catch (Exception e) {
                }

                gatt.disconnect();
        }

    protected void broadcastUpdate(final String action,
                                   BluetoothGatt gatt,
                                   int rssi) {
        final Intent intent = new Intent(action);
        intent.putExtra("deviceAddress", gatt.getDevice().getAddress());
        intent.putExtra("rssi", rssi);
        sendBroadcast(intent);

    }

    /**
         * Initializes a reference to the local Bluetooth adapter.
         *
         * @return Return true if the initialization is successful.
         */
        public boolean initialize() {
            // For API level 18 and above, get a reference to BluetoothAdapter through
            // BluetoothManager.
            if (mBluetoothManager == null) {
                mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (mBluetoothManager == null) {
                    Log.e(TAG, "Unable to initialize BluetoothManager.");
                    return false;
                }
            }

            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }

            return true;
        }

        /**
         * Connects to the GATT server hosted on the Bluetooth LE device.
         *
         * Worth noting that we CANNOT use autoconnect, it's insanely slow:
         * Passing true to connectGatt() autoconnect argument requests a background connection, while passing false requests a direct connection.
         * BluetoothGatt#connect() always requests a background connection.
         * Background connection (according to Bluedroid sources from 4.4.2 AOSP) has scan interval of 1280ms and a window of 11.25ms.
         * This corresponds to about 0.9% duty cycle which explains why connections, when not scanning, can take a long time to complete.
         * Direct connection has interval of 60ms and window of 30ms so connections complete much faster.
         * Additionally there can only be one direct connection request pending at a time and it times out after 30 seconds.
         * onConnectionStateChange() gets called with state=2, status=133 to indicate this timeout.
         *
         * @param address The device address of the destination device.
         *
         * @return Return true if the connection is initiated successfully. The connection result
         *         is reported asynchronously through the
         *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
         *         callback.
         */
        public BluetoothGatt connect(final String address, BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
            if (mBluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return null;
            }

            // Previously connected device.  Try to reconnect.
            if (allowReconnect) {
                if (gatt != null && gatt.getDevice() != null
                        && gatt.getDevice().getAddress() != null
                        && address.equals(gatt.getDevice().getAddress())) {
                    Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                    if (gatt.connect()) {
                        mConnectionState = STATE_CONNECTING;
                        return gatt;
                    } else
                    {
                        Log.e(TAG, "Connect() failed.");
                        return null;
                    }
                }
            }

            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return null;
            }

            GenericBluetoothGattCallback gattCallback = new GenericBluetoothGattCallback();
            gattCallback.actions = actions;


            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null)
            {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice _device : pairedDevices)
                {
                    if (_device.getAddress().equals(device.getAddress()))
                    {
                        gatt = _device.connectGatt(this, false, gattCallback);
                        Log.e(TAG, "Trying to create a new connection. (BONDED)");
                        mConnectionState = STATE_CONNECTING;
                        return gatt;
                    }
                }
            }
            gatt = device.connectGatt(this, false, gattCallback);
            Log.e(TAG, "Trying to create a new connection. (UNBONDED)");
            mConnectionState = STATE_CONNECTING;
            return gatt;
        }

        /**
         * Disconnects an existing connection or cancel a pending connection. The disconnection result
         * is reported asynchronously through the
         * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
         * callback.
         */
        public void disconnect(BluetoothGatt gatt) {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            gatt.disconnect();
        }

        /**
         * After using a given BLE device, the app must call this method to ensure resources are
         * released properly.
         */
        public void close(BluetoothGatt gatt) {
            if (gatt == null) {
                return;
            }
            gatt.close();
            gatt = null;
        }

        /**
         * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
         * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
         * callback.
         *
         * @param characteristic The characteristic to read from.
         */
        public boolean readCharacteristicNoQueue(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }
            return gatt.readCharacteristic(characteristic);
        }


        public boolean readCharacteristic(String characteristicName, BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }
            BluetoothGattService s = gatt.getService(UUID.fromString(characteristicName));
            BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(characteristicName));
            return readCharacteristic(c, gatt, actions);
        }

        public boolean readCharacteristic (BluetoothGattCharacteristic c, BluetoothGatt gatt, BluetoothGattQueuedActions actions)
        {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }
            //put the characteristic into the read queue
            actions.characteristicReadQueue.add(c);
            //if there is only 1 item in the queue, then read it.  If more than 1, we handle asynchronously in the callback above
            //GIVE PRECEDENCE to descriptor writes.  They must all finish first.
            if((actions.characteristicReadQueue.size() == 1) && (actions.descriptorWriteQueue.size() == 0) && (actions.characteristicWriteQueue.size() == 0)) {
                // All other queues empty, go ahead and write
                return gatt.readCharacteristic(c);
            }
            else
            {
                return true;    // queued ok
            }

        }



    /*
            Wrapper to queue write characteristic value
         */
        public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }
            //put the characteristic into the read queue
            actions.characteristicWriteQueue.add(characteristic);
            if((actions.characteristicWriteQueue.size() == 1) && (actions.descriptorWriteQueue.size() == 0) && (actions.characteristicReadQueue.size() == 0)) {
                // All other queues empty, go ahead and write
                boolean retval = gatt.writeCharacteristic(characteristic);

                if (retval)
                    Log.i(TAG, String.format("Directly wrote to characteristic %s", characteristic.getUuid().toString()));
                else
                    Log.e(TAG, String.format("Failed to write to characteristic %s", characteristic.getUuid().toString()));


                return retval;
            }
            else
            {
                Log.i(TAG, String.format("Queued (%d deep) write to characteristic %s", actions.characteristicWriteQueue.size(), characteristic.getUuid().toString()));
                return true;    // queued ok
            }

        }

    /*
            Wrapper to queue write characteristic value
     */
    public boolean writeDescriptor(BluetoothGattDescriptor descriptor, BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
        if (mBluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        //put the characteristic into the read queue
        actions.descriptorWriteQueue.add(descriptor);
        if((actions.descriptorWriteQueue.size() == 1) && (actions.characteristicWriteQueue.size() == 0) && (actions.characteristicReadQueue.size() == 0)) {
            // All other queues empty, go ahead and write
            return gatt.writeDescriptor(descriptor);
        }
        else
        {
            return true;    // queued ok
        }

    }


    /**
     * Start the bonding process. Register for ACTION_BOND_STATE_CHANGED
     * to be notified of the result.
     * @return
     */
    public boolean createBond(BluetoothGatt gatt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return gatt.getDevice().createBond();
        }
        return true;
    }


        /**
         * Enables or disables notification on a give characteristic.
         *
         * @param characteristic Characteristic to act on.
         * @param enabled If true, enable notification.  False otherwise.
         *                @return false if failed
         */
        public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                     boolean enabled,
                                                     BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }
            gatt.setCharacteristicNotification(characteristic, enabled);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(LINKAGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            if (gatt.writeDescriptor(descriptor))
                if (writeDescriptor(descriptor, gatt, actions))    // Must call our queued version
                {
                    // Succeeded
                    Log.d(TAG, "Write descriptor succeeded for notification, characteristic " + characteristic.toString());
                    return true;
                }
                else
                {
                    // Failed
                    Log.e(TAG, "Write descriptor failed for notification, characteristic " + characteristic.toString());
                    return false;
                }
            }
            else
            {
                Log.e(TAG, "Null descriptor.");
                return false;   // Did not succeed
            }
        }

      /**
         * Enables or disables notification on a give characteristic.
         *
         * @param characteristic Characteristic to act on.
         * @param enabled If true, enable notification.  False otherwise.
         */
        public boolean setCharacteristicIndication(BluetoothGattCharacteristic characteristic,
                                                   boolean enabled,
                                                   BluetoothGatt gatt, BluetoothGattQueuedActions actions) {
            if (mBluetoothAdapter == null || gatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }


            if (gatt.setCharacteristicNotification(characteristic, enabled)) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString(LINKAGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); // Should not need this
                // Here we queue the descriptor write, otherwise will fail if another operation is underway.
                if (writeDescriptor(descriptor, gatt, actions)) {
                    // Succeeded
                    Log.d(TAG, "Write descriptor succeeded for indication, characteristic " + characteristic.toString());//characteristic.getStringValue(0));
                    return true;
                } else {
                    // Failed
                    Log.e(TAG, "Write descriptor failed for indication, characteristic " + characteristic.toString());
                    return false;
                }
            }
            else
            {
                Log.e(TAG, "Set char. notif. failed.");
                return false;
            }
        }


        /**
         * Retrieves a list of supported GATT services on the connected device. This should be
         * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
         *
         * @return A {@code List} of supported services.
         */
        public List<BluetoothGattService> getSupportedGattServices(BluetoothGatt gatt) {
            if (gatt == null) return null;

            return gatt.getServices();
        }

        private final String LIST_NAME = "NAME";
        private final String LIST_UUID = "UUID";
        public BluetoothGattCharacteristic getCharacteristicByUUID (UUID uuid, BluetoothGatt gatt) {
            List<BluetoothGattService> gattServices = gatt.getServices();

            for (BluetoothGattService gattService : gattServices) {
                ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    // Look for particular characteristics of interest
                    if (uuid.equals(gattCharacteristic.getUuid())) {
                        return gattCharacteristic;
                    }
                }
            }
            return null;
        }

// New stuff
/*
    public void writeGattDescriptor(BluetoothGattDescriptor d){
        //put the descriptor into the write queue
        descriptorWriteQueue.add(d);
        //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
        if(descriptorWriteQueue.size() == 1){
            gatt.writeDescriptor(d);
        }
    }
*/
    public class LocalBinder extends Binder {
        public BluetoothLeQueuedService getService() {
            return BluetoothLeQueuedService.this;
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

}

package com.linka.lockapp.aos.module.helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.linka.Lock.BLE.BluetoothLEDevice;
import com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes;
import com.linka.Lock.FirmwareAPI.Comms.LockAdV1;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;
import com.linka.Lock.Utility.LockLogDBHelper;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;

import java.util.List;

/**
 * Created by Vanson on 21/2/16.
 */
public class BLEHelpers {

    private static LockLogDBHelper mLockDataLogger;

    public static final int REQUEST_ENABLE_BT = 1;
    public static final long SCAN_PERIOD = 30000;


    public static void initialize(Context context) {
        mLockDataLogger = new LockLogDBHelper(context);
    }


    /* check if phone supports BLE */

    // 1. check for device support of BLE services
    public static BluetoothAdapter checkBLESupportForAdapter(Context context) {

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return null;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(context, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return null;
        }

        return bluetoothAdapter;
    }




    /* create device record */

    public static BluetoothLEDevice makeBluetoothLEDevice(BluetoothDevice device, int rssi, byte[] scanrecord, boolean bonded) {
        return new BluetoothLEDevice(device, rssi, scanrecord, bonded);
    }




    /* log device */

    public static void addLogEntry(String DeviceAddress, LockAdV1 pkt) {
        mLockDataLogger.addLogEntry(DeviceAddress, pkt);
    }

    public static void addLogEntry(String DeviceAddress, LockStatusPacket pkt) {
        mLockDataLogger.addLogEntry(DeviceAddress, pkt);
    }





    /* discover device */

    public static int upsertBluetoothLEDeviceList(List<BluetoothLEDevice> devices, List<Linka> linkaList, BluetoothDevice device, int rssi, byte[] scanrecord) {
        // Check to see if we already have a device with that address
        for (BluetoothLEDevice d : devices) {
            if (d.getAddress().equals(device.getAddress()))
            {
                // Update the values, and return
                LockAdV1 ad_info = d.updateAdvData(device, rssi, scanrecord);

                // If enabled, write to the log
                //
                if (ad_info != null) {
                    mLockDataLogger.addLogEntry(device.getAddress(), ad_info);
                }
                return 1;
            }
        }

        BluetoothLEDevice bluetoothLEDevice = makeBluetoothLEDevice(device, rssi, scanrecord, (device.getBondState() == BluetoothDevice.BOND_BONDED));

        if (isValidLinkaLock(bluetoothLEDevice)) {
            LockAdV1 ad_info = bluetoothLEDevice.getAdvData();

            if (ad_info != null) {
                mLockDataLogger.addLogEntry(device.getAddress(), ad_info);
            }

            devices.add(bluetoothLEDevice);
            linkaList.add(Linka.makeLinka(bluetoothLEDevice));

            return 0;
        }

        return 2;
    }






    /* Identify lock */

    public static boolean isValidLinkaLock(BluetoothLEDevice device) {
        if (device.has128BitUUID(LINKAGattAttributes.UUID_VLSO_MAIN_SVC)) {
            return true;
        }
        return false;
    }





}

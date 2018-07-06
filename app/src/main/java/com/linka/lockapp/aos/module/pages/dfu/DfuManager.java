package com.linka.lockapp.aos.module.pages.dfu;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.setup.SetupLinka1;
import com.linka.lockapp.aos.module.widget.LockController;
import com.pixplicity.easyprefs.library.Prefs;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * Created by Vanson on 16/8/2016.
 */
public class DfuManager {


    public static String BLE_DFU_FW_CHARACTERISTIC = "000015301212EFDE1523785FEABCD123";

    public interface DfuManagerBLEPowerStatusCallback
    {
        void onPowerOn();
        void onPowerOff();
    }

    public LockController lockController;
    public boolean isUploading = false; // Multiple callbacks are firing at once, causing DFU errors
    boolean hasReceivedScanCallback;
    Handler handler = new Handler();


    DfuManagerBLEPowerStatusCallback callback;

    public void startDetectingBLEPowerOn(Context context, DfuManagerBLEPowerStatusCallback callback)
    {
        this.callback = callback;
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
    }

    public void destroyDetectingBLEPowerOn(Context context)
    {
        this.callback = null;
        context.unregisterReceiver(mReceiver);
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (callback != null)
                        {
                            callback.onPowerOff();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (callback != null)
                        {
                            callback.onPowerOn();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:

                        break;
                }
            }
        }
    };


    Context context;

    public void startDfu(Context context, DfuProgressListener dfuProgressListener)
    {

        this.context = context;
        DfuServiceListenerHelper.registerProgressListener(context, dfuProgressListener);

/*
        // Delete the bond for the device
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice _device : pairedDevices) {
                if (_device.getAddress().equals(lockController.getLinka().lock_address)) {
                    if (_device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        LogHelper.e("DFU", "DELETING BOND FOR: " + _device.getAddress());
                        lockController.doDeletePhoneBond(_device);
                    }
                }
            }
        }
*/

        //Occasionally the scan will not callback, so
        //if we do not get a callback within 1 second, that means
        //it isn't returning, so we try again.
        //Important to keep scanning until we've received Linka Fu
        //packet, or else it might never detect Linka Fu
        hasReceivedScanCallback = false;

        //startScan(context);
        startUpload();
    }



    public void destroyDfu(Context context, DfuProgressListener dfuProgressListener)
    {
        this.context = null;
        DfuServiceListenerHelper.unregisterProgressListener(context, dfuProgressListener);
    }




    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mSelectedDevice;


    void startScan(Context context)
    {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
/*
        // For newer Android OS, use the newer scanner object
        // Otherwise use the deprecated scanner
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            final BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            initScanCallback();
            scanner.startScan(null, scanSettings, scanCallback);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!hasReceivedScanCallback){
                        LogHelper.e("DFU-SCAN","Haven't received callback, try again");
                        stopScan();
                        startScan(DfuManager.this.context);
                    }
                }
            }, 1000);


            //If we haven't received a callback after 20 seconds, then we need to restart the process
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!hasReceivedScanCallback){

                        //Set this flag to true so it stops scanning
                        hasReceivedScanCallback = true;
                        LogHelper.e("DFU-SCAN","Haven't received callback, notify to try again");
                        stopScan();
                        sendErrorAndClose();

                    }
                }
            }, 20000);


        } else {*/
            if (bluetoothAdapter == null) { LogHelper.e("DFU","bluetoothAdapter NULL!"); return; }
            if (!bluetoothAdapter.isEnabled()) { LogHelper.e("DFU","bluetoothAdapter NOT ENABLED!"); return; }

            bluetoothAdapter.startLeScan(scanCallbackDeprecated);

            //If we haven't received a callback after 20 seconds, then we need to restart the process
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!hasReceivedScanCallback){

                        //Set this flag to true so it stops scanning
                        hasReceivedScanCallback = true;
                        LogHelper.e("DFU-SCAN","Haven't received callback, notify to try again");
                        stopScan();
                        sendErrorAndClose();
                    }
                }
            }, 20000);
        //}
    }

    //Sends an error message to the user
    //Pops the fragment
    void sendErrorAndClose() {

        //Check the # of times that the user has attempted and failed the firmware update
        //Save the # of times we've failed into User Preferences
        int times_failed;
        if (!Prefs.contains("times-failed-fw")){
            LogHelper.e("First failure","create times-failed-fw");
            times_failed = 1;
        }
        else {
            times_failed = Prefs.getInt("times-failed-fw", 0);
            times_failed += 1;
            LogHelper.e("Firmware update " + "Failure #" , Integer.toString(times_failed));
        }

        SharedPreferences.Editor edit = Prefs.edit();

        //The first 2 times we fail, we should tell them to try again
        if(times_failed <=2) {
            new android.app.AlertDialog.Builder(DfuManager.this.context)
                    .setTitle(R.string.oops)
                    .setMessage(R.string.try_firmware_again)
                    .setNegativeButton(R.string.ok, null)
                    .show();


        }
        //If it's the 3rd time failing, then tell them to restart their phone
        else if(times_failed <= 3){
            new android.app.AlertDialog.Builder(DfuManager.this.context)
                    .setTitle(R.string.uhoh)
                    .setCancelable(false)
                    .setMessage(R.string.fw_restart_phone)
                    .setNegativeButton(R.string.ok, null)
                    .show();


        }
        //If it's the 4th or more time they fail, then display a different message
        //The message tells them to contact LINKA support
        else{
            new android.app.AlertDialog.Builder(DfuManager.this.context)
                    .setTitle(R.string.uhoh)
                    .setCancelable(false)
                    .setMessage(R.string.fw_contact_linka_support)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.send_email, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Helpers.sendEmailWithText(AppMainActivity.getInstance(), _.i(R.string.email_support_url), _.i(R.string.linka_support), "LINKA Firmware Update Not Working", "");
                                } })
                    .show();

        }
        //Count how many times we've failed
        edit.putInt("times-failed-fw", times_failed);
        edit.commit();


        AppBluetoothService.getInstance().dfu = false;
        AppBluetoothService.getInstance().enableFixedTimeScanning(true);

        // Reset the view controller
        if(Linka.getLinkas().size() == 0){
            SetupLinka1 fragment = SetupLinka1.newInstance();
            AppMainActivity.getInstance().pushFragment(fragment);
        }else {
            AppMainActivity.getInstance().setFragment(MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(),MainTabBarPageFragment.LOCK_SCREEN));
        }
    }

    void stopScan()
    {
        /*
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (bluetoothAdapter != null) {
                BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                LogHelper.i("DFU-SCAN", "STOPPING SCAN on thread " + java.lang.Thread.currentThread().getId());
                scanner.stopScan(scanCallback);
                scanner = null;
            }
        } else {*/
        LogHelper.e("DFU SCAN", "Stopping scan");
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(scanCallbackDeprecated);
            }
        //}
    }

    ScanCallback scanCallback;

    // Wrapper class to initialize callback
    // If API > 21
    private void initScanCallback() {

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (scanCallback == null) {
                scanCallback = new ScanCallback() {
                    @TargetApi(21)
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);

                        LogHelper.e("DFU-SCAN", "" + result.getDevice().getName() + " " + result.getDevice().getAddress() + " on thread " + java.lang.Thread.currentThread().getId());
                        if (result.getDevice().getName() != null) {
                            if (!isUploading && result.getDevice().getName().equals("LinkaFu")) {

                                //We've received a callback, and we can start!
                                hasReceivedScanCallback = true;

                                //if (!isUploading && result.getDevice().getAddress().equals(lockController.getLinka().getMACAddress())) {
                                LogHelper.e("DFU-SCAN", "Discovered Matching Device: " + result.getDevice().getName() + " " + result.getDevice().getAddress());
                                stopScan();
                                mSelectedDevice = result.getDevice();
                                try {
                                    removeBond(result.getDevice());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                isUploading = true;
                                startUpload();
                                return;
                            } else if (isUploading) {
                                // Shut down extra callbacks that are running
                                stopScan();

                            }
                        }
                    }
                };
            }
        }
    }

    BluetoothAdapter.LeScanCallback scanCallbackDeprecated = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogHelper.e("DFU-SCAN", "LeScanCallback...");
            LogHelper.e("DFU-SCAN",device.getName() + " " + device.getAddress());
            HashMap<Integer, String> record = AdParser.ParseRecord(scanRecord);
            Set<Integer> keys = record.keySet();

            for (Integer key : keys)
            {
                String rec = record.get(key);
                    if (!isUploading && rec.equals(BLE_DFU_FW_CHARACTERISTIC))
                {
                    hasReceivedScanCallback = true;

                    LogHelper.e("DFU", "Discovered Device for DFU: " + device.getAddress());
                    stopScan();
                    mSelectedDevice = device;
                    try {
                        removeBond(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isUploading = true;
                    startUpload();
                    return;
                }
            }

        }
    };

    public boolean removeBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }







    DfuServiceInitiator starter;

    void startUpload()
    {
        if (context != null)
        {
            starter = new DfuServiceInitiator(lockController.getLinka().getMACAddress())
                    .setDeviceName("LinkaFu")
                    .setKeepBond(false)
                    .setDisableNotification(true)
                    .setPacketsReceiptNotificationsEnabled(true)  // Used to slow down transfer speed for compatibility
                    .setPacketsReceiptNotificationsValue(6);
            starter.setBinOrHex();

            starter.start(context, DfuService.class);
        }
    }


    public void tryPause(Context context)
    {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_PAUSE);
        manager.sendBroadcast(pauseAction);
    }

    public void tryAbort(Context context)
    {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
        manager.sendBroadcast(pauseAction);
    }

    public void tryResume(Context context)
    {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_RESUME);
        manager.sendBroadcast(pauseAction);
    }
}

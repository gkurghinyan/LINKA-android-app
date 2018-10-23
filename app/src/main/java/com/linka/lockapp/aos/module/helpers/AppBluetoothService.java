package com.linka.lockapp.aos.module.helpers;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Vanson on 8/4/16.
 */
public class AppBluetoothService extends Service {
    private final static String TAG = "BluetoothGatt";
    private static Timer timer = new Timer();
    private BluetoothDevice device;
    public boolean is_active = true;
    //TODO set this value to true
    public boolean is_user_selected_device_to_connect =  false;
    public boolean dfu = false;  // semaphor to stop connections during DFU mode

    public int connectOneOutofTen = 0;


    //Function that checks for BLOD for 1 minute after DFU is finished
    //We want to check for LINKA FU starting from 1 minute after DFU is sucessful until 2 minutes after DFU has finished
    //If we detect LINKA FU, then popup a warning
    //If we want to disable the warning, we can set this timestamp to some time far in the past
    public long dfuCompleteTimestamp;
    public long firstDetectLinkaFuTimestamp;

    BluetoothManager bluetoothManager;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public AppBluetoothService() {

    }

    public AppBluetoothService(Context context) {
        initialize();
        startTimer();
        LogHelper.e("AppBluetoothService", "Create");
        instance = this;

        if (context != null) {
            this.context = context;
        }
        if (getContext() != null) {
            if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { return; }
            bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        }
        LocksController.init(context);
        LocksController.getInstance().refresh();
        scanLeDevice(true);
    }



    Context context;



    public static AppBluetoothService instance;
    public static AppBluetoothService init(Context context) {
        if (instance == null) {
            instance = new AppBluetoothService(context);
        }
        return instance;
    }

    public static AppBluetoothService getInstance() {
        return instance;
    }


    //When starting service, dont stop when it is killed
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //sendBroadcast(new Intent("restartAppBluetooth"));
    }


    public boolean initialize() {
        return true;

    }


    public Context getContext() {
        return context;
    }


  /*  private void startTimer()
    {
        //Run scan every 2 seconds, even when app is in background
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //LogHelper.e("BLE Service", "shouldAllowScanning: " + shouldAllowFixedTimeScanning());
                if (shouldAllowFixedTimeScanning()) {

                    if (getContext() != null) {
                        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { return; }
                        bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                        if (bluetoothManager != null) {
                            bluetoothAdapter = bluetoothManager.getAdapter();
                        }
                    }
                    Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
                    LockController targetLockController;
                    if (targetLinka != null) {
                        targetLockController = LocksController.getInstance().getLockController(); //Get the Latest LINKA (the linka that is displayed)
                        if(targetLockController != null && !targetLockController.getIsDeviceConnected(bluetoothManager)){
                            Log.w("BluetoothGatt","call doConnectDevice in AppBluetooth Service line 168");
                            targetLockController.doConnectDevice();
                        }
                    }

                    if (bluetoothAdapter == null) { return; }
                    if (!bluetoothAdapter.isEnabled()) { return; }

                   *//* if (android.os.Build.VERSION.SDK_INT >= 21) {
                        //LogHelper.e("AppBluetoothService","API VERSION >= 21, Using Scanner");
                        final BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //Use low power mode of bluetooth scanning
                                .build();
                        initScanCallback();

                        *//**//*---------------------------------------------------*//**//*
                        //Get LINKA's mac address, and filter scan accordingly
                        //i.e. only return linkas from bluetooth scan
                        List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();

                        String linkaMac = "";
                        List<Linka> linkas = LocksController.getInstance().getLinkas();
                        for (Linka linka : linkas){

                            linkaMac = linka.getMACAddress();
                            ScanFilter scanFilterMac = new ScanFilter.Builder().setDeviceAddress(linkaMac).build();
                            scanFilters.add(scanFilterMac);
                        }

                        *//**//*-----------------------------------------------------*//**//*

                        //To save battery, we scan less frequently, and we can directly call doConnectDevice()
                        // Need to scan, because android requires a scan in order for connection to take place
                      *//**//*  if(connectOneOutofTen > 10) {
                            LogHelper.e("SCAN", "SCANNING TO CONNECT");

                            connectOneOutofTen = 0;
                        }
                        connectOneOutofTen ++ ;*//**//*
                        if (scanner!=null) {
                            Log.w("BluetoothG","start scan");
                            scanner.startScan(scanFilters, scanSettings, scanCallback);
                        }



                    } else
                    {
                        bluetoothAdapter.startLeScan(scanCallbackDeprecated);
                    }
                    beginCollectScanResults();
                    stopScanHandler.postDelayed(stopScanRunnable, 2500);*//*
                    //LogHelper.e("AppBluetoothService", "Scanning...");

                }

            }
        }, 0, 3000); //Has to be at least 2 seconds for the scan to process
    }*/

    private void startTimer()
    {
        //Run scan every 2 seconds, even when app is in background
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                scanLeDevice(false);
                if (device == null){
                    if (LocksController.getInstance()!=null) {
                        LockController targetLockController = LocksController.getInstance().getLockController(); //Get the Latest LINKA (the linka that is displayed)
                        if (targetLockController != null) {
                            targetLockController.doDisconnectDevice();
                        }
                    }
                    scanLeDevice(true);
                }else if (!isDeviceConnected(device)){
                    if (LocksController.getInstance()!=null) {
                        LockController targetLockController = LocksController.getInstance().getLockController(); //Get the Latest LINKA (the linka that is displayed)
                        if (targetLockController != null) {
                            targetLockController.doDisconnectDevice();
                        }
                    }
                    scanLeDevice(true);
                }

            }
        }, 0, 5000); //Has to be at least 2 seconds for the scan to process
    }
    private void scanLeDevice(final boolean enable) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (bluetoothAdapter!=null) {
                //LogHelper.e("AppBluetoothService","API VERSION >= 21, Using Scanner");
                final BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(0)//Use low power mode of bluetooth scanning
                        .build();
                initScanCallback();

                if (scanner!=null) {
                    if (enable) {
                        Log.w(TAG, " start scan");
                        scanner.startScan(null, scanSettings, scanCallback);
                    } else {
                        scanner.flushPendingScanResults(scanCallback);
                        Log.w(TAG, " stop scan");
                        scanner.stopScan(scanCallback);

                    }
                }
            }
        }else {
            if (bluetoothAdapter!=null) {
                if (enable) {
                    // Stops scanning after a pre-defined scan period.
                    Log.w(TAG, " start scan");
                    bluetoothAdapter.startLeScan(mLeScanCallback);
                } else {
                    Log.w(TAG, " stop scan");
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }
        }
    }

    BluetoothAdapter bluetoothAdapter;

    private void initScanCallback() {

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (scanCallback == null) {
                scanCallback = new ScanCallback() {
                    @TargetApi(21)
                    @Override
                    public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                        super.onScanResult(callbackType, result);
                        LogHelper.d("SCAN", "Got device");
                        BluetoothDevice device = result.getDevice();
                        Linka linka = Linka.getLinkaByAddress(result.getDevice().getAddress());
                        if (linka!=null) {
                            connectDevice(device,linka);
                        }
                    }

                    @Override
                    public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
                        for (android.bluetooth.le.ScanResult sr : results) {
                            Log.w("BluetoothG","ScanResult - Results"+ sr.toString());
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.w("BluetoothG", "ScanFailed Error Code: " + errorCode);
                    }
                };
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    Linka linka = Linka.getLinkaByAddress(device.getAddress());
                    if (linka!=null) {
                        connectDevice(device,linka);
                    }
                }

            };

    private void connectDevice(BluetoothDevice device,Linka linka) {
        if (device!=null) {
            this.device = device;
           LockController targetLockController = LocksController.getInstance().getLockController(); //Get the Latest LINKA (the linka that is displayed)
            if(targetLockController != null){
                Log.w(TAG,"call doConnectDevice");
                targetLockController.doConnectDevice(linka);
            }
            scanLeDevice(false);
        }
    }


    private boolean isDeviceConnected(BluetoothDevice device) {
        if (device==null){
            return false;
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager!=null){
            return bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
        }
        return false;
    }



  /*  BluetoothAdapter.LeScanCallback scanCallbackDeprecated = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Linka linka = Linka.getLinkaByAddress(device.getAddress());
            if (linka != null) {
                LogHelper.d("AppBluetoothService", device.getAddress() + " - rssi: " + rssi + " (" + device.getName()  + ")");
            } else {
                LogHelper.d("AppBluetoothService", device.getAddress() + " - rssi: " + rssi);
            }
            addScanResult(device, rssi);
        }
    };
*/
    //ScanCallback scanCallback;
    ScanCallback scanCallback;

    // Wrapper class to initialize callback
    // If API > 21
   /* private void initScanCallback() {

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (scanCallback == null) {
                scanCallback = new ScanCallback() {
                    @TargetApi(21)
                    @Override
                    public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                        super.onScanResult(callbackType, result);
                        LogHelper.d("SCAN", "Got device");
                        BluetoothDevice device = result.getDevice();
                        int rssi = result.getRssi();
                        Linka linka = Linka.getLinkaByAddress(result.getDevice().getAddress());
                        if (linka != null) {
                            LogHelper.d("AppBluetoothService", device.getAddress() + " - rssi: " + rssi + " (" + device.getName() + ")");
                        } else {
                            LogHelper.d("AppBluetoothService", device.getAddress() + " - rssi: " + rssi);
                        }
                        addScanResult(result.getDevice(), result.getRssi());
                    }
                };
            }
        }
    }*/


    public void enableFixedTimeScanning(boolean enabled) {

        //Stop scanning
        this.is_active = enabled;
      /*  if (!this.is_active) {
            if (bluetoothAdapter != null) {
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                    initScanCallback();
                    if (scanner != null) {
                        try {
                            if (bluetoothAdapter.isEnabled()) {
                                scanner.stopScan(scanCallback);
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    bluetoothAdapter.stopLeScan(scanCallbackDeprecated);
                }
                //LogHelper.e("AppBluetoothService", "Stop...");
            }
        }*/
    }

    public boolean shouldAllowFixedTimeScanning() {
        if (!this.is_active) {
            return false;
        }

        return true;
    }




    public class ScanResult {
        BluetoothDevice device;
        int rssi;
//        boolean isLinka = false;
//        LocksController.LockController lockController = null;
    }
    public List<ScanResult> scanResults = new ArrayList<>();

    public void beginCollectScanResults() {
        scanResults.clear();
    }

//    //check the target Linka is connected
//    public boolean isTargetLinkaConnected(){
//        LocksController.LockController targetLockController = null;
//        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
//        if (targetLinka != null) {
//            targetLockController = LocksController.getInstance().getLockControllerByLinka(LinkaNotificationSettings.get_latest_linka());
//        }
//        if(targetLockController == null){
//            return false;
//        }else{
//            if (bluetoothManager != null) {
//                return !targetLockController.getIsDeviceDisconnected(bluetoothManager);
//            }
//            return false;
//        }
//    }

//    public void connectDeviceByRssi(){
//        ScanResult closestResult = null;
//        for (ScanResult scanResult : scanResults) {
//            if (scanResult.isLinka && closestResult == null) {
//                closestResult = scanResult;
//            }else if(scanResult.isLinka && closestResult.rssi < scanResult.rssi ){
//                closestResult = scanResult;
//            }
//        }
//
//        if(closestResult == null) return;
//
//
//        LocksController.LockController targetLockController = closestResult.lockController;
//        //new target linka
//        LinkaNotificationSettings.save_as_latest_linka(closestResult.lockController.getLinka());
//        Linka targetLinka = closestResult.lockController.getLinka();
//
//        if (targetLockController != null && targetLinka.rssi > AppDelegate.min_rssi_autoconnect) {
//            targetLockController.doConnectDevice();
//        }
//
//    }

   /* public void addScanResult(BluetoothDevice device, int rssi) {
        ScanResult scanResult = new ScanResult();
        scanResult.device = device;
        scanResult.rssi = rssi;
        scanResults.add(scanResult);

        LockController targetLockController = null;
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
        if (targetLinka != null) {
            targetLockController = LocksController.getInstance().getLockController(); //Get the Latest LINKA (the linka that is displayed)

        }

        String address = device.getAddress();
        String deviceName = device.getName();

        //------------------------------------------------//
        //The below check range notification disabled because its too unreliable
        //Sometimes it gives a back in range notification, but the bluetooth connection is unsuccessful
        //UpdateRSSI() is called in LockController anyways, so no need to call it before we connect
        *//*
        // do a check range notification here

        for (LockController lockController : lockControllerList) {
            if (lockController.getLinka().lock_address.equals(address)) {
                lockController.getLinka().updateRSSI(true, rssi);
//                scanResult.isLinka = true;
//                scanResult.lockController = lockController;
                break;
            }
        }
        *//*
        // ---------------------------------------------- //

//        scanResults.add(scanResult);


        // do a connection when:
        // 1. not connecting
        // 2. not connected
        // and do disconnect for inactive locks


        //Only allow one connection - CURRENT DEFAULT
        //if (!AppDelegate.shouldAllowMultipleAutoConnect) {
            *//*for (LockController lockController : lockControllerList) {
                boolean hasConnection = false;
                if (bluetoothManager != null) {
                    if (lockController.getLinka().isConnected
                            || !lockController.getIsDeviceDisconnected(bluetoothManager)
                            || lockController.getIsDeviceConnecting()
                            || lockController.getIsDeviceDisconnecting()) {
                        hasConnection = true;
                    }
                    if (hasConnection) {
                        if (targetLockController != lockController)
                        {
                            LogHelper.e("CONNECT", "Disconnect with any device that is not the target controller");
                            lockController.doDisconnectDevice();
                        }
                    }
                }
            }*//*
            if (!dfu &&
                    bluetoothAdapter != null &&
                    bluetoothAdapter.isEnabled() &&    // Make sure BTLE wasn't turned off
                    targetLockController != null &&
                    !targetLockController.getIsDeviceConnecting() &&
                    targetLockController.getLinka() != null &&
                    targetLockController.getLinka().lock_address.equals(address)){  //Check that the MAC id of the device is actually the linka on the homepage
                    //rssi > AppDelegate.min_rssi_autoconnect) {                 //No need for minimum rssi autoconnect; we should connect when we are in range

                //Check if it may be an unsuccessful DFU and a possible BLOD
                if("LinkaFu".equals(deviceName)){
                    Date curDate = new Date();
                    long curTime = curDate.getTime();
                    long secSinceCompleteDFU = curTime - dfuCompleteTimestamp;
                    LogHelper.e("BLOD","FOUND LINKA FU. Check if this is within a dfu time period: " + dfuCompleteTimestamp + "Current Time:" + curTime);

                    //If we are between 30 seconds to 5 minutes of a firmware update completion, then it means that it is a possible BLOD
                    if( secSinceCompleteDFU > 30000 && secSinceCompleteDFU < 300000 && !dfu){
                        LogHelper.e("BLOD","FOUND LINKA FU. Need to display popup!");
                        targetLockController.shouldDisplayBLODPopup = true;
                        dfuCompleteTimestamp = 0;
                    }

                    //If we continously detect a dfu for more than 2 minutes it means that it is a possible BLOD
                    if(firstDetectLinkaFuTimestamp == 0){
                        firstDetectLinkaFuTimestamp = curTime;
                    }
                    long timeContinuousLinkaFu = curTime - firstDetectLinkaFuTimestamp;

                    if( timeContinuousLinkaFu > 120000 && !dfu){
                        LogHelper.e("BLOD","FOUND LINKA FU. Need to display popup!");
                        targetLockController.shouldDisplayBLODPopup = true;
                        dfuCompleteTimestamp = 0;
                        firstDetectLinkaFuTimestamp = 0;
                    }
                }else {
                    //If we detect a non-LINKA Fu, then we can reset the timer.
                    firstDetectLinkaFuTimestamp = 0;
                }

                //MAKE CALL TO CONNECT DEVICE!!
                targetLockController.repeatConnectionUntilSuccessful = true;
               // targetLockController.doConnectDevice();
            }
        //}

        // do a connection when:
        // 1. not connecting
        // 2. not connected

*//*

        else //This is not running
        {
            for (LockController lockController : lockControllerList) {
                //if (lockController.getLinka().lock_address.equals(address) && rssi > AppDelegate.min_rssi_autoconnect) {
                //No need for minimum rssi autoconnect; we should connect when we are in range
                if (lockController.getLinka().lock_address.equals(address)) {
                    lockController.doConnectDevice();
                    break;
                }
            }
        }*//*
    }*/


   /* public void endCollectScanResults() {

        // if BLE is disabled between start & stop, don't record anything

        if (getContext() != null) {
            if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { return; }
            final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (bluetoothAdapter == null) { return; }
        if (!bluetoothAdapter.isEnabled()) { return; }



        // do a check out of range notification here
        // if not discovered, then out of range
*//*
        List<LockController> lockControllerList = LocksController.getInstance().getLockControllers();
        for (LockController lockController : lockControllerList) {
            if (lockController.getLinka().isConnected
                    || lockController.getIsDeviceConnecting()
                    || lockController.getIsDeviceDisconnecting()) {
                continue;
            }

            boolean isScanned = false;
            for (ScanResult scanResult : scanResults) {
                if (scanResult.device.getAddress().equals(lockController.getLinka().lock_address)) {
                    isScanned = true;
                    break;
                }
            }

            if (!isScanned)
            {
                lockController.getLinka().updateRSSI(false, 0); //False means that we are not connected
            }
        }*//*

        scanResults.clear();
    }*/
}
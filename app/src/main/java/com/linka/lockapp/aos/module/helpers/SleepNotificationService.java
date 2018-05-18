package com.linka.lockapp.aos.module.helpers;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

/**
 * Created by Vanson on 8/4/16.
 */
public class SleepNotificationService extends Service {

    public int lastSleepTime;
    private boolean isShowNotifiaction;
    public boolean overdueSleep = false;
    BluetoothManager bluetoothManager;
    Handler sleepNotificationHandler = new Handler();
    Runnable sleepNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            lastSleepTime = 0;
            sendNotification();
        }
    };


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public SleepNotificationService() {

    }

    public SleepNotificationService(Context context) {
        initialize();
        if (context != null) {
            this.context = context;
        }
        if (getContext() != null) {
            if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { return; }
            bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        }
        LogHelper.e("SleepNotificationService", "Create");
        instance = this;


    }

    Context context;



    public static SleepNotificationService instance;
    public static SleepNotificationService init(Context context) {
        if (instance == null) {
            instance = new SleepNotificationService(context);
        }
        return instance;
    }

    public static SleepNotificationService getInstance() {
        return instance;
    }






    public boolean initialize() {

        return true;

    }


    public Context getContext() {
        return context;
    }



    private void startTimer()
    {
        overdueSleep = false;

        try {
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();

        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(targetLinka);
        if(!accessKey.isAdmin()){
                return;
        }

        if (targetLinka != null || lastSleepTime != 0) {
            if (isTargetLinkaConnected() || lastSleepTime != 0) {

                //Get the sleep time
                lastSleepTime = targetLinka.isLocked ? targetLinka.settings_locked_sleep : targetLinka.settings_unlocked_sleep;

                // Final check to make sure the values have been set
                if (lastSleepTime == 0){
                    lastSleepTime = targetLinka.isLocked ? AppDelegate.default_lock_sleep_time : AppDelegate.default_unlock_sleep_time;
                }

                //subtract 3 seconds from sleep time so that it runs before out of range alert runs
                lastSleepTime -= 3;

                LogHelper.e("SleepNotificationService", "lastSleepTime: " + new Integer(lastSleepTime).toString());
                sleepNotificationHandler.removeCallbacks(sleepNotificationRunnable);
                sleepNotificationHandler.postDelayed(sleepNotificationRunnable, lastSleepTime * 1000);
            } /*else {
                sleepNotificationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTimer();
                    }
                }, 10000);
            }*/

        } /*else {
            sleepNotificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTimer();
                }
            }, 10000);
        }*/
        } catch (Exception ex) {
            // The timer isn't critical, and we appear to have a race condition if you remove the lock
            // from the UI while the timer is processing
            // catch it and try again
            LogHelper.e("SleepNotificationService", ex.getMessage());
            sleepNotificationHandler.removeCallbacks(sleepNotificationRunnable);
            sleepNotificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTimer();
                }
            }, 10000);
        }
    }

    public void restartTimer(){
        Log.i("restart timer", "restart time");
        startTimer();
    }

public void stopTimer(){
    sleepNotificationHandler.removeCallbacks(sleepNotificationRunnable);
}

        //check the target Linka is connected
    public boolean isTargetLinkaConnected(){
        LockController targetLockController = null;
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
        if (targetLinka != null) {
            targetLockController = LocksController.getInstance().getLockController();
        }
        if(targetLockController == null){
            return false;
        }else{
            if (bluetoothManager != null) {
               return !targetLockController.getIsDeviceDisconnected(bluetoothManager);
            }
            return false;
        }
    }

    public void sendNotification() {
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
        if (targetLinka == null) {
            return;
        }
        isShowNotifiaction = LinkaNotificationSettings.getSettings().settings_sleep_notification;
        if(!isShowNotifiaction) return;

        //Wait until disconnected if locked and ready to sleep
        if(targetLinka.isConnected && targetLinka.isLocked){
            overdueSleep = true;
            return;
        }

        //If unlocked, only send sleep notificaiton if connected
        if(!targetLinka.isConnected && !targetLinka.isLocked){
            return;
        }

        NotificationsHelper.getInstance().CreateLinkaNotificationMessage(getContext().getString(R.string.tabbar_icon_text_home),
                getContext().getString(R.string.sleep_notification_desc), R.raw.audio_out_of_range, false, LinkaActivity.getLinkaActivitiesByLinka(targetLinka).get(0), AppMainActivity.getInstance());
        /*
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getContext().getString(R.string.tabbar_icon_text_home))
                        .setContentText(getContext().getString(R.string.sleep_notification_desc));

        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
        */
    }



}
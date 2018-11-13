package com.linka.lockapp.aos.module.widget;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Vanson on 28/2/16.
 */
public class LocksController {

    public static final String LOCKSCONTROLLER_NOTIFY_REFRESHED = "[locks_controller_refresh]";
    public static final String LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS = "[locks_controller_refresh_settings]";
    public static final String LOCKSCONTROLLER_NOTIFY_READ_SETTINGS = "[Quick Lock checked]";

    /*
    Locks Controller
    Should support multiple lock connection management by:
    1. one lockBLEServiceProxy instance
    2. multiple lockGATTUpdateReceiver instances
    3. multiple lockBLEGenericListener instances
    4. current saved linkas
     */


    static LocksController instance;

    static LockController lockController;

    Context context;

    public LockBLEServiceProxy lockBLEServiceProxy;
    public LockBLEServiceListener lockBLEServiceListener;

    public static LocksController init(Context context) {
        instance = new LocksController();
        instance.context = context;
        instance.lockBLEServiceListener = new LockBLEServiceListener() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service, LockBLEServiceProxy lockBLEServiceProxy) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName, LockBLEServiceProxy lockBLEServiceProxy) {

            }
        };

        instance.lockBLEServiceProxy = new LockBLEServiceProxy(context, instance.lockBLEServiceListener);
        instance.lockBLEServiceProxy.onCreate();

        lockController = new LockController(context, LinkaNotificationSettings.get_latest_linka(), instance.onRefreshListener, instance.lockBLEServiceProxy);
        LogHelper.e("LocksController" ,"Creating new Lock Controller... " + lockController.hashCode);
        return instance;
    }

    public void onDestroy() {

        lockBLEServiceProxy.onDestroy();
    }

    public static LocksController getInstance() {
        return instance;
    }


    /*
    Get current Linkas function
     */
    public List<Linka> getLinkas() {
        List<Linka> linkas = Linka.getLinkas();
        return linkas;
    }

    public LockController getLockController() {
        return lockController;
    }


    public void refresh() {

        Linka linka = LinkaNotificationSettings.get_latest_linka();

        if (linka != null) {
            if (lockController.linka != null) {
                //Disconnect if lockcontroller is different
                if (!lockController.getLinka().lock_mac_address.equals(linka.lock_mac_address)) {
                    lockController.doDisconnectDevice();
                }
            }
            lockController.changeLinkaForThisLockController(context, linka, onRefreshListener, lockBLEServiceProxy);
            lockController.initialize(true, true);
        }
    }


    OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(LockController lockController) {
            EventBus.getDefault().post(LOCKSCONTROLLER_NOTIFY_REFRESHED);
        }

        @Override
        public void onRefreshSettings(LockController lockController) {
            EventBus.getDefault().post(LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS);
        }
    };


    public interface OnRefreshListener {
        public void onRefresh(LockController lockController);

        public void onRefreshSettings(LockController lockController);
    }
}

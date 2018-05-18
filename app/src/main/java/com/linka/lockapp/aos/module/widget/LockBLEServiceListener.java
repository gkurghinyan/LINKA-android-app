package com.linka.lockapp.aos.module.widget;

import android.content.ComponentName;
import android.os.IBinder;

/**
 * Created by Vanson on 22/2/16.
 */
public interface LockBLEServiceListener {

    /* LockBLEServiceProxy */
    public void onServiceConnected(ComponentName componentName, IBinder service, LockBLEServiceProxy lockBLEServiceProxy);
    public void onServiceDisconnected(ComponentName componentName, LockBLEServiceProxy lockBLEServiceProxy);
}

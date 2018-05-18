package com.linka.lockapp.aos.module.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linka.lockapp.aos.AppDelegate;

/**
 * Created by Vanson on 20/4/2016.
 */

/* NOTE



THIS RECEIVER IS NOT USED



THIS RECEIVER IS NOT USED


NOTE
 */

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /***** For start Service  ****/
        if (AppDelegate.shouldEnableLocationScanning) {
            Intent locationIntent = new Intent(context, AppLocationService.class);
            context.startService(locationIntent);
//            Toast.makeText(context, "BOOTUP LOCATION!", Toast.LENGTH_SHORT).show();
        }

        Intent bluetoothIntent = new Intent(context, AppBluetoothService.class);
        context.startService(bluetoothIntent);
//        Toast.makeText(context, "BOOTUP BLUETOOTH!", Toast.LENGTH_SHORT).show();
    }

}
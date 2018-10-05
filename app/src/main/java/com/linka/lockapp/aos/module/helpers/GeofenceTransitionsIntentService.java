package com.linka.lockapp.aos.module.helpers;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class GeofenceTransitionsIntentService extends IntentService {
    private String linkaAddress;
    private static final int TIME_FOR_LINKA_LOCKING_PROCESS = 3000;
    public static final String LINKA_ADDRESS_EXTRA = "LinkaAddressExtra";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        linkaAddress = intent.getStringExtra(LINKA_ADDRESS_EXTRA);
        if (geofencingEvent.hasError()) {
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            if (Prefs.getString(Constants.LINKA_ADDRESS_FOR_AUTO_UNLOCK,"").equals("")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!LocksController.getInstance().getLockController().getLinka().isLocked) {
                            Intent intent1 = new Intent(AppDelegate.getInstance(), GeofenceService.class);
                            intent1.putExtra(GeofenceService.GEOFENCE_ACTION, GeofenceService.GEOFENCE_REMOVE_ACTION);
                            AppDelegate.getInstance().startService(intent1);
                        }
                    }
                },TIME_FOR_LINKA_LOCKING_PROCESS);

                if(Prefs.getBoolean(Constants.SHOW_BACK_IN_RANGE_NOTIFICATION,false)) {
                    LinkaActivity.saveLinkaActivity(Linka.getLinkaByAddress(linkaAddress), LinkaActivity.LinkaActivityType.isBackInRange);
                }
            }

        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Bundle args = new Bundle();
            args.putBoolean(Constants.LINKA_ADDRESS_FOR_AUTO_UNLOCK,true);

            if(Prefs.getBoolean(Constants.SHOW_OUT_OF_RANGE_NOTIFICATION,false)) {
                LinkaActivity.saveLinkaActivity(Linka.getLinkaByAddress(linkaAddress), LinkaActivity.LinkaActivityType.isOutOfRange);
            }

            LinkaActivity.saveLinkaActivity(Linka.getLinkaByAddress(linkaAddress), LinkaActivity.LinkaActivityType.isAutoUnlockEnabled);
            SharedPreferences.Editor editor = Prefs.edit();
            editor.putString(Constants.LINKA_ADDRESS_FOR_AUTO_UNLOCK,linkaAddress);
            editor.apply();

            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

            ArrayList<String> triggeredIds = new ArrayList<>();

            for (Geofence geofence : triggeredGeofences) {
                triggeredIds.add(geofence.getRequestId());
            }

            removeGeofences(triggeredIds);

        }
    }

    private void removeGeofences(ArrayList<String> requestIds) {
        Intent intent = new Intent(getApplicationContext(), GeofenceService.class);

        intent.putExtra(GeofenceService.GEOFENCE_ACTION, GeofenceService.GEOFENCE_REMOVE_ACTION);
        intent.putStringArrayListExtra(GeofenceService.GEOFENCES_LIST_OF_ID, requestIds);

        startService(intent);
    }

}

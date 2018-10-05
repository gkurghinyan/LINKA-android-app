package com.linka.lockapp.aos.module.helpers;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;

import java.util.ArrayList;
import java.util.List;

import br.com.goncalves.pugnotification.notification.Load;
import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by kyle on 3/7/18.
 */

public class GeofencingIntentService extends IntentService {

    public static GeofencingIntentService instance;
    Context context;
    public GeofencingIntentService(){
        super("GeofencingIntentService");

    }

    public GeofencingIntentService(Context context ){
        super("GeofencingIntentService");

        instance = this;

        if (context != null) {
            this.context = context;
        }

    }

    public static GeofencingIntentService init(Context context) {
        if (instance == null) {
            instance = new GeofencingIntentService(context);
        }
        return instance;
    }


    @Override
    public void onCreate(){
        super.onCreate();

    }
    static String TAG = "GEOFENCING_INTENT_SERVICE";
    protected void onHandleIntent(Intent intent) {

        LogHelper.e("GEOFENCE", "Geofence Intent !");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(this,
                    geofencingEvent.getErrorCode());
            LogHelper.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            LogHelper.e("GEOFENCE", "Geofence Entered !");

            LinkaActivity.saveLinkaActivity(LinkaNotificationSettings.get_latest_linka(), LinkaActivity.LinkaActivityType.isBackInRange);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            LogHelper.e("GEOFENCE", "Geofence Exited!");

            LinkaActivity.saveLinkaActivity(LinkaNotificationSettings.get_latest_linka(), LinkaActivity.LinkaActivityType.isOutOfRange);
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for(Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                String geofence_Id = geofence.getRequestId();
                LogHelper.e("GEOFENCE", "REQUEST ID = " + geofence_Id);
            }

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.

            /*
            sendNotification(geofenceTransitionDetails);

            */
            LogHelper.e(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            LogHelper.e(TAG, "Invalid type" + geofenceTransition);
        }
    }

    String getTransitionString(int transitionType)
    {
        switch (transitionType)
        {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exited";
            default:
                return "Unknown";
        }
    }
    String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences)
    {
        String geofenceTransitionString = getTransitionString(geofenceTransition);

        List<String> triggeringGeofencesIdsList = new ArrayList<String>();
        for (Geofence geofence : triggeringGeofences)
        {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = triggeringGeofencesIdsList.toString();

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    public static String getErrorString(Context context, int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Not Available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error";
        }
    }

}

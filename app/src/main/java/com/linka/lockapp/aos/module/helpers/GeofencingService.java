package com.linka.lockapp.aos.module.helpers;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kyle on 3/6/18.
 */


/** This class has 2 geofences
 *  1) The first geofence controls autounlocking. If user goes outside of this geofence, and returns, then the lock will autounlock
 *
 *  2) The second geofence controls bluetooth scanning. If the user goes outside of this geofence, then bluetooth scanning will turn off, or until the app comes into the foreground.
 *  This is to conserve battery power. This geofence has a set radius of 200 meters
 */
public class GeofencingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "GEOFENCE";

    static int SCANNING_GEOFENCE_RADIUS = 200;

    private GeofencingClient mGeofencingClient;

    public List<Geofence> mGeofenceList = new ArrayList<>();
    public Geofence geofence1;

    public GoogleApiClient googleApiClient;
    Context context;

    PendingIntent mGeofencePendingIntent;
    public GeofencingService() {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LogHelper.e(TAG, "Connected to Fused Location");
    }


    public GeofencingService(Context context) {
        LogHelper.e(TAG, "GeofencingService");

        instance = this;

        if (context != null) {
            this.context = context;
        }
    }


    public static GeofencingService instance;

    public static GeofencingService init(Context context) {
        LogHelper.e(TAG, "init");

        if (instance == null) {
            instance = new GeofencingService(context);
        }
        instance.context = context;

        instance.mGeofencingClient = LocationServices.getGeofencingClient(context);

        instance.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(instance)
                .addOnConnectionFailedListener(instance).build();

        return instance;
    }

    public static GeofencingService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogHelper.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();

    }

    public void addGeofence(double latitude, double longitude, int meters, int timeInMs, String id){
        geofence1 = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)

                .setCircularRegion(
                        latitude,
                        longitude,
                        meters
                )
                .setExpirationDuration(timeInMs)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();


        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        LogHelper.e("GEOFENCE", "Geofence Added!");
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        LogHelper.e("GEOFENCE", "Geofence Failure!");
                        // Failed to add geofences
                        // ...
                    }
                });
    }


    public void startGeofence1(double latitude, double longitude, int meters) {
        LogHelper.e("GEOFENCE", "About to Add Geofence 1");


        int fivehoursInMilliseconds = 5*60*60*1000;
        addGeofence(latitude,longitude, meters, fivehoursInMilliseconds, "Geofence 1");

    }

    public void startGeofence2(double latitude, double longitude) {
        LogHelper.e(TAG, "onCreate");

        int onehourInMilliseconds = 60*60*1000;
        addGeofence(latitude,longitude, 2000, onehourInMilliseconds, "Geofence 2");


    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.addGeofence(geofence1);
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofencingIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }



    @Override
    public void onDestroy() {
        LogHelper.e(TAG, "onDestroy");
        super.onDestroy();
    }


}

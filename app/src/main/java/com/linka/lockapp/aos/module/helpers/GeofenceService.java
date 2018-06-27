package com.linka.lockapp.aos.module.helpers;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.util.ArrayList;
import java.util.List;

public class GeofenceService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String GEOFENCE_ID = "Geofence 1";
    public static final String GEOFENCE_ACTION = "GeofenceAction";
    public static final String GEOFENCES_LIST_OF_ID = "GeofencesListOfId";
    public static final int GEOFENCE_ADD_ACTION = 1;
    public static final int GEOFENCE_REMOVE_ACTION = 2;

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient locationProviderClient;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofences = new ArrayList<>();
    private PendingIntent pendingIntent;
    private int currentAction;
    private List<String> listOfId;

    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            locationProviderClient.removeLocationUpdates(this);
            addGeofence(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude(), LocksController.getInstance().getLockController().getLinka().getAuto_unlock_radius(), Geofence.NEVER_EXPIRE, GEOFENCE_ID);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent != null) {
            currentAction = intent.getIntExtra(GEOFENCE_ACTION, 0);
            listOfId = intent.getStringArrayListExtra(GEOFENCES_LIST_OF_ID);
            initializeGoogleApiClient();
            return START_STICKY;
        }
        else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void initializeGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
    }

    @SuppressLint("MissingPermission")
    private void initializeGeofenceClient(){
        geofencingClient = LocationServices.getGeofencingClient(this);
        if(currentAction == GEOFENCE_ADD_ACTION) {
            locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }else if(currentAction == GEOFENCE_REMOVE_ACTION){
            if(listOfId == null){
                listOfId = new ArrayList<>();
                listOfId.add(GEOFENCE_ID);
            }
            geofencingClient.removeGeofences(listOfId);
        }
    }

    @SuppressLint("MissingPermission")
    public void addGeofence(double latitude, double longitude, int meters, long timeInMs, String id) {
        geofences.add(new Geofence.Builder()
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
                .build());

        geofencingClient.addGeofences(getGeofencingRequest(), getPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        googleApiClient.disconnect();
                        stopSelf();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        stopSelf();
                        Toast.makeText(GeofenceService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getPendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra(GeofenceTransitionsIntentService.LINKA_ADDRESS_EXTRA,LocksController.getInstance().getLockController().getLinka().getMACAddress());
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initializeGeofenceClient();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
        stopSelf();
    }
}

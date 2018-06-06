package com.linka.lockapp.aos.module.helpers;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.module.widget.LocksController;

/**
 * Created by Vanson on 7/4/16.
 */

public class AppLocationService extends Service {

    private static final String TAG = "LOCATION";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = AppDelegate.locationScanningInterval;
    private static final float LOCATION_DISTANCE = 10f;

    public double latitude = 0;
    public double longitude = 0;
    public Location lastLocation;

    Context context;

    public AppLocationService() {

    }

    public AppLocationService(Context context) {
        LogHelper.e(TAG, "AppLocationService");


        instance = this;

        if (context != null) {
            this.context = context;
        }

        startLocation();

    }


    public static AppLocationService instance;
    public static AppLocationService init(Context context) {
        if (instance == null) {
            instance = new AppLocationService(context);
        }
        return instance;
    }

    public static AppLocationService getInstance() {
        return instance;
    }



    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            LogHelper.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            LogHelper.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            longitude = mLastLocation.getLongitude();
            latitude = mLastLocation.getLatitude();
            lastLocation = mLastLocation;
        }

        @Override
        public void onProviderDisabled(String provider) {
            LogHelper.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            LogHelper.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LogHelper.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

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

    public void startLocation() {
        LogHelper.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        LogHelper.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        LogHelper.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public Location getLocation(){
        return lastLocation;

    }
}
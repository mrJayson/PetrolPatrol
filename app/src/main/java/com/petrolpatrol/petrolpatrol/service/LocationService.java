package com.petrolpatrol.petrolpatrol.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.petrolpatrol.petrolpatrol.util.Constants;

import java.io.Serializable;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        Serializable {

    private static final String TAG = makeLogTag(LocationService.class);

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Binder given to clients of this service
    private IBinder binder;

    // Location related members
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean isCurrentlyLocating;


    private synchronized GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private LocationRequest buildLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        //TODO recheck the update intervals
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        return locationRequest;
    }

    @Override
    public void onCreate() {
        binder = new LocationServiceBinder();
        mGoogleApiClient = buildGoogleApiClient();
        mLocationRequest = buildLocationRequest();
        isCurrentlyLocating = false;

        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        stopLocating();
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(Constants.NEW_LOCATION_AVAILABLE);
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void startLocating() {
        // always check to see if permission is granted first
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // not granted, already asked for permission before, so do not ask again
            // display message instead
            return;
        }

        // permission granted, request for location updates, when a location is found,
        // the onLocationChanged callback is invoked
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            isCurrentlyLocating = true;
        }
    }

    public void stopLocating() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        isCurrentlyLocating = false;
    }

    public boolean isCurrentlyLocating() {
        return isCurrentlyLocating;
    }

    /**
     * Local Binder class
     */
    public class LocationServiceBinder extends Binder implements Serializable {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}

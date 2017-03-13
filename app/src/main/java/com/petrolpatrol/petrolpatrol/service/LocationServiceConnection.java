package com.petrolpatrol.petrolpatrol.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.Serializable;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class LocationServiceConnection implements ServiceConnection {
    private static final String TAG = makeLogTag(LocationServiceConnection.class);

    private LocationService locationService;
    private boolean isBound;
    private Context mContext;

    public LocationServiceConnection(Context context) {
        isBound = false;
        mContext = context;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        LOGI(TAG, "onServiceConnected");

        LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) iBinder;
        locationService = binder.getService();
        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    public void bindService() {
        // Ensure that only one binding occurs at a time or else stopping the service will not work
        if (!isBound()) {
            Intent startLocationServiceIntent = new Intent(mContext, LocationService.class);
            mContext.bindService(startLocationServiceIntent, this, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbindService() {
        // cannot unbind an already unbound service, an exception will be thrown
        if (isBound()) {
            try {
                mContext.unbindService(this);
                LOGI(TAG, "onServiceDisconnected");

                locationService = null;
                isBound = false;
            } catch (Exception e) {
                LOGE(TAG, "Service was already unbound when trying to unbind");
            }
        }
    }

    public void startLocating() throws IllegalStateException {
        if (isBound) {
            if (!locationService.isCurrentlyLocating()) {
                //TODO redo, this does not work when location is off and this is called
                locationService.startLocating();
            }
        } else {
            throw new IllegalStateException("Service is not yet bound, cannot invoke any service methods.");
        }
    }

    public void stopLocating() throws IllegalStateException {
        if (isBound) {
            locationService.stopLocating();
        } else {
            throw new IllegalStateException("Service is not yet bound, cannot invoke any service methods.");
        }
    }

    public LocationService getHandle() {
        return locationService;
    }

    public boolean isBound() {
        return isBound;
    }
}

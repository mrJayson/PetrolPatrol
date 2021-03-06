package com.petrolpatrol.petrolpatrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class NewLocationReceiver extends BroadcastReceiver {

    private static final String TAG = makeLogTag(NewLocationReceiver.class);

    private boolean isListening;

    private Listener mListener;

    public NewLocationReceiver(NewLocationReceiver.Listener listener) {
        isListening = false;
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get extra data included in the Intent
        mListener.onLocationReceived((Location) intent.getParcelableExtra(LocationService.ARG_LOCATION));
    }

    public void register(Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(LocationService.ACTION_NEW_LOCATION));
        isListening = true;
    }

    public void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        isListening = false;
    }

    public boolean isListening() {
        return isListening;
    }

    public interface Listener {
        void onLocationReceived(Location location);
    }
}

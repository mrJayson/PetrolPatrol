package com.petrolpatrol.petrolpatrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import com.petrolpatrol.petrolpatrol.util.Constants;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * Created by jason on 16/02/17.
 */
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
        LOGI(TAG, "onReceive");

        mListener.onLocationReceived((Location) intent.getParcelableExtra("location"));
    }

    public void register(Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(Constants.NEW_LOCATION_AVAILABLE));
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

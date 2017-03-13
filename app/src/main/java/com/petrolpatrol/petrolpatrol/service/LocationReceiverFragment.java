package com.petrolpatrol.petrolpatrol.service;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.petrolpatrol.petrolpatrol.R;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public abstract class LocationReceiverFragment extends Fragment implements NewLocationReceiver.Listener {

    private NewLocationReceiver newLocationReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newLocationReceiver = new NewLocationReceiver(this);
    }

    protected void registerReceiverToLocationService() {
        newLocationReceiver.register(getContext());
    }

    protected void unregisterReceiverFromLocationService() {
        newLocationReceiver.unregister(getContext());
    }

}

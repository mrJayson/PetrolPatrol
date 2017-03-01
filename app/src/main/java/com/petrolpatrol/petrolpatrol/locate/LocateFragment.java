package com.petrolpatrol.petrolpatrol.locate;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.datastore.SharedPreferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocateFragment extends Fragment implements NewLocationReceiver.Listener {

    private static final String TAG = makeLogTag(LocateFragment.class);
    private Listener parentListener;
    private NewLocationReceiver newLocationReceiver;



    public static LocateFragment newInstance() {
        // Factory method for creating new fragment instances
        // automatically takes parameters and stores in a bundle for later use in onCreate
        LocateFragment fragment = new LocateFragment();
        Bundle bundle = new Bundle();
        //TODO change to parcelable if there are any args at all
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGI(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // No previous state to restore from
        } else {
            // previous state contains data needed to recreate fragment to how it was before
        }
        newLocationReceiver = new NewLocationReceiver(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_locate, container, false);
        FloatingActionButton locateFab = (FloatingActionButton) v.findViewById(R.id.locate_fab);

        locateFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                registerReceiverToLocationService();
                parentListener.startLocating();
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        LOGI(TAG, "onStart");
        super.onStart();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        LOGI(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LOGI(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        LOGI(TAG, "onStop");
        // Unregister if fragment closes while still broadcast receiving
        unregisterReceiverFromLocationService();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        LOGI(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLocationReceived(Location location) {

        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        FuelCheckClient client = new FuelCheckClient(getContext());

        client.getFuelPricesWithinRadius(location.getLatitude(), location.getLongitude(), parentListener.getSelectedSortBy(), parentListener.getSelectedFuelType(), new FuelCheckClient.FuelCheckResponse<List<Station>>() {
            @Override
            public void onCompletion(List<Station> res) {
                parentListener.displayListFragment(res);
            }
        });

    }

    private void registerReceiverToLocationService() {
        newLocationReceiver.register(getContext());
    }

    private void unregisterReceiverFromLocationService() {
        newLocationReceiver.unregister(getContext());
    }

    public interface Listener {
        void startLocating();
        void stopLocating();
        void displayListFragment(List<Station> list);
        String getSelectedFuelType();
        String getSelectedSortBy();
    }

    @Override
    public void onAttach(Context context) {
        // Context is the parent activity
        LOGI(TAG, "onAttach");
        super.onAttach(context);

        // Check to see if the parent activity has implemented the callback
        if (context instanceof Listener) {
            parentListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        LOGI(TAG, "onDetach");
        super.onDetach();
        parentListener = null;
    }

}

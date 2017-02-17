package com.petrolpatrol.petrolpatrol.locate;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SharedPreferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocateFragment extends Fragment implements NewLocationReceiver.Listener {

    private static final String TAG = makeLogTag(LocateFragment.class);
    private Listener parentListener;
    private NewLocationReceiver newLocationReceiver = new NewLocationReceiver(this);

    public static LocateFragment newInstance(LocationServiceConnection locationServiceConnection) {
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
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // No previous state to restore from
        } else {
            // previous state contains data needed to recreate fragment to how it was before
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_locate, container, false);
        FloatingActionButton locateFab = (FloatingActionButton) v.findViewById(R.id.locate_fab);
//        locateFab.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                registerReceiverToLocationService();
//                parentListener.startLocating();
//            }
//        });
        locateFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                registerReceiverToLocationService();
                parentListener.startLocating();
                FuelCheckClient client = new FuelCheckClient(getContext());

                if (SharedPreferences.getInstance().getString(SharedPreferences.Key.OAUTH_TOKEN) == null ||
                        TimeUtils.isExpired(SharedPreferences.getInstance().getLong(SharedPreferences.Key.OAUTH_EXPIRY_TIME))) {
                    // If there is no auth token or it is expired, get a working one
                    client.authToken(getActivity().getString(R.string.base64Encode));
                }
                client.getFuelPricesWithinRadius(-33.8573284, 150.9372339, "price", "E10");

            }
        });

        return v;
    }

    private void registerReceiverToLocationService() {
        newLocationReceiver.register(getActivity());
    }

    private void unregisterReceiverFromLocationService() {
        newLocationReceiver.unregister(getActivity());
    }

    @Override
    public void onLocationReceived(Location location) {
        String s = String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude());
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface Listener {
        void startLocating();
        void stopLocating();
    }

    @Override
    public void onAttach(Context context) {
        // Context is the parent activity
        super.onAttach(context);

        // Check to see if the parent activity has implemented the callback
        if (context instanceof Listener) {
            parentListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentListener = null;
    }

}

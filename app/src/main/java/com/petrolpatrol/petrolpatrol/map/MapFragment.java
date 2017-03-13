package com.petrolpatrol.petrolpatrol.map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationReceiverFragment;
import com.petrolpatrol.petrolpatrol.ui.Action;

import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class MapFragment extends LocationReceiverFragment implements OnMapReadyCallback {
    private static final String TAG = makeLogTag(MapFragment.class);

    private Listener parentListener;

    private static final String ARG_ACTION = "action";

    private String action = null;

    private List<Station> stationsData = null;
    private CameraPosition cameraPosition = null;

    private GoogleMap map;
    private MapView mapView;

    public MapFragment() {
        // Required empty public constructor
    }

    public static  MapFragment newInstance(Action action) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        if (action != null) {
            args.putString(ARG_ACTION, action.getAction());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static MapFragment newInstance() {
        return newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGE(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            action = getArguments().getString(ARG_ACTION);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LOGE(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOGE(TAG, "onCreateView");
        View view =  inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        LOGE(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LOGE(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LOGE(TAG, "onMapReady");
        this.map = googleMap;
        if (action != null) {
            if (action.equals(Action.FIND_BY_GPS)) {
                registerReceiverToLocationService();
                parentListener.startLocating();
                action = null; // clear the action to prevent it from activating again
            } else if (action.equals(Action.FIND_BY_LOCATION)) {

            }
        } else {

        }
    }

    @Override
    public void onLocationReceived(Location location) {


        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Move camera to current location
        LatLng current = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 8));

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Get fuel data with current location
        FuelCheckClient client = new FuelCheckClient(getContext());

        Preferences pref = Preferences.getInstance();
        client.getFuelPricesWithinRadius(
                latitude,
                longitude,
                pref.getString(Preferences.Key.SELECTED_SORTBY),
                pref.getString(Preferences.Key.SELECTED_FUELTYPE), new FuelCheckClient.FuelCheckResponse<List<Station>>() {
            @Override
            public void onCompletion(List<Station> res) {

                stationsData = res;

                for (Station station : res) {
                    map.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOGE(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.list:
                if (stationsData != null) {

                    parentListener.displayListFragment(stationsData);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        LOGE(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        LOGE(TAG, "onResume");
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
        LOGE(TAG, "onPause");
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
        LOGE(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        LOGE(TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        LOGE(TAG, "onDestroy");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            parentListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentListener = null;
    }

    public interface Listener {
        void startLocating();
        void stopLocating();
        void displayListFragment(List<Station> list);
    }
}

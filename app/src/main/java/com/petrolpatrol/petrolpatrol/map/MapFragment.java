package com.petrolpatrol.petrolpatrol.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationReceiverFragment;
import com.petrolpatrol.petrolpatrol.ui.Action;
import com.petrolpatrol.petrolpatrol.util.Constants;
import com.petrolpatrol.petrolpatrol.util.Utils;

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

    public static MapFragment newInstance(Action action) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        if (action != null) {
            args.putString(ARG_ACTION, action.getAction());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            action = getArguments().getString(ARG_ACTION);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.setMaxZoomPreference(Constants.MAX_ZOOM);

        // Ensure that the map stays within NSW
        LatLng southWestBound = new LatLng(Constants.SOUTH_BOUND, Constants.WEST_BOUND);
        LatLng northEastBound = new LatLng(Constants.NORTH_BOUND, Constants.EAST_BOUND);
        map.setLatLngBoundsForCameraTarget(new LatLngBounds(southWestBound, northEastBound));

        if (action != null) {
            if (action.equals(Action.FIND_BY_GPS)) {
                registerReceiverToLocationService();
                parentListener.startLocating();
                action = null; // clear the action to prevent it from activating again
            } else if (action.equals(Action.FIND_BY_LOCATION)) {
                action = null; // clear the action to prevent it from activating again
            }
        } else {
            if (cameraPosition != null) {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            if (stationsData != null) {
                for (Station station : stationsData) {
                    map.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                }
            }
        }

        // Update map data upon new camera position
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //LOGE(TAG, String.valueOf(Utils.zoomToRadius(map.getCameraPosition().zoom)));
            }
        });
    }

    @Override
    public void onLocationReceived(Location location) {

        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Move camera to current location
        LatLng current = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, Utils.radiusToZoom(3)));

        // Get fuel data with current location
        FuelCheckClient client = new FuelCheckClient(getContext());
        Preferences pref = Preferences.getInstance();
        client.getFuelPricesWithinRadius(
                latitude,
                longitude,
                pref.getString(Preferences.Key.SELECTED_SORTBY),
                pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                new FuelCheckClient.FuelCheckResponse<List<Station>>() {
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        // Record current camera position to restore later
        cameraPosition = map.getCameraPosition();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
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

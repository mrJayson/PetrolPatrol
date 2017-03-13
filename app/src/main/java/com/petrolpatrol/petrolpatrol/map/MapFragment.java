package com.petrolpatrol.petrolpatrol.map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.petrolpatrol.petrolpatrol.R;
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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            action = getArguments().getString(ARG_ACTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_map, container, false);
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
        registerReceiverToLocationService();
        parentListener.startLocating();
    }

    @Override
    public void onLocationReceived(Location location) {
        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Move camera to current location
        LatLng current = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLng(current));
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Get fuel data with current location
        FuelCheckClient client = new FuelCheckClient(getContext());
        client.getFuelPricesWithinRadius(latitude, longitude, parentListener.getSelectedSortBy(), parentListener.getSelectedFuelType(), new FuelCheckClient.FuelCheckResponse<List<Station>>() {
            @Override
            public void onCompletion(List<Station> res) {
                for (Station station : res) {
                    map.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                }
            }
        });
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
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
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
        String getSelectedFuelType();
        String getSelectedSortBy();
    }
}

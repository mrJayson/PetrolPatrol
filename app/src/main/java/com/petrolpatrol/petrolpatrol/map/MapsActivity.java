package com.petrolpatrol.petrolpatrol.map;

import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.RequestTag;
import com.petrolpatrol.petrolpatrol.list.ListActivity;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.ui.IntentAction;
import com.petrolpatrol.petrolpatrol.util.Constants;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, NewLocationReceiver.Listener {

    private static final String TAG = makeLogTag(MapsActivity.class);

    // Keys for bundles and intents
    public static final String ARG_ACTION = "ARG_ACTION";

    private LocationServiceConnection locationServiceConnection;

    private NewLocationReceiver newLocationReceiver;

    /**
     * Parent activities can pass in an intent action to the MapsActivity to indicate the purpose of this MapsActivity.
     * The Map Activity will perform different actions based on the intentAction variable.
     */
    private IntentAction intentAction;

    // Handle to interact with the google Map UI
    private GoogleMap googleMap;

    private List<Station> stationList;
    private CameraPosition cameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentAction = null;
        if (savedInstanceState != null) {
            intentAction = savedInstanceState.getParcelable(ARG_ACTION);
        } else {
            intentAction = getIntent().getParcelableExtra(ARG_ACTION);
        }

        getLayoutInflater().inflate(R.layout.activity_maps, content);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationServiceConnection = new LocationServiceConnection(this);
        newLocationReceiver = new NewLocationReceiver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.list:
                if (stationList != null) {
                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    Bundle bundle = intent.getExtras();
                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    bundle.putParcelableArrayList(ListActivity.ARG_STATIONS, (ArrayList<? extends Parcelable>) stationList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        initialiseMap();

        if (intentAction == null) {
            if (cameraPosition != null) {
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            if (stationList != null) {
                for (Station station : stationList) {
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                }
            }
        } else {
            if (intentAction.toString().equals(IntentAction.FIND_BY_GPS)) {
                if (requestLocationPermissionsIfNecessary()) {
                    newLocationReceiver.register(getBaseContext());
                    if (!locationServiceConnection.isBound()) {
                        locationServiceConnection.bindService();
                    }
                    // If service is not yet bound, the location request will be queued up
                    locationServiceConnection.startLocating();
                }
            }
            else if (intentAction.toString().equals(IntentAction.FIND_BY_LOCATION)) {

            }
        }
    }

    @Override
    protected void onStop() {
        newLocationReceiver.unregister(getBaseContext());
        locationServiceConnection.stopLocating();
        locationServiceConnection.unbindService();
        super.onStop();
    }

    @Override
    public void onLocationReceived(Location location) {
        // only need one location update
        newLocationReceiver.unregister(getBaseContext());
        locationServiceConnection.stopLocating();

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        // Get fuel data with current location
        FuelCheckClient client = new FuelCheckClient(getBaseContext());
        Preferences pref = Preferences.getInstance();
        // Map view uses only price sorted list
        client.getFuelPricesWithinRadius(
                latitude,
                longitude,
                pref.getString(Preferences.Key.SELECTED_SORTBY),
                pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                new RequestTag(RequestTag.GET_FUELPRICES_WITHIN_RADIUS),
                new FuelCheckClient.FuelCheckResponse<List<Station>>() {
                    @Override
                    public void onCompletion(List<Station> res) {
                        stationList = res;
                        googleMap.clear();
                        double maxDistance = 0;
                        for (Station station : res) {
//                            googleMap.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                            if (station.getDistance() > maxDistance) {
                                maxDistance = station.getDistance();
                            }
                        }

                        // Move camera to current location
                        LatLng current = new LatLng(latitude, longitude);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, (float) (Utils.radiusToZoom(maxDistance * getMapAspectRatio()))));
                    }
                });
    }

    private void initialiseMap() {

        // Add styling to googleMaps
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setMinZoomPreference(Constants.MIN_ZOOM);
        googleMap.setMaxZoomPreference(Constants.MAX_ZOOM);

        // Ensure that the map stays within NSW
        LatLng southWestBound = new LatLng(Constants.SOUTH_BOUND, Constants.WEST_BOUND);
        LatLng northEastBound = new LatLng(Constants.NORTH_BOUND, Constants.EAST_BOUND);
        googleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(southWestBound, northEastBound));

        // Set default camera position to Sydney
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.SYDNEY_LAT, Constants.SYDNEY_LONG), Constants.DEFAULT_ZOOM));

        // Update map data upon new camera position
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                FuelCheckClient client = new FuelCheckClient(getBaseContext());
                client.cancelRequests(new RequestTag(RequestTag.GET_FUELPRICES_WITHIN_RADIUS));
                Preferences pref = Preferences.getInstance();
                double latitude = googleMap.getCameraPosition().target.latitude;
                double longitude = googleMap.getCameraPosition().target.longitude;
                int radius = (int) (Utils.zoomToRadius(googleMap.getCameraPosition().zoom) / getMapAspectRatio());
                client.getFuelPricesWithinRadius(
                        latitude,
                        longitude,
                        radius,
                        pref.getString(Preferences.Key.SELECTED_SORTBY),
                        pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                        new RequestTag(RequestTag.GET_FUELPRICES_WITHIN_RADIUS),
                        new FuelCheckClient.FuelCheckResponse<List<Station>>() {
                            @Override
                            public void onCompletion(List<Station> res) {
                                stationList = res;
                                googleMap.clear();
                                for (Station station : res) {
                                    googleMap.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                                }
                            }
                        });
            }
        });
    }

    private boolean requestLocationPermissionsIfNecessary() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // not granted, ask the user for permission
            ActivityCompat.requestPermissions( this, new String[] {  ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION  },
                    Constants.PERMISSION_REQUEST_ACCESS_LOCATION );
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    // Permission Granted
                    newLocationReceiver.register(getBaseContext());
                    if (!locationServiceConnection.isBound()) {
                        locationServiceConnection.bindService();
                    }
                    // If service is not yet bound, the location request will be queued up
                    locationServiceConnection.startLocating();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Permission denied, location features not enabled", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private double getMapAspectRatio() {
        int mapWidth = content.getWidth();
        int mapHeight = content.getHeight();
        double aspectRatio;
        if (mapWidth != 0 && mapHeight != 0) {
            if (mapWidth > mapHeight) {
                aspectRatio = ((double) mapWidth / (double) mapHeight);
            } else {
                aspectRatio = ((double) mapHeight / (double) mapWidth);
            }
        } else {
            // If aspect ratio cannot be determined, then aspect ratio of 1 causes no effect
            aspectRatio = 1;
        }
        return aspectRatio;
    }
}

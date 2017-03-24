package com.petrolpatrol.petrolpatrol.map;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.RequestTag;
import com.petrolpatrol.petrolpatrol.list.ListActivity;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.util.Constants;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
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

    public static final String ACTION_GPS = "ACTION_GPS";

    private LocationServiceConnection locationServiceConnection;

    private NewLocationReceiver newLocationReceiver;

    private MenuItem fuelTypeMenuItem;

    private boolean cancelMarkerUpdate;

    /**
     * Parent activities can pass in an intent action to the MapsActivity to indicate the purpose of this MapsActivity.
     * The Map Activity will perform different actions based on the intentAction variable.
     */

    // Handle to interact with the google Map UI
    private GoogleMap googleMap;

    private List<Station> stationList;
    private CameraPosition cameraPosition;

    private ClusterManager<Marker> clusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_maps, content);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container_map);
        mapFragment.getMapAsync(this);

        locationServiceConnection = new LocationServiceConnection(this);
        newLocationReceiver = new NewLocationReceiver(this);

        cancelMarkerUpdate = true;
    }

    @Override
    protected void onStop() {
        newLocationReceiver.unregister(getBaseContext());
        locationServiceConnection.stopLocating();
        locationServiceConnection.unbindService();
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.menu_map, menu);
        MenuItem menuItem = menu.findItem(R.id.fueltype);
        getMenuInflater().inflate(R.menu.submenu_fueltypes, menuItem.getSubMenu());

        fuelTypeMenuItem = menu.findItem(R.id.fueltype);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Preselect the menu_list items recorded in Preferences
        int fuelTypeResID = IDUtils.identify(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE), "id", getBaseContext());
        MenuItem fuelType = menu.findItem(fuelTypeResID);
        fuelType.setChecked(true);
        int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
        fuelTypeMenuItem.setIcon(iconID);
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
        handleIntent(getIntent());
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
        final Preferences pref = Preferences.getInstance();
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
                        updateMarkersAndMoveCamera(res, getIntent().getAction());
                    }
                });
    }

    private void handleIntent(final Intent intent) {
        if (intent.getAction() == null) {
            if (cameraPosition != null) {
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            if (stationList != null) {
                Preferences pref = Preferences.getInstance();
                for (Station station : stationList) {
                    //googleMap.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())));
                    double price = station.getPrice(pref.getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice();
                    Marker marker = new Marker(price, station.getLatitude(), station.getLongitude());
                    clusterManager.addItem(marker);
                }
                clusterManager.onCameraIdle();
            }
        } else {
            if (intent.getAction().equals(ACTION_GPS)) {
                if (checkLocationPermission(Constants.PERMISSION_REQUEST_ACCESS_LOCATION)) {
                    newLocationReceiver.register(getBaseContext());
                    if (!locationServiceConnection.isBound()) {
                        locationServiceConnection.bindService();
                    }
                    // If service is not yet bound, the location request will be queued up
                    locationServiceConnection.startLocating();
                }
            } else if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
                String query = intent.getStringExtra(SearchManager.QUERY);

                final Preferences pref = Preferences.getInstance();
                FuelCheckClient client = new FuelCheckClient(getBaseContext());
                client.getFuelPricesForLocation(
                        query,
                        pref.getString(Preferences.Key.SELECTED_SORTBY),
                        pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                        new FuelCheckClient.FuelCheckResponse<List<Station>>() {
                            @Override
                            public void onCompletion(List<Station> res) {
                                updateMarkersAndMoveCamera(res, intent.getAction());
                            }
                        });
            }
        }
    }

    /**
     * To be called when there is a new list of stations that needs to be displayed on the map
     * Old markers will be cleared and new markers will be placed and the camera will move to the new markers.
     *
     * @param res The new list of stations.
     * @param intentAction Indicate the context under which this method is called.
     */
    private void updateMarkersAndMoveCamera(List<Station> res, String intentAction) {
        stationList = res;
        clusterManager.clearItems();
        // NSW is in the Southern Hemisphere, so latitudes are flipped
        double northBound = -Constants.MAX_LATITUDE;
        double southBound = Constants.MIN_LATITUDE;
        double eastBound = Constants.MIN_LONGITUDE;
        double westBound = Constants.MAX_LONGITUDE;
        for (Station station : res) {
            northBound = Math.max(northBound, station.getLatitude());
            southBound = Math.min(southBound, station.getLatitude());
            eastBound = Math.max(eastBound, station.getLongitude());
            westBound = Math.min(westBound, station.getLongitude());
            double price = station.getPrice(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice();
            Marker marker = new Marker(price, station.getLatitude(), station.getLongitude());
            clusterManager.addItem(marker);
        }

        // Move camera to location
        try {
            LatLngBounds bounds = new LatLngBounds(new LatLng(southBound, westBound), new LatLng(northBound, eastBound));
            int padding = 50; // amount of padding in px to apply to the map edges
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            cancelMarkerUpdate = true; // prevent the resulting onCameraIdle of the camera movement from executing
            clusterManager.onCameraIdle();
        } catch (IllegalArgumentException iae) {
            if (intentAction.equals(Intent.ACTION_SEARCH)) {
                Toast.makeText(getBaseContext(), getString(R.string.search_invalid), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialise the GoogleMap that is returned from {@link SupportMapFragment#getMapAsync}
     * All static styling added to the map is done here.
     */
    private void initialiseMap() {

        clusterManager = new ClusterManager<>(getBaseContext(), googleMap);

        // Add styling to googleMaps
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        if (checkLocationPermission(Constants.PERMISSION_REQUEST_ENABLE_MY_LOCATION)) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        googleMap.setMinZoomPreference(Constants.MIN_ZOOM);
        googleMap.setMaxZoomPreference(Constants.MAX_ZOOM);

        // Ensure that the map stays within NSW
        LatLng southWestBound = new LatLng(Constants.SOUTH_BOUND, Constants.WEST_BOUND);
        LatLng northEastBound = new LatLng(Constants.NORTH_BOUND, Constants.EAST_BOUND);
        googleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(southWestBound, northEastBound));

        // Set default camera position to Sydney
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.SYDNEY_LAT, Constants.SYDNEY_LONG), Constants.DEFAULT_ZOOM));

        clusterManager.setRenderer(new ClusterRenderer(getBaseContext(), googleMap, clusterManager));
        googleMap.setOnMarkerClickListener(clusterManager);

        // Update map data upon new camera position
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (cancelMarkerUpdate) {
                    /* If it reaches here, then this might be the initial zoom
                     * Initial zoom is the camera moving to the default camera position
                     * Do not want markers being placed on the default position
                     *
                     * If it reaches here, then this might be invoked from gps fix
                     * The markers have already been calculated and do not need to be re-fetched
                     */
                    cancelMarkerUpdate = false;
                }
                else {
                    FuelCheckClient client = new FuelCheckClient(getBaseContext());
                    client.cancelRequests(new RequestTag(RequestTag.GET_FUELPRICES_WITHIN_RADIUS));
                    final Preferences pref = Preferences.getInstance();
                    double latitude = googleMap.getCameraPosition().target.latitude;
                    double longitude = googleMap.getCameraPosition().target.longitude;
                    int radius = (int) Utils.zoomToRadius(googleMap.getCameraPosition().zoom);
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
                                    clusterManager.clearItems();
                                    for (Station station : res) {
                                        double price = station.getPrice(pref.getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice();
                                        Marker marker = new Marker(price, station.getLatitude(), station.getLongitude());
                                        clusterManager.addItem(marker);
                                    }
                                    clusterManager.onCameraIdle();
                                }
                            });
                }
            }
        });
    }

    private boolean checkLocationPermission(int permissionCode) {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // not granted, ask the user for permission
            ActivityCompat.requestPermissions( this, new String[] {  ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION  },
                    permissionCode);
            return false;
        } else {
            return true;
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_ACCESS_LOCATION:
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
                    Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.PERMISSION_REQUEST_ENABLE_MY_LOCATION:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                } else {
                    // Permission Denied
                    Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Computes the aspect ratio of the map view for any given screen.
     * @return The aspect ratio of the map view.
     */
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

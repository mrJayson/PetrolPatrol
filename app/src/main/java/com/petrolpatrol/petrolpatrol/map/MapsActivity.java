package com.petrolpatrol.petrolpatrol.map;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.details.DetailsActivity;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheck;
import com.petrolpatrol.petrolpatrol.fuelcheck.RequestTag;
import com.petrolpatrol.petrolpatrol.list.ListActivity;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.AverageParcel;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.util.Constants;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, NewLocationReceiver.Listener {

    private static final String TAG = makeLogTag(MapsActivity.class);

    // Most recent action stored, used to identify action when refreshing
    private String action;

    private LocationServiceConnection locationServiceConnection;

    private NewLocationReceiver newLocationReceiver;

    private MenuItem fuelTypeMenuItem;

    private boolean cancelMarkerUpdate;

    // Handle to interact with the google Map UI
    private GoogleMap googleMap;

    // Holds references to all stations loaded in this activity
    private SparseArray<Station> visitedStations;

    // Holds references to the visitedStations currently visible Google Map viewport
    private List<Station> visibleStations;

    private SparseArray<Marker> markerSet;

    private ClusterManager<Marker> clusterManager;

    private String mostRecentQuery;

    private Map<String, Average> averages;

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
        visitedStations = new SparseArray<>();
        markerSet = new SparseArray<>();

        mostRecentQuery = null;

        AverageParcel averageParcel;
        if (savedInstanceState != null) {
            averageParcel = savedInstanceState.getParcelable(AverageParcel.ARG_AVERAGE);
        } else {
            averageParcel = getIntent().getParcelableExtra(AverageParcel.ARG_AVERAGE);
        }
        if (averageParcel != null) {
            averages = averageParcel.getAverages();
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
        int fuelTypeResID = IDUtils.identify(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE), "id", getBaseContext());
        MenuItem fuelType = menu.findItem(fuelTypeResID);
        fuelType.setChecked(true);
        int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
        fuelTypeMenuItem.setIcon(iconID);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.list:
                if (visibleStations != null) {
                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    Bundle bundle = intent.getExtras();
                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    bundle.putParcelableArrayList(ListActivity.ARG_STATIONS, (ArrayList<? extends Parcelable>) visibleStations);

                    switch (action) {
                        case Constants.ACTION_GPS:
                            bundle.putDouble(ListActivity.ARG_LATITUDE, googleMap.getCameraPosition().target.latitude);
                            bundle.putDouble(ListActivity.ARG_LONGITUDE, googleMap.getCameraPosition().target.longitude);
                            bundle.putFloat(ListActivity.ARG_ZOOM, googleMap.getCameraPosition().zoom);
                            break;
                        case Intent.ACTION_SEARCH:
                            bundle.putString(ListActivity.ARG_QUERY, mostRecentQuery);
                            break;
                    }
                    bundle.putParcelable(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
                    intent.putExtras(bundle);
                    // Pass on whatever action this activity is in onto the next for corresponding refresh actions
                    intent.setAction(action);

                    startActivity(intent);
                }
                return true;
            default:
                try {
                    return Utils.fuelTypeSwitch(id, new Utils.Callback() {
                        @Override
                        public void execute() {
                            Preferences pref = Preferences.getInstance(getBaseContext());
                            item.setChecked(true);
                            pref.put(Preferences.Key.SELECTED_FUELTYPE, String.valueOf(item.getTitle()));
                            int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
                            fuelTypeMenuItem.setIcon(iconID);

                            // all markers need to be refreshed, but stations do not have to be wiped
                            // markers will be updated with the new fueltype

                            FuelCheck client = new FuelCheck(getBaseContext());
                            switch (action) {
                                case Constants.ACTION_GPS:
                                    if (googleMap != null && googleMap.getCameraPosition() != null)
                                    client.getFuelPricesWithinRadius(
                                            googleMap.getCameraPosition().target.latitude,
                                            googleMap.getCameraPosition().target.longitude,
                                            (int) Utils.zoomToRadius(googleMap.getCameraPosition().zoom),
                                            pref.getString(Preferences.Key.SELECTED_SORTBY),
                                            pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                                            new FuelCheck.OnResponseListener<List<Station>>() {
                                                @Override
                                                public void onCompletion(List<Station> res) {
                                                    markerSet.clear();
                                                    clusterManager.clearItems();

                                                    updateMarkers(res, false);
                                                    clusterManager.cluster();
                                                }
                                            });

                                    break;
                                case Intent.ACTION_SEARCH:
                                    if (mostRecentQuery != null) {
                                        client.getFuelPricesForLocation(
                                                mostRecentQuery,
                                                pref.getString(Preferences.Key.SELECTED_SORTBY),
                                                pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                                                new FuelCheck.OnResponseListener<List<Station>>() {
                                                    @Override
                                                    public void onCompletion(List<Station> res) {
                                                        markerSet.clear();
                                                        clusterManager.clearItems();
                                                        updateMarkersAndMoveCamera(res, action);
                                                    }
                                                });
                                    }

                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                } catch (NoSuchFieldException e) {
                    return super.onOptionsItemSelected(item);
                }
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
        // Only need one location update
        newLocationReceiver.unregister(getBaseContext());
        locationServiceConnection.stopLocating();

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        // Get fuel data with current location
        FuelCheck client = new FuelCheck(getBaseContext());
        final Preferences pref = Preferences.getInstance(getBaseContext());
        // Map view uses only price sorted list
        client.getFuelPricesWithinRadius(
                latitude,
                longitude,
                pref.getString(Preferences.Key.SELECTED_SORTBY),
                pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                new RequestTag(RequestTag.GET_FUELPRICES_WITHIN_RADIUS),
                new FuelCheck.OnResponseListener<List<Station>>() {
                    @Override
                    public void onCompletion(List<Station> res) {
                        updateMarkersAndMoveCamera(res, getIntent().getAction());
                    }
                });
    }

    private void handleIntent(final Intent intent) {
        action = intent.getAction();
        Preferences pref = Preferences.getInstance(getBaseContext());

        switch (action) {
            case Constants.ACTION_GPS:
                // The GPS locate action is invoked
                if (checkLocationPermission(Constants.PERMISSION_REQUEST_ACCESS_LOCATION)) {
                    newLocationReceiver.register(getBaseContext());
                    if (!locationServiceConnection.isBound()) {
                        locationServiceConnection.bindService();
                    }
                    // If service is not yet bound, the location request will be queued up
                    locationServiceConnection.startLocating();
                }
                break;

            case Intent.ACTION_SEARCH:
                // The search action is invoked
                String query = intent.getStringExtra(SearchManager.QUERY);

                // Capitalise the first letter of each word because the API demands it
                String capitalisedQuery = "";
                for (String word : query.split(" ")) {
                    word = word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase();
                    capitalisedQuery += word + " ";
                }
                capitalisedQuery = capitalisedQuery.trim();
                query = capitalisedQuery;

                // Store query for later use in refreshing the page
                mostRecentQuery = query;

                FuelCheck client = new FuelCheck(getBaseContext());
                client.getFuelPricesForLocation(
                        query,
                        pref.getString(Preferences.Key.SELECTED_SORTBY),
                        pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                        new FuelCheck.OnResponseListener<List<Station>>() {
                            @Override
                            public void onCompletion(List<Station> res) {
                                updateMarkersAndMoveCamera(res, intent.getAction());
                            }
                        });
                break;

            default:
                // No action given, try to restore the activity from a previous state
                if (googleMap.getCameraPosition() != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(googleMap.getCameraPosition()));
                }
                if (visibleStations != null) {
                    updateMarkers(visibleStations, false);
                    clusterManager.onCameraIdle();
                }
                break;
        }
    }

    /**
     * To be called when there is a new list of visitedStations that needs to be displayed on the map
     *
     * @param res The new list of visitedStations.
     * @param intentAction Indicate the context under which this method is called.
     */
    private void updateMarkersAndMoveCamera(List<Station> res, String intentAction) {

        // Move camera to location
        try {
            LatLngBounds bounds = updateMarkers(res, true);
            int padding = 100; // amount of padding in px to apply to the map edges
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            cancelMarkerUpdate = true; // prevent the resulting onCameraIdle of the camera movement from executing
            clusterManager.onCameraIdle();
        } catch (IllegalArgumentException iae) {
            if (intentAction.equals(Intent.ACTION_SEARCH)) {
                Toast.makeText(getBaseContext(), getString(R.string.search_invalid), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private LatLngBounds updateMarkers(List<Station> res, boolean returnBounds) {

        // NSW is in the Southern Hemisphere, so latitudes are flipped
        double northBound = -Constants.MAX_LATITUDE;
        double southBound = Constants.MIN_LATITUDE;
        double eastBound = Constants.MIN_LONGITUDE;
        double westBound = Constants.MAX_LONGITUDE;

        // The received list of visitedStations should correspond to the new camera position and the visible visitedStations
        visibleStations = res;
        for (Station station : res) {
            if (visitedStations.get(station.getId()) == null) {
                // The current station has not been visited before, add to list
                visitedStations.put(station.getId(), station);
            } else {
                // The current station has been visited before, update the associated price
                Price updatePrice = station.getPrice(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE));
                visitedStations.get(station.getId()).setPrice(updatePrice);
            }

            if (markerSet.get(station.getId()) == null) {
                try {
                    double price = station.getPrice(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice();
                    Marker marker = new Marker(price, station.getLatitude(), station.getLongitude(), String.valueOf(station.getId()));
                    clusterManager.addItem(marker);
                    markerSet.put(station.getId(), marker);
                } catch (NullPointerException npe) {
                    // The received list does not have the price that we want, should not have occurred
                    // ignore as missing information does not warrant a marker
                }
            }

            if (returnBounds) {
                northBound = Math.max(northBound, station.getLatitude());
                southBound = Math.min(southBound, station.getLatitude());
                eastBound = Math.max(eastBound, station.getLongitude());
                westBound = Math.min(westBound, station.getLongitude());
            }
        }
        if (returnBounds) {
            return new LatLngBounds(new LatLng(southBound, westBound), new LatLng(northBound, eastBound));
        } else {
            return null;
        }
    }

    /**
     * Initialise the GoogleMap that is returned from {@link SupportMapFragment#getMapAsync}
     * All static styling added to the map is done here.
     */
    private void initialiseMap() {

        clusterManager = new ClusterManager<>(getBaseContext(), googleMap);

        clusterManager.setRenderer(new ClusterRenderer(getBaseContext(), googleMap, clusterManager, averages));
        clusterManager.setAnimation(true);

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

        // Redirect to the Details page when the corresponding marker is clicked
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                try {
                    DetailsActivity.displayDetails(Integer.valueOf(marker.getTitle()), averages, MapsActivity.this);
                } catch (NumberFormatException nfe) {
                    return true;
                }
                return true;
            }
        });

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
                    FuelCheck client = new FuelCheck(getBaseContext());
                    Preferences pref = Preferences.getInstance(getBaseContext());
                    switch (action) {
                        case Constants.ACTION_GPS:
                            client.cancelRequests(new RequestTag(RequestTag.GET_FUELPRICES_WITHIN_RADIUS));
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
                                    new FuelCheck.OnResponseListener<List<Station>>() {
                                        @Override
                                        public void onCompletion(List<Station> res) {
                                            updateMarkers(res, false);
                                            clusterManager.onCameraIdle();
                                        }
                                    });
                            break;
                        case Intent.ACTION_SEARCH:
                            client.getFuelPricesForLocation(
                                    mostRecentQuery,
                                    pref.getString(Preferences.Key.SELECTED_SORTBY),
                                    pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                                    new FuelCheck.OnResponseListener<List<Station>>() {
                                        @Override
                                        public void onCompletion(List<Station> res) {
                                            updateMarkers(res, false);
                                            clusterManager.cluster();
                                        }
                                    });
                            break;
                    }
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

    @Override
    public void startActivity(Intent intent) {
        // Check if search intent
        if (!intent.hasExtra(AverageParcel.ARG_AVERAGE)) {
            intent.putExtra(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }
        super.startActivity(intent);
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

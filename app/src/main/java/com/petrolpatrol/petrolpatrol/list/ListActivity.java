package com.petrolpatrol.petrolpatrol.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.details.DetailsActivity;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.AverageParcel;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.util.Constants;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.List;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.Constants.*;

public class ListActivity extends BaseActivity implements ListAdapter.Listener{

    // Keys for bundles and intents
    public static final String ARG_STATIONS = "ARG_STATIONS";
    public static final String ARG_LATITUDE = "ARG_LATITUDE";
    public static final String ARG_LONGITUDE = "ARG_LONGITUDE";
    public static final String ARG_ZOOM = "ARG_ZOOM";
    public static final String ARG_QUERY = "ARG_QUERY";

    private String action;

    private List<Station> stationList;

    private RecyclerView containerList;

    private ListAdapter adapter;

    private MenuItem fuelTypeMenuItem;

    private double latitude;
    private double longitude;
    private float zoom;

    private String query;

    private Map<String, Average> averages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationList = null;
        AverageParcel averageParcel;
        if (savedInstanceState != null) {
            stationList = savedInstanceState.getParcelableArrayList(ARG_STATIONS);
            averageParcel = savedInstanceState.getParcelable(AverageParcel.ARG_AVERAGE);
        } else {
            stationList = getIntent().getParcelableArrayListExtra(ARG_STATIONS);
            averageParcel = getIntent().getParcelableExtra(AverageParcel.ARG_AVERAGE);
        }
        if (averageParcel != null) {
            averages = averageParcel.getAverages();
        }

        action = getIntent().getAction();

        query = null;

        switch (action) {
            case Constants.ACTION_GPS:
                latitude = getIntent().getDoubleExtra(ARG_LATITUDE, SYDNEY_LAT);
                longitude = getIntent().getDoubleExtra(ARG_LONGITUDE, SYDNEY_LONG);
                zoom = getIntent().getFloatExtra(ARG_ZOOM, DEFAULT_ZOOM);
                break;
            case Intent.ACTION_SEARCH:
                query = getIntent().getStringExtra(ARG_QUERY);
                break;
        }

        getLayoutInflater().inflate(R.layout.activity_list, content);

        containerList = (RecyclerView) findViewById(R.id.container_list_details);

        Preferences pref = Preferences.getInstance(getBaseContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());

        adapter = new ListAdapter(getBaseContext(), stationList, averages, this);
        containerList.setLayoutManager(layoutManager);
        containerList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.menu_list, menu);
        MenuItem menuItem = menu.findItem(R.id.fueltype);
        getMenuInflater().inflate(R.menu.submenu_fueltypes, menuItem.getSubMenu());

        fuelTypeMenuItem = menu.findItem(R.id.fueltype);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Preferences pref = Preferences.getInstance(getBaseContext());
        // Preselect the menu_list items recorded in Preferences
        int fuelTypeResID = IDUtils.identify(pref.getString(Preferences.Key.SELECTED_FUELTYPE), "id", getBaseContext());
        MenuItem fuelType = menu.findItem(fuelTypeResID);
        fuelType.setChecked(true);

        int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
        fuelTypeMenuItem.setIcon(iconID);

        int sortByResID = IDUtils.identify("sort_" + pref.getString(Preferences.Key.SELECTED_SORTBY).toLowerCase(), "id", getBaseContext());
        MenuItem sortBy = menu.findItem(sortByResID);
        sortBy.setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.sort_price:
            case R.id.sort_distance:
                item.setChecked(true);
                Preferences.getInstance(getBaseContext()).put(Preferences.Key.SELECTED_SORTBY, String.valueOf(item.getTitle()));
                adapter.sort(String.valueOf(item.getTitle()));
                return true;
            default:
                try {
                    return Utils.fuelTypeSwitch(id, new Utils.Callback() {
                        @Override
                        public void execute() {
                            item.setChecked(true);
                            Preferences.getInstance(getBaseContext()).put(Preferences.Key.SELECTED_FUELTYPE, String.valueOf(item.getTitle()));
                            int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
                            fuelTypeMenuItem.setIcon(iconID);

                            FuelCheckClient client = new FuelCheckClient(getBaseContext());
                            final Preferences pref = Preferences.getInstance(getBaseContext());
                            switch (action) {
                                case ACTION_GPS:
                                    adapter.clear();
                                    client.getFuelPricesWithinRadius(
                                            latitude,
                                            longitude,
                                            (int) Utils.zoomToRadius(zoom),
                                            pref.getString(Preferences.Key.SELECTED_SORTBY),
                                            pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                                            new FuelCheckClient.FuelCheckResponse<List<Station>>() {
                                                @Override
                                                public void onCompletion(List<Station> res) {
                                                    adapter.updateStations(res);
                                                }
                                            }
                                    );
                                    break;
                                case Intent.ACTION_SEARCH:
                                    adapter.clear();
                                    client.getFuelPricesForLocation(
                                            query,
                                            pref.getString(Preferences.Key.SELECTED_SORTBY),
                                            pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                                            new FuelCheckClient.FuelCheckResponse<List<Station>>() {
                                                @Override
                                                public void onCompletion(List<Station> res) {
                                                    adapter.updateStations(res);
                                                }
                                            });
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
    public void startActivity(Intent intent) {
        // Check if search intent
        if (!intent.hasExtra(AverageParcel.ARG_AVERAGE)) {
            intent.putExtra(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }
        super.startActivity(intent);
    }

    @Override
    public void displayDetails(int stationID) {
        DetailsActivity.displayDetails(stationID, averages, this);
    }
}

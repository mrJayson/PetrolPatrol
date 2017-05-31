package com.petrolpatrol.petrolpatrol.details;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheck;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.AverageParcel;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;

import java.util.List;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class DetailsActivity extends BaseActivity {

    private static final String TAG = makeLogTag(DetailsActivity.class);

    public static final String ARG_STATION_ID = "ARG_STATION_ID";

    private Station station;

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView containerDetailsListView;

    private DetailsAdapter adapter;

    private MenuItem favouritesMenuItem;

    private Map<String, Average> averages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        station = null;
        AverageParcel averageParcel;
        if (savedInstanceState != null) {
            SQLiteClient client = new SQLiteClient(getBaseContext());
            client.open();
            station = client.getStation(savedInstanceState.getInt(ARG_STATION_ID));
            client.close();
            averageParcel = savedInstanceState.getParcelable(AverageParcel.ARG_AVERAGE);
        } else {
            SQLiteClient client = new SQLiteClient(getBaseContext());
            client.open();
            station = client.getStation(getIntent().getIntExtra(ARG_STATION_ID, 0));
            client.close();
            averageParcel = getIntent().getParcelableExtra(AverageParcel.ARG_AVERAGE);
        }
        if (averageParcel != null) {
            averages = averageParcel.getAverages();
        }

        getLayoutInflater().inflate(R.layout.activity_details, content);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.container_swipe_refresh);
        containerDetailsListView = (RecyclerView) findViewById(R.id.container_list_details);
        TextView name = (TextView) findViewById(R.id.details_name);
        name.setText(station.getName());
        TextView address = (TextView) findViewById(R.id.details_address);
        address.setText(station.getAddress());
        ImageView icon = (ImageView) findViewById(R.id.details_brand);
        int res = getBaseContext().getResources().getIdentifier(station.getBrand().getImageName(), "drawable", getBaseContext().getPackageName());
        if (res == 0) {
            // no icon can be found, use Independent as default
            icon.setImageResource(R.drawable.logo_independent);
        } else {
            icon.setImageResource(res);
        }
        LOGE(TAG, String.valueOf(res));

        // assign the adapter
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        adapter = new DetailsAdapter(getBaseContext(), averages);
        containerDetailsListView.setLayoutManager(layoutManager);
        containerDetailsListView.setAdapter(adapter);

        // Using the station ID, retrieve the prices for the station
        retrievePrices();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // clear the list on refresh because to convey that an update is occurring
                // Without clearing, it looks like nothing is happening
                adapter.clear();
                retrievePrices();
            }
        });
    }

    private void retrievePrices() {
        swipeContainer.setRefreshing(true);
        FuelCheck client = new FuelCheck(getBaseContext());
        client.getFuelPricesForStation(station.getId(), new FuelCheck.OnResponseListener<List<Price>>() {
            @Override
            public void onCompletion(List<Price> res) {
                adapter.updatePrices(res);
                swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.menu_details, menu);
        favouritesMenuItem = menu.findItem(R.id.favourite);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Preferences pref = Preferences.getInstance(getBaseContext());
        List<Integer> favourites = pref.getFavourites();
        if (favourites.contains(station.getId())) {
            favouritesMenuItem.setIcon(R.drawable.ic_favorite_white_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.favourite:
                if (station != null) {
                    boolean toggled = toggleFavourites(station.getId());
                    if (toggled) {
                        // set into enabled state
                        item.setIcon(R.drawable.ic_favorite_white_24dp);
                    } else {
                        // set into un-enabled state
                        item.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean toggleFavourites(int stationID) {
        Preferences pref = Preferences.getInstance(getBaseContext());
        List<Integer> favourites = pref.getFavourites();
        boolean toggleStatus;
        if (favourites.contains(stationID)) {
            // if it already contains, then remove
            favourites.remove(Integer.valueOf(stationID)); // need to be explicit since it might remove by index instead
            toggleStatus = false;
        } else {
            favourites.add(stationID);
            toggleStatus = true;
        }
        pref.putFavourites(favourites);
        return toggleStatus;
    }

    public static void displayDetails(int stationID, Map<String, Average> averages, Activity sourceActivity) {
        Intent intent = new Intent(sourceActivity.getApplicationContext(), DetailsActivity.class);
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(DetailsActivity.ARG_STATION_ID, stationID);
        if (averages != null) {
            bundle.putParcelable(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }
        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        // Check if search intent
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            intent.putExtra(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }

        super.startActivity(intent);
    }
}

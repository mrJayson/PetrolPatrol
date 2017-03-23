package com.petrolpatrol.petrolpatrol.details;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;

import java.util.List;

public class DetailsActivity extends BaseActivity {

    public static final String ARG_STATION_ID = "ARG_STATION_ID";

    private Station station;

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView containerDetailsListView;

    private DetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        station = null;
        if (savedInstanceState != null) {
            SQLiteClient client = new SQLiteClient(getBaseContext());
            client.open();
            station = client.getStation(savedInstanceState.getInt(ARG_STATION_ID));
            client.close();
        } else {
            SQLiteClient client = new SQLiteClient(getBaseContext());
            client.open();
            station = client.getStation(getIntent().getIntExtra(ARG_STATION_ID, 0));
            client.close();
        }

        getLayoutInflater().inflate(R.layout.activity_details, content);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.container_swipe_refresh);
        containerDetailsListView = (RecyclerView) findViewById(R.id.container_list_list);
        TextView name = (TextView) findViewById(R.id.details_name);
        name.setText(station.getName());
        TextView address = (TextView) findViewById(R.id.details_address);
        address.setText(station.getAddress());

        // assign the adapter
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        adapter = new DetailsAdapter(getBaseContext());
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
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void retrievePrices() {
        FuelCheckClient client = new FuelCheckClient(getBaseContext());
        client.getFuelPricesForStation(station.getId(), new FuelCheckClient.FuelCheckResponse<List<Price>>() {
            @Override
            public void onCompletion(List<Price> res) {
                adapter.updatePrices(res);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

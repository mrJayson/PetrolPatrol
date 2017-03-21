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
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.List;

public class ListActivity extends BaseActivity implements ListAdapter.Listener{

    // Keys for bundles and intents
    public static final String ARG_STATIONS = "ARG_STATIONS";

    private List<Station> stationList;

    private RecyclerView containerList;

    private MenuItem fuelTypeMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationList = null;
        if (savedInstanceState != null) {
            stationList = savedInstanceState.getParcelableArrayList(ARG_STATIONS);
        } else {
            stationList = getIntent().getParcelableArrayListExtra(ARG_STATIONS);
        }

        getLayoutInflater().inflate(R.layout.activity_list, content);

        containerList = (RecyclerView) findViewById(R.id.container_details_list);

        Preferences pref = Preferences.getInstance();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        RecyclerView.Adapter adapter = new ListAdapter(getBaseContext(), stationList, pref.getString(Preferences.Key.SELECTED_FUELTYPE), this);
        containerList.setLayoutManager(layoutManager);
        containerList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        MenuItem menuItem = menu.findItem(R.id.fueltype);
        getMenuInflater().inflate(R.menu.submenu_fueltypes, menuItem.getSubMenu());

        fuelTypeMenuItem = menu.findItem(R.id.fueltype);

        Preferences pref = Preferences.getInstance();
        // Preselect the menu_list items recorded in Preferences
        int fuelTypeResID = Utils.identify(pref.getString(Preferences.Key.SELECTED_FUELTYPE), "id", getBaseContext());
        MenuItem fuelType = menu.findItem(fuelTypeResID);
        fuelType.setChecked(true);
        int iconID = Utils.identify(Utils.fuelTypeToIconName(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
        fuelTypeMenuItem.setIcon(iconID);

        int sortByResID = Utils.identify("sort_" + pref.getString(Preferences.Key.SELECTED_SORTBY).toLowerCase(), "id", getBaseContext());
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
                Preferences.getInstance().put(Preferences.Key.SELECTED_SORTBY, String.valueOf(item.getTitle()));
                return true;
            default:
                try {
                    return Utils.fuelTypeSwitch(id, new Utils.Callback() {
                        @Override
                        public void execute() {
                            item.setChecked(true);
                            Preferences.getInstance().put(Preferences.Key.SELECTED_FUELTYPE, String.valueOf(item.getTitle()));
                            int iconID = Utils.identify(Utils.fuelTypeToIconName(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
                            fuelTypeMenuItem.setIcon(iconID);
                        }
                    });
                } catch (NoSuchFieldException e) {
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    @Override
    public void displayDetails(int stationID) {
        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(DetailsActivity.ARG_STATION_ID, stationID);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}

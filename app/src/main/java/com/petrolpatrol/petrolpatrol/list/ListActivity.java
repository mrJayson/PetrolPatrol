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
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.List;

public class ListActivity extends BaseActivity implements ListAdapter.Listener{

    // Keys for bundles and intents
    public static final String ARG_STATIONS = "ARG_STATIONS";

    private List<Station> stationList;

    private RecyclerView containerList;

    private ListAdapter adapter;

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

        containerList = (RecyclerView) findViewById(R.id.container_list_list);

        Preferences pref = Preferences.getInstance();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());

        adapter = new ListAdapter(getBaseContext(), stationList, pref.getString(Preferences.Key.SELECTED_FUELTYPE), this);
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

        Preferences pref = Preferences.getInstance();
        // Preselect the menu_list items recorded in Preferences
        int fuelTypeResID = IDUtils.identify(pref.getString(Preferences.Key.SELECTED_FUELTYPE), "id", getBaseContext());
        MenuItem fuelType = menu.findItem(fuelTypeResID);
        fuelType.setChecked(true);

        int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
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
                Preferences.getInstance().put(Preferences.Key.SELECTED_SORTBY, String.valueOf(item.getTitle()));
                adapter.sort(String.valueOf(item.getTitle()));
                return true;
            default:
                try {
                    return Utils.fuelTypeSwitch(id, new Utils.Callback() {
                        @Override
                        public void execute() {
                            item.setChecked(true);
                            Preferences.getInstance().put(Preferences.Key.SELECTED_FUELTYPE, String.valueOf(item.getTitle()));
                            int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
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
        DetailsActivity.displayDetails(stationID, this);
    }
}

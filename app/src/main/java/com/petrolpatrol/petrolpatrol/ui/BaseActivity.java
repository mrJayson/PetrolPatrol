package com.petrolpatrol.petrolpatrol.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.datastore.SharedPreferences;
import com.petrolpatrol.petrolpatrol.locate.ListFragment;
import com.petrolpatrol.petrolpatrol.locate.LocateFragment;
import com.petrolpatrol.petrolpatrol.model.FuelType;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationService;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.util.Constants;

import java.util.Iterator;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity implements LocateFragment.Listener, ListFragment.Listener {

    private static final String TAG = makeLogTag(BaseActivity.class);

    private LocationServiceConnection mLocationServiceConnection;

    private FragmentManager mfragmentManager = getSupportFragmentManager();

    private String selectedFuelType;
    private String selectedSortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocationServiceConnection = new LocationServiceConnection(this);

        if (requestLocationPermissionsIfNecessary()) {
            bindToLocationService();
        }

        selectedFuelType = SharedPreferences.getInstance().getString(SharedPreferences.Key.DEFAULT_FUELTYPE);
        selectedSortBy = SharedPreferences.getInstance().getString(SharedPreferences.Key.DEFAULT_SORTBY);

        // Show the locate fragment each time on start up
        displayLocateFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindFromLocationService();
        super.onStop();
    }

    // Build the Toolbar menu upon first start
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Menu items will be displayed in the order they are declared in code
        int menuOrdering = 0;

        // Placeholders while creating the menu
        MenuItem menuItem;
        SubMenu subMenu;

        SQLiteClient sqLiteClient = new SQLiteClient(getApplicationContext());

        /*
         * Search button
         */

        menuItem = menu.add(Menu.NONE, R.id.id_menu_search, Menu.FIRST + menuOrdering++, R.string.menu_search);
        menuItem.setIcon(R.drawable.ic_search_white_24dp);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        /*
         * FuelTypes submenu
         */

        subMenu = menu.addSubMenu(R.id.id_group_fueltype, Menu.NONE, Menu.FIRST + menuOrdering++, R.string.menu_fueltype);

        menuItem = subMenu.getItem();
        menuItem.setIcon(R.drawable.ic_local_gas_station_white_24dp);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        // Populate the submenu from database
        sqLiteClient.open();
        List<FuelType> fuelTypes =  sqLiteClient.getAllFuelTypes();
        sqLiteClient.close();

        for (FuelType fuelType : fuelTypes) {
            menuItem = subMenu.add(
                    R.id.id_group_fueltype,
                    Menu.NONE,
                    Menu.FIRST + menuOrdering++,
                    fuelType.getCode()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    menuItem.setChecked(true);
                    SharedPreferences.getInstance().put(SharedPreferences.Key.DEFAULT_FUELTYPE, String.valueOf(menuItem.getTitle()));
                    return true;
                }
            });

            if (SharedPreferences.getInstance().getString(SharedPreferences.Key.DEFAULT_FUELTYPE).equals(fuelType.getCode())) {
                menuItem.setChecked(true);
            }
        }
        subMenu.setGroupCheckable(R.id.id_group_fueltype, true, true);

        /*
         * SortBy submenu
         */

        subMenu = menu.addSubMenu(R.id.id_menu_sort, Menu.NONE, Menu.FIRST + menuOrdering++, R.string.menu_sort);

        menuItem = subMenu.getItem();
        menuItem.setIcon(R.drawable.ic_sort_white_24dp);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menuItem = subMenu.add(R.id.id_menu_sort, R.id.id_menu_sort_price, Menu.FIRST + menuOrdering++, R.string.menu_sort_price);
        menuItem.setIcon(R.drawable.ic_attach_money_black_24dp);
        if (SharedPreferences.getInstance().getString(SharedPreferences.Key.DEFAULT_SORTBY).equals(getApplicationContext().getString(R.string.menu_sort_price))) {
            menuItem.setChecked(true);
        }

        menuItem = subMenu.add(R.id.id_menu_sort, R.id.id_menu_sort_distance, Menu.FIRST + menuOrdering++, R.string.menu_sort_distance);
        menuItem.setIcon(R.drawable.ic_directions_black_24dp);
        if (SharedPreferences.getInstance().getString(SharedPreferences.Key.DEFAULT_SORTBY).equals(getApplicationContext().getString(R.string.menu_sort_distance))) {
            menuItem.setChecked(true);
        }

        subMenu.setGroupCheckable(R.id.id_menu_sort, true, true);

        // Settings menuItem
//        menuItem = menu.add(Menu.NONE, R.id.id_menu_settings, Menu.FIRST + menuOrdering++, R.string.menu_settings);
//        menuItem.setIcon(R.drawable.ic_settings_white_24dp);
//        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.id_menu_settings:
                return true;
            case R.id.id_menu_sort_price:
            case R.id.id_menu_sort_distance:
                item.setChecked(true);
                SharedPreferences.getInstance().put(SharedPreferences.Key.DEFAULT_SORTBY, String.valueOf(item.getTitle()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void bindToLocationService() {
        mLocationServiceConnection.bindService();
    }

    private void unbindFromLocationService() {
        mLocationServiceConnection.unbindService();
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
                    bindToLocationService();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Permission denied, location features not enabled", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Debug purposes
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fragment callback methods
     */

    @Override
    public void startLocating() {
        mLocationServiceConnection.startLocating();
    }

    @Override
    public void stopLocating() {
        mLocationServiceConnection.stopLocating();
    }

    private void displayLocateFragment() {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        LocateFragment locateFragment = LocateFragment.newInstance();
        transaction.replace(R.id.fragment_container, locateFragment);
        transaction.commit();
    }

    @Override
    public void displayListFragment(List<Station> list) {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        ListFragment listFragment = ListFragment.newInstance(list);
        transaction.replace(R.id.fragment_container, listFragment);
        transaction.commit();
    }

    @Override
    public String getSelectedFuelType() {
        return selectedFuelType;
    }

    @Override
    public String getSelectedSortBy() {
        return selectedSortBy;
    }

}

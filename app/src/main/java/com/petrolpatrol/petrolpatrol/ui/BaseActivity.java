package com.petrolpatrol.petrolpatrol.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.details.DetailsFragment;
import com.petrolpatrol.petrolpatrol.list.ListFragment;
import com.petrolpatrol.petrolpatrol.map.MapFragment;
import com.petrolpatrol.petrolpatrol.trend.TrendFragment;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.util.Constants;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity
        implements TrendFragment.Listener, ListFragment.Listener,
        DetailsFragment.Listener, MapFragment.Listener {

    private static final String TAG = makeLogTag(BaseActivity.class);

    private LocationServiceConnection mLocationServiceConnection;

    private FragmentManager mfragmentManager = getSupportFragmentManager();

    private DrawerLayout mDrawerContainer;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mDrawerContainer = (DrawerLayout) findViewById(R.id.drawer_container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Navigation Drawer interaction from the toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLocationServiceConnection = new LocationServiceConnection(this);

        // Show the trend fragment each time on start up
        displayTrendFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (requestLocationPermissionsIfNecessary()) {
            bindToLocationService();
        }
    }

    @Override
    protected void onStop() {
        unbindFromLocationService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mDrawerContainer.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isNavDrawerOpen() {
        return mDrawerContainer != null && mDrawerContainer.isDrawerOpen(GravityCompat.START);
    }

    private void closeNavDrawer() {
        if (mDrawerContainer != null) {
            mDrawerContainer.closeDrawer(GravityCompat.START);
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
     * Fragments callback methods
     */

    @Override
    public void startLocating() {
        mLocationServiceConnection.startLocating();
    }

    @Override
    public void stopLocating() {
        mLocationServiceConnection.stopLocating();
    }

    private void displayTrendFragment() {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        TrendFragment trendFragment = TrendFragment.newInstance();
        transaction.replace(R.id.fragment_container, trendFragment);
        transaction.commit();
    }

    @Override
    public void displayMapFragment() {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        MapFragment mapFragment = MapFragment.newInstance(new Action(Action.FIND_BY_GPS));
        transaction.replace(R.id.fragment_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void displayListFragment(List<Station> list) {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        ListFragment listFragment = ListFragment.newInstance(list);
        transaction.replace(R.id.fragment_container, listFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void displayDetailsFragment(int stationID) {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        DetailsFragment detailsFragment = DetailsFragment.newInstance(stationID);
        transaction.replace(R.id.fragment_container, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}

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
import android.widget.FrameLayout;
import android.widget.Toast;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.locate.ListFragment;
import com.petrolpatrol.petrolpatrol.locate.LocateFragment;
import com.petrolpatrol.petrolpatrol.service.LocationService;
import com.petrolpatrol.petrolpatrol.service.LocationServiceConnection;
import com.petrolpatrol.petrolpatrol.util.Constants;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity implements LocateFragment.Listener, ListFragment.Listener {

    private static final String TAG = makeLogTag(BaseActivity.class);

    private LocationServiceConnection mLocationServiceConnection;

    private FragmentManager mfragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mLocationServiceConnection = new LocationServiceConnection(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (requestLocationPermissionsIfNecessary()) {
            bindToLocationService();
        }
        // Show the locate fragment each time on start up
        displayLocateFragment();
    }

    @Override
    protected void onStop() {
        unbindFromLocationService();
        super.onStop();
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
     * Listener for the {@link LocateFragment} fragment
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
    public void displayListFragment() {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();
        ListFragment listFragment = ListFragment.newInstance("","");
        transaction.replace(R.id.fragment_container, listFragment);
        transaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

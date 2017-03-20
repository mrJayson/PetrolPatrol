package com.petrolpatrol.petrolpatrol.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.trend.TrendActivity;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = makeLogTag(TrendActivity.class);

    private DrawerLayout drawer;
    private Toolbar toolbar;
    protected FrameLayout content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Navigation Drawer interaction from the toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        content = (FrameLayout) findViewById(R.id.fragment_container);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
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
        return drawer != null && drawer.isDrawerOpen(GravityCompat.START);
    }

    private void closeNavDrawer() {
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
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
}

package com.petrolpatrol.petrolpatrol.home;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheck;
import com.petrolpatrol.petrolpatrol.home.fragment.favourite.FavouriteFragment;
import com.petrolpatrol.petrolpatrol.home.fragment.trend.TrendFragment;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.AverageParcel;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.HashMap;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class HomeActivity extends BaseActivity implements TrendFragment.OnFragmentInteractionListener, FavouriteFragment.OnFragmentInteractionListener{

    private static final String TAG = makeLogTag(HomeActivity.class);

    private PagerAdapter pagerAdapter;

    private ViewPager mViewPager;

    private MenuItem fuelTypeMenuItem;

    private Map<String, Average> averages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise UI components
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        averages = new HashMap<>();

        // Handle savedInstanceState
        if (savedInstanceState != null) {
            AverageParcel averageParcel = savedInstanceState.getParcelable(AverageParcel.ARG_AVERAGE);
            if (averageParcel != null) {
                averages = averageParcel.getAverages();
            }
        }

        if (averages == null || averages.isEmpty()) {
            FuelCheck client = new FuelCheck(getBaseContext());
            client.getAverages(new FuelCheck.OnResponseListener<Map<String, Average>>() {
                @Override
                public void onCompletion(Map<String, Average> res) {
                    averages = res;
                    // Pass this data onto fragments
                    if (pagerAdapter != null) {
                        pagerAdapter.getTrendFragment().updateAverages(averages);
                        pagerAdapter.getFavouriteFragment().updateAverages(averages);
                    }
                }
            });
        }

        // Initialise fragment management
        if (averages == null || averages.isEmpty()) {
            pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        } else {
            pagerAdapter = new PagerAdapter(getSupportFragmentManager(), averages);
        }

        // Set up the ViewPager with the adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(pagerAdapter);

        // Set up tabs with the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
        try {
            return Utils.fuelTypeSwitch(id, new Utils.Callback() {
                @Override
                public void execute() {
                    item.setChecked(true);
                    Preferences.getInstance(getBaseContext()).put(Preferences.Key.SELECTED_FUELTYPE, String.valueOf(item.getTitle()));
                    int iconID = IDUtils.identify(Utils.fuelTypeToIconName(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE)), "drawable", getBaseContext());
                    fuelTypeMenuItem.setIcon(iconID);
                    pagerAdapter.getTrendFragment().refresh();
                    pagerAdapter.getFavouriteFragment().refresh();

                }
            });
        } catch (NoSuchFieldException e) {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (averages != null) {
            outState.putParcelable(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void startActivity(Intent intent) {
        // Check if search intent
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
                if (!averages.isEmpty()) {
                    intent.putExtra(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
                }
            }
        }
        super.startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

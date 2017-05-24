package com.petrolpatrol.petrolpatrol.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.petrolpatrol.petrolpatrol.home.fragment.favourite.FavouriteFragment;
import com.petrolpatrol.petrolpatrol.home.fragment.trend.TrendFragment;
import com.petrolpatrol.petrolpatrol.model.Average;

import java.util.Map;

class PagerAdapter extends FragmentPagerAdapter {

    private final TrendFragment trendFragment;
    private final FavouriteFragment favouriteFragment;

    PagerAdapter(FragmentManager fm) {
        super(fm);
        trendFragment = TrendFragment.newInstance();
        favouriteFragment = FavouriteFragment.newInstance();
    }

    PagerAdapter(FragmentManager fm, Map<String, Average> averages) {
        super(fm);
        if (averages == null) {
            trendFragment = TrendFragment.newInstance();
            favouriteFragment = FavouriteFragment.newInstance();
        } else {
            trendFragment = TrendFragment.newInstance(averages);
            favouriteFragment = FavouriteFragment.newInstance(averages);
        }
    }

    TrendFragment getTrendFragment() {
        return trendFragment;
    }

    FavouriteFragment getFavouriteFragment() {
        return favouriteFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return trendFragment;
            case 1:
                return favouriteFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Trends";
            case 1:
                return "Favourites";
        }
        return null;
    }
}
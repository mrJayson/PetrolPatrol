package com.petrolpatrol.petrolpatrol.trend;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;

import java.util.ArrayList;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrendFragment extends Fragment implements NewLocationReceiver.Listener {

    private static final String TAG = makeLogTag(TrendFragment.class);
    private Listener parentListener;
    private NewLocationReceiver newLocationReceiver;

    private List<TrendData> dataWeek;
    private List<TrendData> dataMonth;
    private List<TrendData> dataYear;

    private FrameLayout chartContainer;
    private LineChart chartWeek;
    private LineChart chartMonth;
    private LineChart chartYear;

    private TrendResolution selectedResolution;

    //private LineChart chart;

    public static TrendFragment newInstance() {
        // Factory method for creating new fragment instances
        // automatically takes parameters and stores in a bundle for later use in onCreate
        TrendFragment fragment = new TrendFragment();
        Bundle bundle = new Bundle();
        //TODO change to parcelable if there are any args at all
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGI(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // No previous state to restore from
        } else {
            // previous state contains data needed to recreate fragment to how it was before
        }
        newLocationReceiver = new NewLocationReceiver(this);

        setHasOptionsMenu(true);

        dataWeek = new ArrayList<TrendData>();
        dataMonth = new ArrayList<TrendData>();
        dataYear = new ArrayList<TrendData>();

        selectedResolution = TrendResolution.WEEK;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trend, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.E10:
            case R.id.U91:
            case R.id.E85:
            case R.id.P95:
            case R.id.P98:
            case R.id.DL:
            case R.id.PDL:
            case R.id.B20:
            case R.id.LPG:
            case R.id.CNG:
            case R.id.LNG:
            case R.id.EV:
            case R.id.H2:
                item.setChecked(true);
                //selectedFuelType = String.valueOf(item.getTitle());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chartContainer = (FrameLayout) view.findViewById(R.id.container_trend_chart);

        chartWeek = new LineChart(getContext());
        chartWeek.setId(R.id.chart_week);
        chartWeek.setVisibility(View.GONE);
        chartContainer.addView(chartWeek);

        chartMonth = new LineChart(getContext());
        chartMonth.setId(R.id.chart_month);
        chartMonth.setVisibility(View.GONE);
        chartContainer.addView(chartMonth);

        chartYear = new LineChart(getContext());
        chartYear.setId(R.id.chart_year);
        chartYear.setVisibility(View.GONE);
        chartContainer.addView(chartYear);

        retrieveTrendsData(parentListener.getSelectedFuelType());

        TabLayout tab = (TabLayout) view.findViewById(R.id.chart_tab);
        for (TrendResolution trend : TrendResolution.values()) {
            tab.addTab(tab.newTab().setText(trend.getHandle()));
        }
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedResolution = TrendResolution.toEnum(String.valueOf(tab.getText()));
                makeChartVisible(selectedResolution);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        FloatingActionButton locateFab = (FloatingActionButton) view.findViewById(R.id.locate_fab);
        locateFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                registerReceiverToLocationService();
                parentListener.startLocating();
            }
        });
    }

    private void makeChartVisible(TrendResolution resolution) {
        switch (resolution) {
            case WEEK:
                chartWeek.setVisibility(View.VISIBLE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.GONE);
                break;
            case MONTH:
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.VISIBLE);
                chartYear.setVisibility(View.GONE);
                break;
            case YEAR:
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void retrieveTrendsData(String fuelType) {
        FuelCheckClient client = new FuelCheckClient(getContext());
        client.getTrend(fuelType, new FuelCheckClient.FuelCheckResponse<List<TrendData>>() {
            @Override
            public void onCompletion(List<TrendData> res) {
                dataWeek.clear();
                dataMonth.clear();
                dataYear.clear();

                for (TrendData data : res) {
                    switch (TrendResolution.toEnum(data.getPeriod())) {
                        case WEEK:
                            dataWeek.add(data);
                            break;
                        case MONTH:
                            dataMonth.add(data);
                            break;
                        case YEAR:
                            dataYear.add(data);
                    }
                }
                chartWeek = drawChart(dataWeek, TrendResolution.WEEK, chartWeek);
                chartMonth = drawChart(dataMonth, TrendResolution.MONTH, chartMonth);
                chartYear = drawChart(dataYear, TrendResolution.YEAR, chartYear);

                makeChartVisible(selectedResolution);
            }
        });
    }

    private LineChart drawChart(List<TrendData> dataList, TrendResolution resolution, LineChart chart) {
        // Remove existing chart if there is one
        if (chart != null) {
            chartContainer.removeView(chart);
        }
        chart = new LineChart(getContext());
        switch (resolution) {
            case WEEK:
                chart.setId(R.id.chart_week);
                break;
            case MONTH:
                chart.setId(R.id.chart_month);
                break;
            case YEAR:
                chart.setId(R.id.chart_year);
                break;
        }
        chart.setVisibility(View.GONE);
        chartContainer.addView(chart);

        List<Entry> data = new ArrayList<Entry>();
        List<String> xLabels = new ArrayList<String>();
        int i = 1;
        for (TrendData trendData : dataList) {
            if (trendData.getPeriod().equals(resolution.getHandle())) {
                try {
                    data.add(
                            new Entry((float) i++
                                    , (float) trendData.getPrice()));
                    xLabels.add(trendData.getCaptured());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Format the X axis labels to something more user-friendly
        IAxisValueFormatter formatter = null;
        switch (resolution) {
            case WEEK:
                formatter = new XAxisWeekValueFormatter(xLabels);
                break;
            case MONTH:
                //TODO Month view is still too cluttered, need to fix
                formatter = new XAxisMonthValueFormatter(xLabels);
                break;
            case YEAR:
                formatter = new XAxisYearValueFormatter(xLabels);
                break;
        }
        if (formatter != null) {
            chart.getXAxis().setValueFormatter(formatter);
        }

        // Format how the chart line looks
        LineDataSet dataSet = new LineDataSet(data, "Price");
        dataSet.setValueTextSize(10);
        dataSet.setCircleRadius(6);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        if (resolution != TrendResolution.MONTH) {
            chart.setTouchEnabled(false);
            chart.setHorizontalScrollBarEnabled(true);
        }
        chart.getLegend().setEnabled(false);

        // Remove the chart description
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        // Add padding inside the chart
        chart.setExtraLeftOffset(20);
        chart.setExtraRightOffset(20);
        chart.setExtraBottomOffset(20);

        // Remove axis decorations
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawLabels(false);

        // Position the X Axis at the bottom of the chart
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.invalidate();

        return chart;
    }

    @Override
    public void onStart() {
        LOGI(TAG, "onStart");
        super.onStart();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        LOGI(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LOGI(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        LOGI(TAG, "onStop");
        // Unregister if fragment closes while still broadcast receiving
        unregisterReceiverFromLocationService();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        LOGI(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLocationReceived(Location location) {

        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        FuelCheckClient client = new FuelCheckClient(getContext());

        client.getFuelPricesWithinRadius(location.getLatitude(), location.getLongitude(), parentListener.getSelectedSortBy(), parentListener.getSelectedFuelType(), new FuelCheckClient.FuelCheckResponse<List<Station>>() {
            @Override
            public void onCompletion(List<Station> res) {
                parentListener.displayListFragment(res);
            }
        });
    }

    private void registerReceiverToLocationService() {
        newLocationReceiver.register(getContext());
    }

    private void unregisterReceiverFromLocationService() {
        newLocationReceiver.unregister(getContext());
    }

    public interface Listener {
        void startLocating();
        void stopLocating();
        void displayListFragment(List<Station> list);
        String getSelectedFuelType();
        String getSelectedSortBy();
    }

    @Override
    public void onAttach(Context context) {
        // Context is the parent activity
        LOGI(TAG, "onAttach");
        super.onAttach(context);

        // Check to see if the parent activity has implemented the callback
        if (context instanceof Listener) {
            parentListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        LOGI(TAG, "onDetach");
        super.onDetach();
        parentListener = null;
    }

}

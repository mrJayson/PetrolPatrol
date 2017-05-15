package com.petrolpatrol.petrolpatrol.home;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.map.MapsActivity;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.AverageParcel;
import com.petrolpatrol.petrolpatrol.ui.BaseActivity;
import com.petrolpatrol.petrolpatrol.util.Constants;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class HomeActivity extends BaseActivity {

    private static final String TAG = makeLogTag(HomeActivity.class);

    private Map<String, Average> averages;

    private List<Trend> dataWeek;
    private List<Trend> dataMonth;
    private List<Trend> dataYear;

    private FrameLayout chartContainer;
    private LineChart chartWeek;
    private LineChart chartMonth;
    private LineChart chartYear;

    private String chartFuelType;

    private ChartResolution chartResolution;

    private RecyclerView containerFavouritesListView;
    private FavouritesAdapter adapter;

    private MenuItem fuelTypeMenuItem;
    private TextView averageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_home, content);

        averageTextView = (TextView) findViewById(R.id.today_price);

        chartContainer = (FrameLayout) findViewById(R.id.container_chart);

        containerFavouritesListView = (RecyclerView) findViewById(R.id.container_list_favourites);

        averages = new HashMap<>();
        dataWeek = new ArrayList<>();
        dataMonth = new ArrayList<>();
        dataYear = new ArrayList<>();
        chartResolution = ChartResolution.WEEK; // The week tab is default

        // Initialise the tab UI functionality of the charts
        TabLayout tab = (TabLayout) findViewById(R.id.tab_chart);
        for (ChartResolution trend : ChartResolution.values()) {
            tab.addTab(tab.newTab().setText(trend.getHandle()));
        }
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                chartResolution = ChartResolution.toEnum(String.valueOf(tab.getText()));
                updateVisibility(chartResolution);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Initialise the listener of the fab
        FloatingActionButton locateFab = (FloatingActionButton) findViewById(R.id.fab_gps);
        locateFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.setAction(Constants.ACTION_GPS);
                intent.putExtra(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
                startActivity(intent);
            }
        });

        if (averages.isEmpty()) {
            FuelCheckClient client = new FuelCheckClient(getBaseContext());
            client.getAverages(new FuelCheckClient.FuelCheckResponse<Map<String, Average>>() {
                @Override
                public void onCompletion(Map<String, Average> res) {
                    averages = res;
                    // Display the current averageTextView
                    updateAverage(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE));

                    // assign the adapter
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
                    adapter = new FavouritesAdapter(averages, getBaseContext());
                    adapter.setEmptyView(findViewById(R.id.empty_view_favourites));
                    containerFavouritesListView.setLayoutManager(layoutManager);
                    containerFavouritesListView.setAdapter(adapter);
                }
            });
        }
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
    protected void onStart() {
        super.onStart();

        invalidateOptionsMenu();

        // Initialise or update the line charts
        if (!Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE).equals(chartFuelType) ||
                dataWeek.isEmpty() || dataMonth.isEmpty() || dataYear.isEmpty()) {
            retrieveTrendsData(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE));
        } else {
            // If data already exists, no need to re-fetch
            if (!dataWeek.isEmpty()) {
                chartWeek = drawChart(dataWeek, ChartResolution.WEEK, chartWeek);
            }
            if (!dataMonth.isEmpty()) {
                chartMonth = drawChart(dataMonth, ChartResolution.MONTH, chartMonth);
            }
            if (!dataYear.isEmpty()) {
                chartYear = drawChart(dataYear, ChartResolution.YEAR, chartYear);
            }
            updateVisibility(chartResolution);
        }

        if (adapter != null) {
            adapter.refresh();
        }
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
                    retrieveTrendsData(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE));
                    updateAverage(Preferences.getInstance(getBaseContext()).getString(Preferences.Key.SELECTED_FUELTYPE));
                    adapter.refresh();

                }
            });
        } catch (NoSuchFieldException e) {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Requests current trend data from the FuelCheck API to display on the UI.
     * @param fuelType Trend data can only be shown for one fuel type at a time,
     *                 this parameter selects which fuel type trend data to retrieve.
     */
    private void retrieveTrendsData(final String fuelType) {
        FuelCheckClient client = new FuelCheckClient(getBaseContext());
        client.getTrend(fuelType, new FuelCheckClient.FuelCheckResponse<List<Trend>>() {
            @Override
            public void onCompletion(List<Trend> res) {
                dataWeek.clear();
                dataMonth.clear();
                dataYear.clear();
                chartFuelType = fuelType; // Keep track of which fuel type is currently being displayed

                for (Trend data : res) {
                    switch (ChartResolution.toEnum(data.getPeriod())) {
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
                if (!dataWeek.isEmpty()) {
                    chartWeek = drawChart(dataWeek, ChartResolution.WEEK, chartWeek);
                }
                if (!dataMonth.isEmpty()) {
                    chartMonth = drawChart(dataMonth, ChartResolution.MONTH, chartMonth);
                }
                if (!dataYear.isEmpty()) {
                    chartYear = drawChart(dataYear, ChartResolution.YEAR, chartYear);
                }
                updateVisibility(chartResolution);
            }
        });
    }

    /**
     * Draws a line chart view to be displayed on the UI.
     * @param dataList The list of data points to be charted.
     * @param resolution The resolution of this chart is used to distinguish between other existing charts on display.
     * @param chart The currently existing line chart so that it may be overridden.
     * @return The updated line chart
     */
    private LineChart drawChart(List<Trend> dataList, ChartResolution resolution, LineChart chart) {
        // Remove existing chart if there is one
        if (chart != null) {
            chartContainer.removeView(chart);
        }
        chart = new LineChart(getBaseContext());
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

        List<Entry> data = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        int i = 1; // Start at 1 so that x axis drawing will not be off
        for (Trend trend : dataList) {
            if (trend.getPeriod().equals(resolution.getHandle())) {
                try {
                    data.add(
                            new Entry((float) i++
                                    , (float) trend.getPrice()));
                    xLabels.add(trend.getCaptured());
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
                formatter = new XAxisMonthValueFormatter(xLabels);
                break;
            case YEAR:
                formatter = new XAxisYearValueFormatter(xLabels);
                break;
        }
        chart.getXAxis().setValueFormatter(formatter);

        // Format how the chart line looks
        LineDataSet dataSet = new LineDataSet(data, "Price");
        dataSet.setValueTextSize(10);
        dataSet.setCircleRadius(6);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2);
        dataSet.setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
        dataSet.setCircleColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));

        // Format data points to display with one decimal place
        dataSet.setValueFormatter(new DefaultValueFormatter(1));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);

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

        // Month view needs different customisations because of there are more data points
        if (resolution == ChartResolution.MONTH) {
            // Month view starts off fully zoomed out
            chart.getAxisLeft().setDrawLabels(true);
            chart.getAxisLeft().setDrawGridLines(true);
            dataSet.setDrawValues(false);
            chart.setTouchEnabled(true);
            chart.setDoubleTapToZoomEnabled(false);

            // Set window the same size as the week window
            float yRange = chart.getYChartMax() - chart.getYChartMin();
            chart.setVisibleYRange(yRange, yRange, YAxis.AxisDependency.LEFT);
            chart.setVisibleXRangeMinimum(dataWeek.size());

            final LineChart finalChart = chart;
            final DataSet<Entry> finalDataSet = dataSet;
            chart.setOnChartGestureListener(new OnChartGestureListener() {
                @Override
                public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                }

                @Override
                public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                }

                @Override
                public void onChartLongPressed(MotionEvent me) {
                }

                @Override
                public void onChartDoubleTapped(MotionEvent me) {
                }

                @Override
                public void onChartSingleTapped(MotionEvent me) {
                }

                @Override
                public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {
                }

                @Override
                public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                    if (finalChart.getViewPortHandler().getScaleX() <= 2) {
                        // If the chart is closer to the zoomed out side
                        finalChart.getAxisLeft().setDrawLabels(true);
                        finalChart.getAxisLeft().setDrawGridLines(true);
                        finalDataSet.setDrawValues(false);
                    } else {
                        // Zoomed in closer than 2x scale
                        finalChart.getAxisLeft().setDrawLabels(false);
                        finalChart.getAxisLeft().setDrawGridLines(false);
                        finalDataSet.setDrawValues(true);
                    }
                }
            });
        }
        chart.invalidate();
        return chart;
    }

    /**
     * Changes which chart is displayed to the user.
     * @param resolution The selected trend resolution to display.
     */
    private void updateVisibility(ChartResolution resolution) {
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

    /**
     * Changes the average price that is displayed to the user based on the selected fueltype.
     * @param selectedFuelType The new fueltype to display
     */
    private void updateAverage(String selectedFuelType) {
        Average average = averages.get(selectedFuelType);
        if (average != null) {
            averageTextView.setText(String.valueOf(average.getPrice()));
        }
    }

    @Override
    public void startActivity(Intent intent) {
        // Check if search intent
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            intent.putExtra(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }

        super.startActivity(intent);
    }
}

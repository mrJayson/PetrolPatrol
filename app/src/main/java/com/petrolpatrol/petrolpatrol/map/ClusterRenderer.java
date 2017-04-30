package com.petrolpatrol.petrolpatrol.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.trend.TodayPrice;
import com.petrolpatrol.petrolpatrol.util.Constants;

import java.util.Map;

class ClusterRenderer extends DefaultClusterRenderer {

    private Context context;
    private LayoutInflater inflater;
    private final String notApplicable;

    private Map<String, TodayPrice> todayPrices;

    private IconGenerator iconFactory;

    ClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        inflater = LayoutInflater.from(context);
        notApplicable = context.getString(R.string.not_applicable);
        FuelCheckClient client = new FuelCheckClient(context);
        client.getTodayPrices(new FuelCheckClient.FuelCheckResponse<Map<String, TodayPrice>>() {
            @Override
            public void onCompletion(Map<String, TodayPrice> res) {
                todayPrices = res;
            }
        });
         iconFactory = new IconGenerator(context);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {
        String priceOutput;
        if (item instanceof Marker) {
            priceOutput = String.valueOf(((Marker) item).getPrice());
        } else {
            priceOutput = notApplicable;
        }
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(priceOutput)));
    }


    private Bitmap getMarkerBitmapFromView(String priceString) {

/*        @SuppressLint("InflateParams")
        View markerView = inflater.inflate(R.layout.marker_map, null);
        ImageView circleView = (ImageView) markerView.findViewById(R.id.marker_circle);
        TextView priceView = (TextView) markerView.findViewById(R.id.marker_price);
        priceView.setText(price);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0,0,markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.buildDrawingCache();

        //Define a bitmap with the same size as the view
        Bitmap bitmap = Bitmap.createBitmap(markerView.getWidth(), markerView.getHeight(), Bitmap.Config.ARGB_8888);

        //Bind a canvas to it
        Canvas canvas = new Canvas(bitmap);

        markerView.draw(canvas);*/


        String fuelType = Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE);
        double fuelTypeMean = todayPrices.get(fuelType).getPrice();
        double priceNum = Double.valueOf(priceString);
        if (priceNum < (fuelTypeMean - Constants.STANDARD_DEV)) {
            // price is at the cheaper end of the distribution
            iconFactory.setBackground(context.getDrawable(R.drawable.shape_marker_good));

        }
        else if (priceNum > (fuelTypeMean + Constants.STANDARD_DEV)) {
            // price is at the expensive end of the distribution
            iconFactory.setBackground(context.getDrawable(R.drawable.shape_marker_bad));

        } else {
            // price is in the middle of the distribution
            iconFactory.setBackground(context.getDrawable(R.drawable.shape_marker_neutral));

        }
        iconFactory.setContentPadding(55,55,55,55);
        return iconFactory.makeIcon(String.valueOf(priceString));

        //return bitmap;
    }
}

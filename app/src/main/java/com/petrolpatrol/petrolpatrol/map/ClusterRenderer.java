package com.petrolpatrol.petrolpatrol.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import com.petrolpatrol.petrolpatrol.util.Colour;
import com.petrolpatrol.petrolpatrol.util.Gradient;

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

        String fuelType = Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE);
        double fuelTypeMean = todayPrices.get(fuelType).getPrice();
        double priceNum = Double.valueOf(priceString);

        Gradient grad = new Gradient(context,fuelTypeMean);

        Colour tint = grad.gradiateColour(priceNum);
        Drawable border = context.getDrawable(R.drawable.marker_border);
        // Default border colour is black
        if (border != null) {
            border.setTint(tint.integer);
        }
        Drawable background = context.getDrawable(R.drawable.marker_background);
        Drawable[] layers = {border, background};
        LayerDrawable compositeDrawable = new LayerDrawable(layers);
        iconFactory.setBackground(compositeDrawable);

        iconFactory.setContentPadding(55,55,55,55);
        return iconFactory.makeIcon(String.valueOf(priceString));

        //return bitmap;
    }
}

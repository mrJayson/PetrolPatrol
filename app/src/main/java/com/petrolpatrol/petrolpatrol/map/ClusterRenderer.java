package com.petrolpatrol.petrolpatrol.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.petrolpatrol.petrolpatrol.R;

class ClusterRenderer extends DefaultClusterRenderer {

    private LayoutInflater inflater;
    private final String notApplicable;

    ClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
        inflater = LayoutInflater.from(context);
        notApplicable = context.getString(R.string.not_applicable);
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


    private Bitmap getMarkerBitmapFromView(String price) {

        @SuppressLint("InflateParams")
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

        markerView.draw(canvas);

        return bitmap;
    }
}

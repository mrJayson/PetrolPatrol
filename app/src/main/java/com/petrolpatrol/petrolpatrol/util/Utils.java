package com.petrolpatrol.petrolpatrol.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.petrolpatrol.petrolpatrol.R;

public class Utils {

    private static final double equatorLengthInKm = 40075;

    public static double radiusToZoom(double radiusInKm) {
        return (Math.log(equatorLengthInKm/radiusInKm)/Math.log(2));
    }

    public static double zoomToRadius(double zoom) {
        return (equatorLengthInKm/(Math.pow(2, zoom)));
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static boolean fuelTypeSwitch(int id, Callback callback) throws NoSuchFieldException {
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
                // Execute if it is one of the fuel types
                callback.execute();
                return true;
        }
        throw new NoSuchFieldException("id does not correspond to a fuel type");
    }

    public static int identify(String value, String defType, Context context) {
        return context.getResources().getIdentifier(value, defType, context.getPackageName());
    }

    public static String fuelTypeToIconName(String fuelTypeID) {
        return "ic_fuel_" + fuelTypeID.toLowerCase();
    }

    public interface Callback {
        void execute();
    }
}

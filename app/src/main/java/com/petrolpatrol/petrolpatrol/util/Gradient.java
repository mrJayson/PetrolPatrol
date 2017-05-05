package com.petrolpatrol.petrolpatrol.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import com.petrolpatrol.petrolpatrol.R;

public class Gradient {

    private static int gradientRange = 20;

    private final Colour lowColour;
    private final Colour midColour;
    private final Colour highColour;

    private final double meanPrice;

    public Gradient(Context context, double meanPrice) {

        this.lowColour = new Colour(Integer.toHexString(ContextCompat.getColor(context, R.color.sushi)).substring(2));
        this.midColour = new Colour(Integer.toHexString(ContextCompat.getColor(context, R.color.school_bus_yellow)).substring(2));
        this.highColour = new Colour(Integer.toHexString(ContextCompat.getColor(context, R.color.cinnabar)).substring(2));

        this.meanPrice = meanPrice;
    }

    public Colour gradiateColour(double price) {
        int intPrice = (int) price;
        int intMeanPrice = (int) meanPrice;

        if (intPrice < (intMeanPrice - gradientRange)) {
            return lowColour;
        }
        else if (intPrice >= (intMeanPrice - gradientRange) && intPrice < intMeanPrice) {
            double p = ((double) intMeanPrice - (double) intPrice) / (double) gradientRange;
            int r = (int) ((double) lowColour.r * p + (double) midColour.r * (1-p));
            int g = (int) ((double) lowColour.g * p + (double) midColour.g * (1-p));
            int b = (int) ((double) lowColour.b * p + (double) midColour.b * (1-p));
            return new Colour(r,g,b);
        }
        else if (intPrice >= intMeanPrice && intPrice < (intMeanPrice + gradientRange)) {
            double p = ((double) intPrice - (double) intMeanPrice) / (double) gradientRange;
            int r = (int) ((double) midColour.r * (1-p) + (double) highColour.r * p);
            int g = (int) ((double) midColour.g * (1-p) + (double) highColour.g * p);
            int b = (int) ((double) midColour.b * (1-p) + (double) highColour.b * p);
            return new Colour(r,g,b);
        }
        else if (intPrice >= (intMeanPrice + gradientRange)) {
            return highColour;
        }
        return highColour;
    }
}

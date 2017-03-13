package com.petrolpatrol.petrolpatrol.util;

import android.content.Context;
import com.petrolpatrol.petrolpatrol.R;

/**
 * Created by jason on 13/03/17.
 */
public class Utils {

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

    public interface Callback {
        void execute();
    }
}

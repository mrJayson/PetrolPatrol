package com.petrolpatrol.petrolpatrol.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jason on 14/02/17.
 */
public class TimeUtils {

    public static final String epochTimeZero = "01/01/1970 00:00:00";

    public static String UTCTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public static boolean isExpired(long expiryTimeMilli) {
        //TODO consider whether buffer time should be given as a conservative measure
        return (expiryTimeMilli <= System.currentTimeMillis());
    }
}

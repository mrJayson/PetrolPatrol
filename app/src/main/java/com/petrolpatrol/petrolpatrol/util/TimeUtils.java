package com.petrolpatrol.petrolpatrol.util;

import android.text.format.DateUtils;

import java.text.ParseException;
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

    public static long timeStampToMilli(String timeStamp) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(timeStamp).getTime();
    }

    public static String timeAgo(String timeStamp) {

        String time = null;

        try {
            long diff = System.currentTimeMillis() - timeStampToMilli(timeStamp);
            long diffSeconds = diff / 1000;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays > 0) {
                if (diffDays == 1) {
                    time = diffDays + " day ago ";
                } else {
                    time = diffDays + " days ago ";
                }
            } else {
                if (diffHours > 0) {
                    if (diffHours == 1) {
                        time = diffHours + " hr ago";
                    } else {
                        time = diffHours + " hrs ago";
                    }
                } else {
                    if (diffMinutes > 0) {
                        if (diffMinutes == 1) {
                            time = diffMinutes + " min ago";
                        } else {
                            time = diffMinutes + " mins ago";
                        }
                    } else {
                        if (diffSeconds > 0) {
                            time = "just now";
                        }
                    }
                }
            }
        } catch (ParseException pe) {
            time = "N/A";
        }
        return time;
    }
}

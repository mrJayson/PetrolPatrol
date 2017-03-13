package com.petrolpatrol.petrolpatrol.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class TimeUtils {

    private static final String TAG = makeLogTag(TimeUtils.class);
    private static final String[] dateFormats = {"yyyy-MM-dd", "dd/MM/yyyy", "dd/MM/yyyy hh:mm:ss", "MMMM yyyy"};

    public static final String epochTimeZero = "01/01/1970 00:00:00";

    public static String UTCTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public static long timeStampToMilli(String timeStamp) throws ParseException {
        return parse(timeStamp).getTime();
    }

    private static String dateToFormat(String dateStamp, String format) throws ParseException {
        SimpleDateFormat dateFormat =  new SimpleDateFormat(format);
        Date date = parse(dateStamp);
        if (date != null) {
            return dateFormat.format(date);
        } else {
            return null;
        }
    }

    public static String dateToDayOfWeek(String dateStamp) throws ParseException {
        return dateToFormat(dateStamp, "EEE");
    }

    public static String dateToDayOfMonth(String dateStamp) throws ParseException {
        return dateToFormat(dateStamp, "dd");
    }

    public static String dateToMonthOfYear(String dateStamp) throws ParseException {
        return dateToFormat(dateStamp, "MMM");
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

    private static Date parse(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (String format : dateFormats) {
            dateFormat.applyPattern(format);
            try {
                return dateFormat.parse(time);
            } catch (ParseException e) {
                // Do nothing, continue with the rest of the formats
            }
        }
        return null;
    }
}

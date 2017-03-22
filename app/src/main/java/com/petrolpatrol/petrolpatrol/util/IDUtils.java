package com.petrolpatrol.petrolpatrol.util;

import android.content.Context;

import java.util.UUID;

public class IDUtils {

    public static String UUID() {
        return UUID.randomUUID().toString();
    }

    public static int identify(String value, String defType, Context context) {
        return context.getResources().getIdentifier(value, defType, context.getPackageName());
    }
}

package com.petrolpatrol.petrolpatrol.datastore;

import android.content.Context;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * Created by jason on 16/02/17.
 */
public class SharedPreferences {

    private static final String TAG = makeLogTag(SharedPreferences.class);

    private static final String PREFERENCES_NAME = SharedPreferences.class.getSimpleName();

    private static SharedPreferences instance;
    private android.content.SharedPreferences sharedPreferences;
    private android.content.SharedPreferences.Editor editor;
    private Context context;

    public enum Key {
        /* Recommended naming convention:
        * ints, floats, doubles, longs:
        * SAMPLE_NUM or SAMPLE_COUNT or SAMPLE_INT, SAMPLE_LONG etc.
        *
        * boolean: IS_SAMPLE, HAS_SAMPLE, CONTAINS_SAMPLE
        *
        * String: SAMPLE_KEY, SAMPLE_STR or just SAMPLE
        */
        OAUTH_TOKEN,
        OAUTH_EXPIRY_TIME,
        REFERENCE_MODIFIED_TIMESTAMP
    }

    private SharedPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferences(context);
        }
        return instance;
    }

    public static synchronized SharedPreferences getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Need to initialize the singleton with Context first");
        }
        return instance;
    }

    // getters

    public String getString(Key key, String defaultValue) {
        return sharedPreferences.getString(key.name(), defaultValue);
    }

    public String getString(Key key) {
        return getString(key, null);
    }

    public int getInt(Key key, int defaultValue) {
        return sharedPreferences.getInt(key.name(), defaultValue);
    }

    public int getInt(Key key) {
        return sharedPreferences.getInt(key.name(), 0);
    }

    public long getLong(Key key, long defaultValue) {
        return sharedPreferences.getLong(key.name(), defaultValue);
    }

    public long getLong(Key key) {
        return sharedPreferences.getLong(key.name(), 0);
    }

    public float getFloat(Key key, float defaultValue) {
        return sharedPreferences.getFloat(key.name(), defaultValue);
    }

    public float getFloat(Key key) {
        return sharedPreferences.getFloat(key.name(), 0);
    }

    public double getDouble(Key key, double defaultValue) {
        try {
            return Double.valueOf(sharedPreferences.getString(key.name(), String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public double getDouble(Key key) {
        return getDouble(key, 0);
    }

    public boolean getBoolean(Key key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key.name(), defaultValue);
    }

    public boolean getBoolean(Key key) {
        return sharedPreferences.getBoolean(key.name(), false);
    }

    // setters

    public void put(Key key, boolean val) {
        editor.putBoolean(key.name(), val);
        editor.commit();
    }
    public void put(Key key, int val) {
        editor.putInt(key.name(), val);
        editor.commit();
    }
    public void put(Key key, long val) {
        editor.putLong(key.name(), val);
        editor.commit();
    }
    public void put(Key key, double val) {
        editor.putString(key.name(), String.valueOf(val));
        editor.commit();
    }
    public void put(Key key, float val) {
        editor.putFloat(key.name(), val);
        editor.commit();
    }
    public void put(Key key, String val) {
        editor.putString(key.name(), val);
        editor.commit();
    }

    // remove all keys from SharedPreferences
    public void clear() {
        editor.clear();
        editor.commit();
    }
}

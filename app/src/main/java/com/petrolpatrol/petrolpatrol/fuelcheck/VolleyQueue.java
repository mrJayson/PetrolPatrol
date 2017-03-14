package com.petrolpatrol.petrolpatrol.fuelcheck;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * Volley works by adding requests to a {@link RequestQueue}, the requestQueue manages all the requests asynchronously,
 * It is optimal to work with only one requestQueue so the management of all pending requests can occur efficiently.
 */
public class VolleyQueue {
    private static final String TAG = makeLogTag(VolleyQueue.class);

    private static VolleyQueue instance;

    private RequestQueue requestQueue;
    private Context context;

    private VolleyQueue(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public static VolleyQueue getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyQueue(context);
        }
        return instance;
    }

    public static VolleyQueue getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Need to call getInstance with a context the first time");
        }
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    public void cancelRequests(RequestTag tag) {
        getRequestQueue().cancelAll(tag.getTag());
    }

}

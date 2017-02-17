package com.petrolpatrol.petrolpatrol.fuelcheck;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SharedPreferences;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * Created by jason on 16/02/17.
 */
public class FuelCheckClient {

    private static final String TAG = makeLogTag(FuelCheckClient.class);

    private Context context;

    private static final String baseURL = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/";

    public FuelCheckClient(Context context) {
        this.context = context;
    }

    public interface FuelCheckResponse<T> {
        public void onCompletion(T res);
    }

    public void authToken(String base64Encode) {
        String url = "https://api.onegov.nsw.gov.au/oauth/client_credential/accesstoken?grant_type=client_credentials";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", "Basic " + base64Encode);
        headerMap.put("dataType", "json");
        requestGET(url, headerMap, new FuelCheckResponse<FuelCheckResult>() {
            @Override
            public void onCompletion(FuelCheckResult res) {
                try {
                    if (res.isSuccess() && res.dataIsObject()) {
                        JSONObject jsonObject = res.getDataAsObject();
                        String authToken = jsonObject.getString("access_token");
                        long issuedAt = jsonObject.getLong("issued_at");
                        long lifeSpan = jsonObject.getLong("expires_in") * 1000; // Convert to milliseconds
                        SharedPreferences.getInstance().put(SharedPreferences.Key.OAUTH_TOKEN, authToken);
                        SharedPreferences.getInstance().put(SharedPreferences.Key.OAUTH_EXPIRY_TIME, issuedAt + lifeSpan);
                    }
                } catch (JSONException e) {
                    LOGE(TAG, "Auth token Error");
                    e.printStackTrace();
                }
            }
        });
    }

    public void getFuelPricesWithinRadius(double latitude, double longitude, String sortBy, String fuelType) {
        String url = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/nearby";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("apikey", context.getString(R.string.clientID));
        headerMap.put("transactionid", IDUtils.UUID());
        headerMap.put("requesttimestamp", TimeUtils.UTCTimestamp());
        // Content-type header is apparently already inserted
        //params.put("Content-type", "application/json; charset=utf-8");
        headerMap.put("Authorization", "Bearer "+ SharedPreferences.getInstance().getString(SharedPreferences.Key.OAUTH_TOKEN));

        // Prepare the jsonbody of the request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("fueltype", fuelType);
            jsonBody.put("brand", new JSONArray()
                    .put("7-Eleven")
                    .put("BP")
                    .put("Budget")
                    .put("Caltex")
                    .put("Caltex Woolworths")
                    .put("Coles Express")
                    .put("Costco")
                    .put("Enhance")
                    .put("Independent")
                    .put("Liberty")
                    .put("Lowes")
                    .put("Matilda")
                    .put("Metro Fuel")
                    .put("Mobil")
                    .put("Prime Petroleum")
                    .put("Puma Energy")
                    .put("Shell")
                    .put("Speedway")
                    .put("Tesla")
                    .put("United")
                    .put("Westside"));
            jsonBody.put("latitude", String.valueOf(latitude));
            jsonBody.put("longitude", String.valueOf(longitude));
            jsonBody.put("radius", "10");
            jsonBody.put("sortby", sortBy);
            jsonBody.put("sortascending", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestPOST(url, headerMap, jsonBody, new FuelCheckResponse<FuelCheckResult>() {

            @Override
            public void onCompletion(FuelCheckResult res) {
                try {
                    if (res.isSuccess() && res.dataIsObject()) {
                        JSONArray pricesJSON = res.getDataAsObject().getJSONArray("prices");
                        LOGI(TAG, pricesJSON.toString(4));
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    public void requestGET(String url, final Map<String, String> headerMap, final FuelCheckResponse<FuelCheckResult> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    /**
                     * Called when a response is received.
                     *
                     * @param response
                     */
                    @Override
                    public void onResponse(JSONObject response) {
                        // structure the response in a FuelCheckResult
                        FuelCheckResult res = new FuelCheckResult();

                        res.setSuccess(true);
                        // Embed response as is, the callback will handle the deserialization
                        res.setData(response);
                        completion.onCompletion(res);
                    }
                }, new Response.ErrorListener() {

            /**
             * Callback method that an error has been occurred with the
             * provided error code and optional user-readable message.
             *
             * @param error
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                FuelCheckResult res = new FuelCheckResult();
                res.setSuccess(false);
                completion.onCompletion(res);
                displayVolleyResponseError(error);
            }
        }) {
            /**
             * Returns a list of extra HTTP headers to go along with this request. Can
             * throw {@link AuthFailureError} as authentication may be required to
             * provide these values.
             *
             * @throws AuthFailureError In the event of auth failure
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headerMap;
            }
        };

        // Hand the request over to the request queue
        VolleyQueue.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void requestPOST(String url, final Map<String, String> headerMap, JSONObject jsonBody, final FuelCheckResponse<FuelCheckResult> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {

                    /**
                     * Called when a response is received.
                     *
                     * @param response
                     */
                    @Override
                    public void onResponse(JSONObject response) {
                        // structure the response in a FuelCheckResult
                        FuelCheckResult res = new FuelCheckResult();

                        res.setSuccess(true);
                        // Embed response as is, the callback will handle the deserialization
                        res.setData(response);
                        completion.onCompletion(res);
                    }
                }, new Response.ErrorListener() {

            /**
             * Callback method that an error has been occurred with the
             * provided error code and optional user-readable message.
             *
             * @param error
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                FuelCheckResult res = new FuelCheckResult();
                res.setSuccess(false);
                completion.onCompletion(res);
                displayVolleyResponseError(error);
            }
        }) {
            /**
             * Returns a list of extra HTTP headers to go along with this request. Can
             * throw {@link AuthFailureError} as authentication may be required to
             * provide these values.
             *
             * @throws AuthFailureError In the event of auth failure
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headerMap;
            }
        };

        // Hand the request over to the request queue
        VolleyQueue.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void displayVolleyResponseError(VolleyError error) {
        Context context = VolleyQueue.getInstance().getContext();
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            Toast.makeText(context,
                    context.getString(R.string.error_network_timeout),
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof AuthFailureError) {
            Toast.makeText(context,
                    context.getString(R.string.error_auth_failure),
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof ServerError) {
            Toast.makeText(context,
                    context.getString(R.string.error_server),
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof NetworkError) {
            Toast.makeText(context,
                    context.getString(R.string.error_network),
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof ParseError) {
            Toast.makeText(context,
                    context.getString(R.string.error_parse),
                    Toast.LENGTH_LONG).show();
        }
    }

}

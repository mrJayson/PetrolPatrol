package com.petrolpatrol.petrolpatrol.fuelcheck;

import android.content.Context;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.petrolpatrol.petrolpatrol.R;
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

    private static final String baseURL = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/";

    public interface FuelCheckResponse<T> {
        public void onCompletion(T res);
    }

    public void authToken(String base64Encode, final FuelCheckResponse<String> completion) {
        String url = "https://api.onegov.nsw.gov.au/oauth/client_credential/accesstoken?grant_type=client_credentials";
        String authToken = null;
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", "Basic " + base64Encode);
        headerMap.put("dataType", "json");
        requestGET(url, headerMap, new FuelCheckResponse<FuelCheckResult>() {
            @Override
            public void onCompletion(FuelCheckResult res) {
                try {
                    LOGI(TAG, res.getDataAsObject().toString(3));
                } catch (JSONException e) {
                    e.printStackTrace();
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

    public void requestPOST(String url, final Map<String, String> headerMap, final FuelCheckResponse<FuelCheckResult> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
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

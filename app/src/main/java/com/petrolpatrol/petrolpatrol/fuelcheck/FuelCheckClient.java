package com.petrolpatrol.petrolpatrol.fuelcheck;

import android.content.Context;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.datastore.SharedPreferences;
import com.petrolpatrol.petrolpatrol.model.Brand;
import com.petrolpatrol.petrolpatrol.model.FuelType;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void getFuelPricesWithinRadius(double latitude, double longitude, String sortBy, String fuelType, final FuelCheckResponse<List<Station>> completion) {
        String url = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/nearby";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
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
            jsonBody.put("radius", "3");
            jsonBody.put("sortby", sortBy);
            jsonBody.put("sortascending", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestPOST(url, headerMap, jsonBody, new FuelCheckResponse<FuelCheckResult>() {

            @Override
            public void onCompletion(FuelCheckResult res) {
                LOGI(TAG, "nearby query processing");
                JSONArray JSONResponse;
                GsonBuilder gsonBuilder = new GsonBuilder();
                List<Station> orderedStationList = new ArrayList<>();
                final SQLiteClient sqliteClient = new SQLiteClient(context);

                try {
                    if (res.isSuccess() && res.dataIsObject()) {
                        if (res.getDataAsObject().get("stations") instanceof JSONArray && res.getDataAsObject().get("prices") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONArray("stations");

                            final Map<Integer, Station> map = new HashMap<>();
                            for (Station station : toStationObjects(JSONResponse)) {
                                map.put(station.getId(), station);
                            }

                            // Custom deserializer needed to deal with non-primitive types in the Price class
                            JsonDeserializer<Price> deserializer = new JsonDeserializer<Price>() {
                                @Override
                                public Price deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                                    JsonObject priceJson = json.getAsJsonObject();
                                    Station station = map.get(priceJson.get("stationcode").getAsInt());
                                    sqliteClient.open();
                                    FuelType fuelType = sqliteClient.getFuelType(priceJson.get("fueltype").getAsString());
                                    sqliteClient.close();

                                    Price price = new Price(
                                            priceJson.get("stationcode").getAsInt(),
                                            fuelType,
                                            priceJson.get("price").getAsDouble(),
                                            priceJson.get("lastupdated").getAsString()
                                            );

                                    return price;
                                }
                            };

                            JSONResponse = res.getDataAsObject().getJSONArray("prices");

                            gsonBuilder.registerTypeAdapter(Price.class, deserializer);
                            Gson gson = gsonBuilder.create();

                            for (int i = 0; i < JSONResponse.length(); i++) {
                                Price price = gson.fromJson(JSONResponse.get(i).toString(),Price.class);
                                Station station = (map.get(price.getStationID()));
                                station.setPrice(price);
                                orderedStationList.add(station);
                            }

                        } else {
                            throw new JSONException("Invalid JSON");
                        }
                    }
                } catch (Exception e) {
                    LOGE(TAG, "Error occurred processing data");
                    e.printStackTrace();
                }
                completion.onCompletion(orderedStationList);
            }
        });
    }

    public void getReferenceData(final FuelCheckResponse completion) {
        String url = "https://api.onegov.nsw.gov.au/FuelCheckRefData/v1/fuel/lovs";
        SQLiteClient sqliteClient = new SQLiteClient(context);
        sqliteClient.open();
        String ifModifiedSince = sqliteClient.getMetadata("REFERENCE_MODIFIED_TIMESTAMP");
        sqliteClient.close();
        if (ifModifiedSince == null) {
            ifModifiedSince = TimeUtils.epochTimeZero;
        }
        String authToken = SharedPreferences.getInstance().getString(SharedPreferences.Key.OAUTH_TOKEN);

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("apikey", context.getString(R.string.clientID));
        headerMap.put("transactionid", IDUtils.UUID());
        headerMap.put("requesttimestamp", TimeUtils.UTCTimestamp());
        headerMap.put("if-modified-since", ifModifiedSince);
        headerMap.put("Content-Type", "application/json; charset=utf-8");
        headerMap.put("Authorization", "Bearer "+ authToken);

        requestGET(url, headerMap, new FuelCheckResponse<FuelCheckResult>() {
            @Override
            public void onCompletion(FuelCheckResult res) {
                if (res.isSuccess()) {
                    // Add Brands and FuelTypes first, then stations
                    JSONArray JSONResponse;
                    Gson gson = new Gson();
                    final SQLiteClient sqliteClient = new SQLiteClient(context);

                    try {
                        // Brands
                        if (res.getDataAsObject().get("brands") instanceof JSONObject
                                && res.getDataAsObject().getJSONObject("brands").get("items") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONObject("brands").getJSONArray("items");
                        }
                        else if (res.getDataAsObject().get("brands") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONArray("brands");
                        } else {
                            throw new JSONException("Invalid JSON");
                        }
                        Type brandListType = new TypeToken<ArrayList<Brand>>(){}.getType();
                        List<Brand> brands = gson.fromJson(JSONResponse.toString(),brandListType);

                        // Insert into database
                        sqliteClient.open();
                        for (Brand b : brands) {
                            sqliteClient.insertBrand(b);
                        }
                        sqliteClient.close();

                        // FuelTypes
                        if (res.getDataAsObject().get("fueltypes") instanceof JSONObject
                                && res.getDataAsObject().getJSONObject("fueltypes").get("items") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONObject("fueltypes").getJSONArray("items");
                        }
                        else if (res.getDataAsObject().get("fueltypes") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONArray("fueltypes");
                        } else {
                            throw new JSONException("Invalid JSON");
                        }
                        Type fuelTypeListType = new TypeToken<ArrayList<FuelType>>(){}.getType();
                        List<FuelType> fuelTypes = gson.fromJson(JSONResponse.toString(),fuelTypeListType);

                        // Insert into database
                        sqliteClient.open();
                        for (FuelType ft : fuelTypes) {
                            sqliteClient.insertFuelType(ft);
                        }
                        sqliteClient.close();

                        // Stations
                        if (res.getDataAsObject().get("stations") instanceof JSONObject
                                && res.getDataAsObject().getJSONObject("stations").get("items") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONObject("stations").getJSONArray("items");
                        }
                        else if (res.getDataAsObject().get("stations") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONArray("stations");
                        } else {
                            throw new JSONException("Invalid JSON");
                        }

                        List<Station> stations = toStationObjects(JSONResponse);

                        // Insert into database
                        sqliteClient.open();
                        for (Station station : stations) {
                            sqliteClient.insertStation(station);
                        }
                        sqliteClient.close();

                        sqliteClient.open();
                        sqliteClient.setMetadata("REFERENCE_MODIFIED_TIMESTAMP", TimeUtils.UTCTimestamp());
                        sqliteClient.close();

                        completion.onCompletion(res);

                    } catch (Exception e) {
                        LOGE(TAG, "Error occurred processing reference data");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void authToken(final FuelCheckResponse<FuelCheckResult> completion) {
        String url = "https://api.onegov.nsw.gov.au/oauth/client_credential/accesstoken?grant_type=client_credentials";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Basic " + context.getString(R.string.base64Encode));
        headerMap.put("dataType", "json");
        requestGET(url, headerMap, new FuelCheckResponse<FuelCheckResult>() {
            @Override
            public void onCompletion(FuelCheckResult res) {
                try {
                    if (res.isSuccess() && res.dataIsObject()) {
                        JSONObject jsonObject = res.getDataAsObject();
                        String authToken = jsonObject.getString("access_token");
                        SharedPreferences.getInstance().put(SharedPreferences.Key.OAUTH_TOKEN, authToken);
                        if (completion != null) {
                            completion.onCompletion(res);
                        }
                    }
                } catch (JSONException e) {
                    LOGE(TAG, "Auth token Error");
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestGET(final String url, final Map<String, String> headerMap, final FuelCheckResponse<FuelCheckResult> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    /**
                     * Called when a response is received.
                     *
                     * @param response The volley response
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
             * @param error The error response
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                FuelCheckResult res = new FuelCheckResult();
                res.setSuccess(false);
                completion.onCompletion(res);

                // Error Handling
                Context context = VolleyQueue.getInstance().getContext();
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(context,
                            context.getString(R.string.error_network_timeout),
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    // Auth token is invalid, attempt to get a new one
                    authToken(new FuelCheckResponse<FuelCheckResult>() {
                        @Override
                        public void onCompletion(FuelCheckResult res) {
                            String authToken = SharedPreferences.getInstance().getString(SharedPreferences.Key.OAUTH_TOKEN);
                            headerMap.put("Authorization", "Bearer "+ authToken);
                            requestGET(url, headerMap, completion);
                        }
                    });

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

    private void requestPOST(final String url, final Map<String, String> headerMap, final JSONObject jsonBody, final FuelCheckResponse<FuelCheckResult> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {

                    /**
                     * Called when a response is received.
                     *
                     * @param response The volley response
                     */
                    @Override
                    public void onResponse(JSONObject response) {
                        LOGI(TAG, "requestPOST successful response");
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
             * @param error The error response
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                LOGI(TAG, "requestPOST bad response");
                FuelCheckResult res = new FuelCheckResult();
                res.setSuccess(false);
                completion.onCompletion(res);

                // Error Handling
                Context context = VolleyQueue.getInstance().getContext();
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(context,
                            context.getString(R.string.error_network_timeout),
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    // Auth token is invalid, attempt to get a new one
                    authToken(new FuelCheckResponse<FuelCheckResult>() {
                        @Override
                        public void onCompletion(FuelCheckResult res) {
                            String authToken = SharedPreferences.getInstance().getString(SharedPreferences.Key.OAUTH_TOKEN);
                            headerMap.put("Authorization", "Bearer "+ authToken);
                            requestPOST(url, headerMap, jsonBody, completion);
                        }
                    });

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

    private List<Price> toPriceObjects(JSONArray JSON) {
        final SQLiteClient sqliteClient = new SQLiteClient(context);
        List<Price> prices = new ArrayList<>();
        return prices;
    }

    private List<Station> toStationObjects(JSONArray JSON) {

        final SQLiteClient sqliteClient = new SQLiteClient(context);
        List<Station> stations = new ArrayList<>();

        // Custom deserializer needed to deal with non-primitive types in the ServiceStation class
        JsonDeserializer<Station> deserializer = new JsonDeserializer<Station>() {
            @Override
            public Station deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject stationJson = json.getAsJsonObject();
                JsonObject locationJson = stationJson.getAsJsonObject("location");
                double distance = Station.NO_DISTANCE;
                if (locationJson.has("distance")) {
                    distance = locationJson.get("distance").getAsDouble();
                }
                sqliteClient.open();
                Brand brand = sqliteClient.getBrand(stationJson.get("brand").getAsString());
                sqliteClient.close();
                return new Station(
                        brand,
                        stationJson.get("code").getAsInt(),
                        stationJson.get("name").getAsString(),
                        stationJson.get("address").getAsString(),
                        locationJson.get("latitude").getAsDouble(),
                        locationJson.get("longitude").getAsDouble(),
                        distance
                );
            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Station.class, deserializer);
        Gson customGson = gsonBuilder.create();

        try {
            for (int i = 0; i < JSON.length(); i++) {
                stations.add(customGson.fromJson(JSON.get(i).toString(), Station.class));
            }
        } catch (JSONException e) {
            LOGE(TAG, "Error occurred processing JSONStations");
            e.printStackTrace();
        }
        return stations;
    }
}

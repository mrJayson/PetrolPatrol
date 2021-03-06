package com.petrolpatrol.petrolpatrol.fuelcheck;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.*;
import com.petrolpatrol.petrolpatrol.model.Trend;
import com.petrolpatrol.petrolpatrol.util.IDUtils;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.*;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class FuelCheck {

    private static final String TAG = makeLogTag(FuelCheck.class);

    private Context context;

    private static final String baseURL = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/";

    public FuelCheck(Context context) {
        this.context = context;
    }

    public interface OnResponseListener<T> {
        void onCompletion(T res);
    }

    public void getTrend(String fuelType, final OnResponseListener<List<Trend>> completion) {
        String url = "http://api.onegov.nsw.gov.au/FuelCheckApp/v1/fuel/prices/trends/";

        JSONObjectRequestGET(url + fuelType, new OnResponseListener<Response>() {
            @Override
            public void onCompletion(Response res) {
                JSONArray JSONResponse;
                if (res.isSuccess() && res.dataIsObject()) {
                    try {
                        if (res.getDataAsObject().get("AveragePrices") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONArray("AveragePrices");
                            completion.onCompletion(toTrendObjects(JSONResponse));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void getAverages(final OnResponseListener<Map<String, Average>> completion) {
        String url = "http://api.onegov.nsw.gov.au/FuelCheckApp/v1/fuel/prices/currenttrend";
        JSONArrayRequestGET(url, new OnResponseListener<Response>() {
            @Override
            public void onCompletion(Response res) {
                if (res.isSuccess() && res.dataIsArray()) {
                    JSONArray JSONResponse = res.getDataAsArray();
                    completion.onCompletion(toAverageObjects(JSONResponse));
                }
            }
        });
    }

    public void getFuelPricesForStation(final int stationCode, final OnResponseListener<List<Price>> completion) {
        String url = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/station/";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("apikey", context.getString(R.string.clientID));
        headerMap.put("transactionid", IDUtils.UUID());
        headerMap.put("requesttimestamp", TimeUtils.UTCTimestamp());
        headerMap.put("Content-type", "application/json; charset=utf-8");
        headerMap.put("Authorization", "Bearer "+ Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN));

        JSONObjectRequestGET(url + String.valueOf(stationCode), headerMap, new OnResponseListener<Response>() {
            @Override
            public void onCompletion(Response res) {
                JSONArray JSONResponse;
                if (res.isSuccess() && res.dataIsObject()) {
                    try {
                        if (res.getDataAsObject().get("prices") instanceof JSONArray) {
                            JSONResponse = res.getDataAsObject().getJSONArray("prices");
                            completion.onCompletion(toPriceObjects(JSONResponse, stationCode));
                        }
                    } catch (JSONException e) {
                        LOGE(TAG, "Error occurred processing data");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void getFuelPricesWithinRadius(double latitude, double longitude, String sortBy, String fuelType, final OnResponseListener<List<Station>> completion) {
        getFuelPricesWithinRadius(latitude, longitude, null, sortBy, fuelType, null, completion);
    }

    public void getFuelPricesWithinRadius(double latitude, double longitude, String sortBy, String fuelType, RequestTag tag, final OnResponseListener<List<Station>> completion) {
        getFuelPricesWithinRadius(latitude, longitude, null, sortBy, fuelType, tag, completion);
    }

    public void getFuelPricesWithinRadius(double latitude, double longitude, Integer radiusInKm, String sortBy, String fuelType, final OnResponseListener<List<Station>> completion) {
        getFuelPricesWithinRadius(latitude, longitude, radiusInKm, sortBy, fuelType, null, completion);
    }

    public void getFuelPricesWithinRadius(double latitude, double longitude, Integer radiusInKm, String sortBy, String fuelType, RequestTag tag, final OnResponseListener<List<Station>> completion) {
        String url = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/nearby";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("apikey", context.getString(R.string.clientID));
        headerMap.put("transactionid", IDUtils.UUID());
        headerMap.put("requesttimestamp", TimeUtils.UTCTimestamp());
        // Content-type header is apparently already inserted
        //params.put("Content-type", "application/json; charset=utf-8");
        headerMap.put("Authorization", "Bearer "+ Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN));

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
            if (radiusInKm != null) {
                jsonBody.put("radius", String.valueOf(radiusInKm));
            }
            jsonBody.put("sortby", sortBy);
            jsonBody.put("sortascending", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestPOST(url, headerMap, jsonBody, tag, new OnResponseListener<Response>() {

            @Override
            public void onCompletion(Response res) {
                new AsyncTask<Response, Integer, List<Station>>() {
                    @Override
                    protected List<Station> doInBackground(Response... res) {
                        JSONArray JSONResponse;
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        List<Station> orderedStationList = new ArrayList<>();
                        final SQLiteClient sqliteClient = new SQLiteClient(context);

                        try {
                            if (res[0].isSuccess() && res[0].dataIsObject()) {
                                if (res[0].getDataAsObject().get("stations") instanceof JSONArray && res[0].getDataAsObject().get("prices") instanceof JSONArray) {
                                    JSONResponse = res[0].getDataAsObject().getJSONArray("stations");

                                    final Map<Integer, Station> map = new HashMap<>();
                                    for (Station station : toStationObjects(JSONResponse)) {
                                        map.put(station.getId(), station);
                                    }

                                    // Custom deserializer needed to deal with non-primitive types in the Price class
                                    JsonDeserializer<Price> deserializer = new JsonDeserializer<Price>() {
                                        @Override
                                        public Price deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                                            JsonObject priceJson = json.getAsJsonObject();
                                            sqliteClient.open();
                                            FuelType fuelType = sqliteClient.getFuelType(priceJson.get("fueltype").getAsString());
                                            sqliteClient.close();

                                            return new Price(
                                                    priceJson.get("stationcode").getAsInt(),
                                                    fuelType,
                                                    priceJson.get("price").getAsDouble(),
                                                    priceJson.get("lastupdated").getAsString()
                                            );
                                        }
                                    };

                                    JSONResponse = res[0].getDataAsObject().getJSONArray("prices");

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
                        return orderedStationList;
                    }

                    @Override
                    protected void onPostExecute(List<Station> stations) {
                        completion.onCompletion(stations);
                    }
                }.execute(res);
            }
        });
    }

    public void getFuelPricesForLocation(String query, String sortBy, String fuelType, final OnResponseListener<List<Station>> completion) {

        String url = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/location";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("apikey", context.getString(R.string.clientID));
        headerMap.put("transactionid", IDUtils.UUID());
        headerMap.put("requesttimestamp", TimeUtils.UTCTimestamp());
        // Content-type header is apparently already inserted
        //params.put("Content-type", "application/json; charset=utf-8");
        headerMap.put("Authorization", "Bearer "+ Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN));

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
            jsonBody.put("namedlocation", query);
            jsonBody.put("sortby", sortBy);
            jsonBody.put("sortascending", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestPOST(url, headerMap, jsonBody, new OnResponseListener<Response>() {

            @Override
            public void onCompletion(Response res) {
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
                                    sqliteClient.open();
                                    FuelType fuelType = sqliteClient.getFuelType(priceJson.get("fueltype").getAsString());
                                    sqliteClient.close();

                                    return new Price(
                                            priceJson.get("stationcode").getAsInt(),
                                            fuelType,
                                            priceJson.get("price").getAsDouble(),
                                            priceJson.get("lastupdated").getAsString()
                                    );
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

    public void getReferenceData(final OnResponseListener<Object> completion) {
        String url = "https://api.onegov.nsw.gov.au/FuelCheckRefData/v1/fuel/lovs";
        SQLiteClient sqliteClient = new SQLiteClient(context);
        sqliteClient.open();
        String ifModifiedSince = sqliteClient.getMetadata("REFERENCE_MODIFIED_TIMESTAMP");
        sqliteClient.close();
        if (ifModifiedSince == null) {
            ifModifiedSince = TimeUtils.epochTimeZero;
        }
        String authToken = Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN);

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("apikey", context.getString(R.string.clientID));
        headerMap.put("transactionid", IDUtils.UUID());
        headerMap.put("requesttimestamp", TimeUtils.UTCTimestamp());
        headerMap.put("if-modified-since", ifModifiedSince);
        headerMap.put("Content-Type", "application/json; charset=utf-8");
        headerMap.put("Authorization", "Bearer "+ authToken);

        JSONObjectRequestGET(url, headerMap, new OnResponseListener<Response>() {
            @Override
            public void onCompletion(Response res) {
                if (res.isSuccess()) {
                    // Add Brands and FuelTypes first, then stations
                    JSONArray JSONResponse;
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

                        Gson brandGson = new GsonBuilder().registerTypeAdapter(Brand.class, new JsonDeserializer<Brand>() {
                            @Override
                            public Brand deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                                JsonObject brandJson = jsonElement.getAsJsonObject();
                                if (brandJson.has("id")) {
                                    return new Brand(brandJson.get("id").getAsInt(), brandJson.get("name").getAsString());
                                } else {
                                    return new Brand(brandJson.get("name").getAsString());
                                }
                            }
                        }).create();

                        Type brandListType = new TypeToken<ArrayList<Brand>>(){}.getType();
                        List<Brand> brands = brandGson.fromJson(JSONResponse.toString(),brandListType);

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

                        Gson fuelTypeGson = new GsonBuilder().registerTypeAdapter(FuelType.class, new JsonDeserializer<FuelType>() {
                            @Override
                            public FuelType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                                JsonObject fuelTypeJson = jsonElement.getAsJsonObject();
                                if (fuelTypeJson.has("id")) {
                                    return new FuelType(fuelTypeJson.get("id").getAsInt(), fuelTypeJson.get("code").getAsString(),  fuelTypeJson.get("name").getAsString());
                                } else {
                                    return new FuelType(fuelTypeJson.get("code").getAsString(), fuelTypeJson.get("name").getAsString());
                                }
                            }
                        }).create();

                        Type fuelTypeListType = new TypeToken<ArrayList<FuelType>>(){}.getType();
                        List<FuelType> fuelTypes = fuelTypeGson.fromJson(JSONResponse.toString(),fuelTypeListType);

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

    private void authToken(final OnResponseListener<Response> completion) {
        String url = "https://api.onegov.nsw.gov.au/oauth/client_credential/accesstoken?grant_type=client_credentials";

        // Prepare the header arguments
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Basic " + context.getString(R.string.base64Encode));
        headerMap.put("dataType", "json");
        JSONObjectRequestGET(url, headerMap, new OnResponseListener<Response>() {
            @Override
            public void onCompletion(Response res) {
                try {
                    if (res.isSuccess() && res.dataIsObject()) {
                        JSONObject jsonObject = res.getDataAsObject();
                        String authToken = jsonObject.getString("access_token");
                        Preferences.getInstance().put(Preferences.Key.OAUTH_TOKEN, authToken);
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

    private void JSONArrayRequestGET(String url, OnResponseListener<Response> completion) {
        JSONArrayRequestGET(url, Collections.<String, String> emptyMap(), completion);
    }

    private void JSONArrayRequestGET(final String url, final Map<String, String> headerMap, final OnResponseListener<Response> completion) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new com.android.volley.Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        // structure the response in a Response
                        Response res = new Response();

                        res.setSuccess(true);
                        // Embed response as is, the callback will handle the deserialization
                        res.setData(response);
                        completion.onCompletion(res);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Response res = new Response();
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
                    authToken(new OnResponseListener<Response>() {
                        @Override
                        public void onCompletion(Response res) {
                            String authToken = Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN);
                            headerMap.put("Authorization", "Bearer "+ authToken);
                            JSONObjectRequestGET(url, headerMap, completion);
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
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headerMap;
            }
        };

        // Hand the request over to the request queue
        VolleyQueue.getInstance(context).addToRequestQueue(jsonArrayRequest);
    }

    private void JSONObjectRequestGET(String url, OnResponseListener<Response> completion) {
        JSONObjectRequestGET(url, Collections.<String, String> emptyMap(), completion);
    }

    private void JSONObjectRequestGET(final String url, final Map<String, String> headerMap, final OnResponseListener<Response> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // structure the response in a Response
                        Response res = new Response();

                        res.setSuccess(true);
                        // Embed response as is, the callback will handle the deserialization
                        res.setData(response);
                        completion.onCompletion(res);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Response res = new Response();
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
                    authToken(new OnResponseListener<Response>() {
                        @Override
                        public void onCompletion(Response res) {
                            String authToken = Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN);
                            headerMap.put("Authorization", "Bearer "+ authToken);
                            JSONObjectRequestGET(url, headerMap, completion);
                        }
                    });

                } else if (error instanceof ServerError) {
                    try {
                        LOGE(TAG, new String(error.networkResponse.data,"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context,
                            "server1Error",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(context,
                            "networkError",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(context,
                            "parseError",
                            Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headerMap;
            }
        };

        // Hand the request over to the request queue
        VolleyQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void requestPOST(final String url, final Map<String, String> headerMap, final JSONObject jsonBody, final OnResponseListener<Response> completion) {
        requestPOST(url, headerMap, jsonBody, null, completion);
    }

    private void requestPOST(final String url, final Map<String, String> headerMap, final JSONObject jsonBody, RequestTag tag, final OnResponseListener<Response> completion) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // structure the response in a Response
                        Response res = new Response();

                        res.setSuccess(true);
                        // Embed response as is, the callback will handle the deserialization
                        res.setData(response);
                        completion.onCompletion(res);
                    }
                }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Response res = new Response();
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
                    authToken(new OnResponseListener<Response>() {
                        @Override
                        public void onCompletion(Response res) {
                            String authToken = Preferences.getInstance().getString(Preferences.Key.OAUTH_TOKEN);
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
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headerMap;
            }
        };

        if (tag != null) {
            jsonObjectRequest.setTag(tag.getTag());
        }
        // Hand the request over to the request queue
        VolleyQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private List<Price> toPriceObjects(JSONArray JSON, final int stationCode) {
        final SQLiteClient sqliteClient = new SQLiteClient(context);
        List<Price> prices = new ArrayList<>();

        JsonDeserializer<Price> deserializer = new JsonDeserializer<Price>() {
            @Override
            public Price deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject priceJson = json.getAsJsonObject();
                sqliteClient.open();
                FuelType fuelType = sqliteClient.getFuelType(priceJson.get("fueltype").getAsString());
                sqliteClient.close();
                return new Price(
                        stationCode,
                        fuelType,
                        priceJson.get("price").getAsDouble(),
                        priceJson.get("lastupdated").getAsString()
                );
            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Price.class, deserializer);
        Gson customGson = gsonBuilder.create();

        try {
            for (int i = 0; i < JSON.length(); i++) {
                prices.add(customGson.fromJson(JSON.get(i).toString(), Price.class));
            }
        } catch (JSONException e) {
            LOGE(TAG, "Error occurred processing JSONPrices");
            e.printStackTrace();
        }

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

    private Map<String, Average> toAverageObjects(JSONArray JSON) {

        final SQLiteClient sqliteClient = new SQLiteClient(context);
        Map<String, Average> averages = new HashMap<>();

        JsonDeserializer<Average> deserializer = new JsonDeserializer<Average>() {

            @Override
            public Average deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject averageJson = json.getAsJsonObject();
                sqliteClient.open();
                FuelType fuelType = sqliteClient.getFuelType(averageJson.get("Code").getAsString());
                sqliteClient.close();

                return new Average(
                        fuelType,
                        averageJson.get("Price").getAsDouble(),
                        averageJson.get("Variance").getAsDouble());
            }
        };
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Average.class, deserializer);
        Gson customGson = gsonBuilder.create();
        try {
            for (int i = 0; i < JSON.length(); i++) {
                Average average = customGson.fromJson(JSON.get(i).toString(), Average.class);
                averages.put(average.getFuelType().getCode(), average);
            }
        } catch (JSONException e) {
            LOGE(TAG, "Error occurred processing JSONAverages");
            e.printStackTrace();
        }
        return averages;
        }

    private List<Trend> toTrendObjects(JSONArray JSON) {

        final List<Trend> trend = new ArrayList<>();

        JsonDeserializer<Trend> deserializer = new JsonDeserializer<Trend>() {
            @Override
            public Trend deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject trendJson = json.getAsJsonObject();
                return new Trend(
                        trendJson.get("Period").getAsString(),
                        trendJson.get("Captured").getAsString(),
                        trendJson.get("Price").getAsDouble()
                );

            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Trend.class, deserializer);
        Gson customGson = gsonBuilder.create();
        try {
            for (int i = 0; i < JSON.length(); i++) {
                trend.add(customGson.fromJson(JSON.get(i).toString(), Trend.class));
            }
        } catch (JSONException e) {
            LOGE(TAG, "Error occurred processing JSONTrend");
            e.printStackTrace();
        }

        return trend;
    }

    public void cancelRequests(RequestTag tag) {
        VolleyQueue.getInstance(context).cancelRequests(tag);
    }
}

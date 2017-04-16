package com.petrolpatrol.petrolpatrol.datastore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.petrolpatrol.petrolpatrol.model.Brand;
import com.petrolpatrol.petrolpatrol.model.FuelType;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;

import java.util.ArrayList;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class SQLiteClient extends SQLiteOpenHelper {

    private static final String TAG = makeLogTag(SQLiteClient.class);

    private SQLiteDatabase databaseHandle;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PetrolPatrol.sqlite";

    private static final String TABLE_METADATA = "metadata";
    private static final String TABLE_BRANDS = "brands";
    private static final String TABLE_FUELTYPES = "fueltypes";
    private static final String TABLE_STATIONS = "stations";
    private static final String TABLE_PRICES = "prices";
    private static final String INDEX_STATIONS = "stations_index";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_BRAND = "brand";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_STATION_ID = "stationID";
    private static final String COLUMN_FUELTYPE_ID = "fuelTypeID";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_LAST_UPDATED = "last_updated";
    private static final String COLUMN_KEY = "key";
    private static final String COLUMN_VALUE = "value";

    private SQLiteClient(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQLiteClient(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String CREATE_TABLE_METADATA = "CREATE TABLE " + TABLE_METADATA + "("
                + COLUMN_KEY + " TEXT, "
                + COLUMN_VALUE + " TEXT, "
                + "UNIQUE (" + COLUMN_KEY + ")"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_METADATA);

        String CREATE_TABLE_BRANDS = "CREATE TABLE " + TABLE_BRANDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + "UNIQUE (" + COLUMN_NAME + ")"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_BRANDS);

        String CREATE_TABLE_FUELTYPES = "CREATE TABLE " + TABLE_FUELTYPES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_CODE + " TEXT NOT NULL, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + "UNIQUE (" + COLUMN_CODE + ", " + COLUMN_NAME + ")"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_FUELTYPES);

        String CREATE_TABLE_STATIONS = "CREATE TABLE " + TABLE_STATIONS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_ADDRESS + " TEXT NOT NULL, "
                + COLUMN_BRAND + " INTEGER, "
                + COLUMN_LATITUDE + " REAL NOT NULL, "
                + COLUMN_LONGITUDE + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_BRAND + ") REFERENCES " + TABLE_BRANDS + "(" + COLUMN_ID + ")"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_STATIONS);

        String CREATE_TABLE_PRICES = "CREATE TABLE " + TABLE_PRICES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_STATION_ID + " INTEGER, "
                + COLUMN_FUELTYPE_ID + " INTEGER, "
                + COLUMN_PRICE + " REAL NOT NULL, "
                + COLUMN_LAST_UPDATED + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_STATION_ID + ") REFERENCES " + TABLE_STATIONS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_FUELTYPE_ID + ") REFERENCES " + TABLE_FUELTYPES + "(" + COLUMN_ID + "),"
                + "UNIQUE (" + COLUMN_STATION_ID + ", " + COLUMN_FUELTYPE_ID + ")"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_PRICES);

        String CREATE_INDEX_STATIONS = "CREATE INDEX " + INDEX_STATIONS + " ON " + TABLE_STATIONS + "(" + COLUMN_ID + ")";
        sqLiteDatabase.execSQL(CREATE_INDEX_STATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        LOGI(TAG, "Upgrading databaseHandle from version " + oldVersion + " to " + newVersion + ", current data will be wiped.");

        clearDb(sqLiteDatabase);
        onCreate(sqLiteDatabase);
    }

    private void clearDb(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_METADATA);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANDS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FUELTYPES);
    }

    public void resetDatabase() {
        open();
        clearDb(databaseHandle);
        onCreate(databaseHandle);
        close();
    }

    public void open() {
        if (databaseHandle == null) {
            databaseHandle = getWritableDatabase();
        }
    }

    public void close() {
        if (databaseHandle != null) {
            databaseHandle.close();
        }
        databaseHandle = null;
    }

    /*
     * Metadata
     */

    public String getMetadata(String name) {
        String metadata = null;
        Cursor cursor = databaseHandle.query(TABLE_METADATA, null, COLUMN_KEY + " = \"" + name + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            metadata = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
        }
        cursor.close();
        return metadata;
    }

    public void setMetadata(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_KEY, key);
        contentValues.put(COLUMN_VALUE, value);
        databaseHandle.insertWithOnConflict(TABLE_METADATA, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /*
     * Brand
     */

    public Brand getBrand(int id) {
        Brand brand = null;
        Cursor cursor = databaseHandle.query(TABLE_BRANDS, null, COLUMN_ID + " = \"" + id + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            brand = new Brand(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        }
        cursor.close();
        return  brand;
    }

    public Brand getBrand(String name) {
        Brand brand = null;
        Cursor cursor = databaseHandle.query(TABLE_BRANDS, null, COLUMN_NAME + " = \"" + name + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            brand = new Brand(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));        }
        cursor.close();
        return  brand;
    }

    public List<Brand> getAllBrands() {
        List<Brand> allBrands = new ArrayList<>();
        Cursor cursor = databaseHandle.query(TABLE_BRANDS, null, null, null, null, null, null);
        Brand brand;
        while (cursor.moveToNext()) {
            brand = new Brand(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            allBrands.add(brand);
        }
        cursor.close();
        return allBrands;
    }

    public void insertBrand(Brand brand) {
        ContentValues contentValues = new ContentValues();
        if (brand.getId() != Brand.NO_ID) {
            contentValues.put(COLUMN_ID, brand.getId());
        }
        contentValues.put(COLUMN_NAME, brand.getName());
        databaseHandle.insertWithOnConflict(TABLE_BRANDS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /*
     * FuelType
     */

    public FuelType getFuelType(String code) {
        FuelType fuelType = null;
        Cursor cursor = databaseHandle.query(TABLE_FUELTYPES, null, COLUMN_CODE + " = \"" + code + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            fuelType = new FuelType(cursor.getInt(
                    cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CODE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        }
        cursor.close();
        return  fuelType;
    }

    public FuelType getFuelType(int id) {
        FuelType fuelType = null;
        Cursor cursor = databaseHandle.query(TABLE_FUELTYPES, null, COLUMN_ID + " = \"" + id + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            fuelType = new FuelType(cursor.getInt(
                    cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CODE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        }
        cursor.close();
        return  fuelType;
    }

    public List<FuelType> getAllFuelTypes() {
        List<FuelType> allFuelTypes = new ArrayList<>();
        Cursor cursor = databaseHandle.query(TABLE_FUELTYPES, null, null, null, null, null, null);
        FuelType fuelType;
        while (cursor.moveToNext()) {
            fuelType = new FuelType(cursor.getInt(
                    cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CODE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            );
            allFuelTypes.add(fuelType);
        }
        cursor.close();
        return allFuelTypes;
    }

    public void insertFuelType(FuelType fuelType) {
        ContentValues contentValues = new ContentValues();
        if (fuelType.getId() != FuelType.NO_ID) {
            contentValues.put(COLUMN_ID, fuelType.getId());
        }
        contentValues.put(COLUMN_CODE, fuelType.getCode());
        contentValues.put(COLUMN_NAME, fuelType.getName());
        databaseHandle.insertWithOnConflict(TABLE_FUELTYPES, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /*
     * Station
     */

    public Station getStation(int id) {
        Station station = null;
        Cursor cursor = databaseHandle.query(TABLE_STATIONS, null, COLUMN_ID + " = \"" + id + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            station = new Station(
                    getBrand(cursor.getInt(cursor.getColumnIndex(COLUMN_BRAND))), cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
            );
            for (Price price : getPricesByStation(station.getId())) {
                station.setPrice(price);
            }
        }
        cursor.close();
        return station;
    }

    public void insertStation(Station station) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, station.getId());
        contentValues.put(COLUMN_NAME, station.getName());
        contentValues.put(COLUMN_ADDRESS, station.getAddress());
        contentValues.put(COLUMN_BRAND, station.getBrand().getId());
        contentValues.put(COLUMN_LATITUDE, station.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, station.getLongitude());
        databaseHandle.insertWithOnConflict(TABLE_STATIONS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        for (Price price : station.getAllPrices()) {
            insertPrice(price);
        }
    }

    public List<Station> getAllStations() {
        List<Station> allStations = new ArrayList<>();
        Cursor cursor = databaseHandle.query(TABLE_STATIONS, null, null, null, null, null, null);
        Station station;
        while (cursor.moveToNext()) {
            station = new Station(
                    getBrand(cursor.getInt(cursor.getColumnIndex(COLUMN_BRAND))), cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
            );
            for (Price price : getPricesByStation(station.getId())) {
                station.setPrice(price);
            }
            allStations.add(station);
        }
        cursor.close();
        return allStations;
    }

    /*
     * Price
     */

    public void insertPrice(Price price) {
        ContentValues contentValues = new ContentValues();
        if (price.getId() != Price.NO_ID) {
            contentValues.put(COLUMN_ID, price.getId());
        }
        contentValues.put(COLUMN_STATION_ID, price.getStationID());
        contentValues.put(COLUMN_FUELTYPE_ID, price.getFuelType().getId());
        contentValues.put(COLUMN_PRICE, price.getPrice());
        contentValues.put(COLUMN_LAST_UPDATED, price.getLastUpdated());
        databaseHandle.insertWithOnConflict(TABLE_PRICES, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Price> getPricesByStation(int stationID) {
        List<Price> prices = new ArrayList<>();
        Cursor cursor = databaseHandle.query(TABLE_PRICES, null, COLUMN_STATION_ID + " = \"" + stationID + "\"", null, null, null, null);
        Price price;
        while (cursor.moveToNext()) {
            price = new Price(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_STATION_ID)),
                    getFuelType(cursor.getInt(cursor.getColumnIndex(COLUMN_FUELTYPE_ID))),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPDATED))
            );
            prices.add(price);
        }
        cursor.close();
        return prices;
    }

    public List<Price> getAllPrices() {
        List<Price> allPrices = new ArrayList<>();
        Cursor cursor = databaseHandle.query(TABLE_PRICES, null, null, null, null, null, null);
        Price price;
        while (cursor.moveToNext()) {
            price = new Price(cursor.getInt(cursor.getColumnIndex(COLUMN_STATION_ID)),
                    getFuelType(cursor.getInt(cursor.getColumnIndex(COLUMN_FUELTYPE_ID))),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPDATED))
            );
            allPrices.add(price);
        }
        cursor.close();
        return allPrices;
    }
}
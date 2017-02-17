package com.petrolpatrol.petrolpatrol.datastore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.petrolpatrol.petrolpatrol.model.Brand;
import com.petrolpatrol.petrolpatrol.model.FuelType;
import com.petrolpatrol.petrolpatrol.model.Station;

import java.util.ArrayList;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * Created by jason on 17/02/17.
 */
public class SQLiteClient extends SQLiteOpenHelper {

    private static final String TAG = makeLogTag(SQLiteClient.class);

    private SQLiteDatabase databaseHandle;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PetrolPatrol.sqlite";

    private static final String TABLE_BRANDS = "brands";
    private static final String TABLE_FUELTYPES = "fueltypes";
    private static final String TABLE_STATIONS = "stations";
    private static final String INDEX_STATIONS = "stations_index";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_BRAND = "brand";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_CODE = "code";

    public SQLiteClient(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
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

        String CREATE_INDEX_STATIONS = "CREATE INDEX " + INDEX_STATIONS + " ON " + TABLE_STATIONS + "(" + COLUMN_ID + ")";
        sqLiteDatabase.execSQL(CREATE_INDEX_STATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        LOGI(TAG, "Upgrading databaseHandle from version " + oldVersion + " to " + newVersion + ", current data will be wiped.");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANDS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FUELTYPES);
        onCreate(sqLiteDatabase);
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
     * Brand
     */

    public Brand getBrand(int id) {
        Brand brand = null;
        Cursor cursor = databaseHandle.query(TABLE_BRANDS, null, COLUMN_ID + " = \"" + id + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            brand = new Brand(cursor.getInt(0), cursor.getString(1));
        }
        cursor.close();
        return  brand;
    }

    public Brand getBrand(String name) {
        Brand brand = null;
        Cursor cursor = databaseHandle.query(TABLE_BRANDS, null, COLUMN_NAME + " = \"" + name + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            brand = new Brand(cursor.getInt(0), cursor.getString(1));
        }
        cursor.close();
        return  brand;
    }

    public List<Brand> getAllBrands() {
        List<Brand> allBrands = new ArrayList<Brand>();
        Cursor cursor = databaseHandle.query(TABLE_BRANDS, null, null, null, null, null, null);
        Brand brand;
        while (cursor.moveToNext()) {
            brand = new Brand(cursor.getInt(0), cursor.getString(1));
            allBrands.add(brand);
        }
        cursor.close();
        return allBrands;
    }

    public void insertBrand(Brand brand) {
        ContentValues contentValues = new ContentValues();
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
            fuelType = new FuelType(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
        }
        cursor.close();
        return  fuelType;
    }

    public FuelType getFuelType(int id) {
        FuelType fuelType = null;
        Cursor cursor = databaseHandle.query(TABLE_FUELTYPES, null, COLUMN_ID + " = \"" + id + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            fuelType = new FuelType(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
        }
        cursor.close();
        return  fuelType;
    }

    public List<FuelType> getAllFuelTypes() {

        List<FuelType> allFuelTypes = new ArrayList<FuelType>();
        Cursor cursor = databaseHandle.query(TABLE_FUELTYPES, null, null, null, null, null, null);
        FuelType fuelType;
        while (cursor.moveToNext()) {
            fuelType = new FuelType(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            allFuelTypes.add(fuelType);
        }
        cursor.close();
        return allFuelTypes;
    }

    public void insertFuelType(FuelType fuelType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CODE, fuelType.getCode());
        contentValues.put(COLUMN_NAME, fuelType.getName());
        databaseHandle.insertWithOnConflict(TABLE_FUELTYPES, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /*
     * FuelType
     */

    public Station getStation(int id) {
        Station station = null;
        Cursor cursor = databaseHandle.query(TABLE_STATIONS, null, COLUMN_ID + " = \"" + id + "\"", null, null, null, null);
        if (cursor.moveToFirst()) {
            station = new Station(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    getBrand(cursor.getInt(3)),
                    new Station.Location(cursor.getDouble(4),cursor.getDouble(5)));
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
    }
}

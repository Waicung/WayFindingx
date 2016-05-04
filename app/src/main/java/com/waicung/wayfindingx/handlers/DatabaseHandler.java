package com.waicung.wayfindingx.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.waicung.wayfindingx.models.LocationRecord;

import java.util.ArrayList;

/**
 * Created by waicung on 04/05/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    //database name
    public static final String DATABASE_NAME = "FindWay.db";
    //information of user table:(id,password,primary_user)
    //primary indicate if a user is the main user of the app, whose activity will be recorded
    public static final String LOCATION_TABLE_NAME = "locations";
    public static final String LOCATION_TIMESTAMP = "time_stamp";
    public static final String LOCATION_LAT = "latitude";
    public static final String LOCATION_LNG = "longitude";
    public static final String LOCATION_STEP = "step";
    public static final String LOCATION_EVENT = "event";
    //create user table query
    private static final String LOCATION_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + LOCATION_TABLE_NAME + " (" +
                    "id" + " INTEGER PRIMARY KEY," +
                    LOCATION_TIMESTAMP + " Long NOT NULL," +
                    LOCATION_LAT + " DOUBLE NOT NULL," +
                    LOCATION_LNG + " DOUBLE NOT NULL," +
                    LOCATION_STEP + " INT NOT NULL," +
                    LOCATION_EVENT + " TEXT);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        db.execSQL(LOCATION_TABLE_CREATE);

    }

    //method for user record insertion
    public boolean insertLocation (Double lagitude, Double longitude, long time, int step, String event){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCATION_LAT, lagitude);
        contentValues.put(LOCATION_LNG, longitude);
        contentValues.put(LOCATION_TIMESTAMP, time);
        contentValues.put(LOCATION_STEP, step);
        contentValues.put(LOCATION_EVENT, event);
        db.insert(LOCATION_TABLE_NAME, null, contentValues);
        return true;
    }

    //return all the record
    public ArrayList<LocationRecord> getData(){
        ArrayList<LocationRecord> locations = new ArrayList<>();
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + LOCATION_TABLE_NAME + " LIMIT 10", null);
        while (cursor.moveToNext()){
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(LOCATION_LAT)
            );
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(LOCATION_LNG)
            );
            long time = cursor.getLong(
                    cursor.getColumnIndexOrThrow(LOCATION_TIMESTAMP)
            );
            int step_number = cursor.getInt(
                    cursor.getColumnIndexOrThrow(LOCATION_STEP)
            );
            String event = cursor.getString(
                    cursor.getColumnIndexOrThrow(LOCATION_EVENT)
            );
            LocationRecord location = new LocationRecord(lat,lng,time,step_number,event);
            locations.add(location);
        }
        return locations;

    }

    public void  newTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        db.execSQL(LOCATION_TABLE_CREATE);
    }
}

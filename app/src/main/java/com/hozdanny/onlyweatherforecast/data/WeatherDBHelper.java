package com.hozdanny.onlyweatherforecast.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hozdanny.onlyweatherforecast.data.WeatherDBContract.LocationEntry;
import com.hozdanny.onlyweatherforecast.data.WeatherDBContract.WeatherEntry;

/**
 * Created by hoz.danny on 7/6/16.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "onlyWeatherForecast.db";

    private static final int DATABASE_VERSION = 1;


    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " +
                " );";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
                " UNIQUE (" + WeatherEntry.COLUMN_DATE + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        // Log.i("Database","SQL_LOCATION:"+SQL_CREATE_LOCATION_TABLE);
        // Log.i("Database","SQL_WEATHER:"+SQL_CREATE_WEATHER_TABLE);

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        // Database: SQL_LOCATION:CREATE TABLE location (
        // _id INTEGER PRIMARY KEY,location_setting TEXT UNIQUE NOT NULL,
        // city_name TEXT NOT NULL,
        // coord_lat REAL NOT NULL,
        // coord_long REAL NOT NULL  );

        db.execSQL(SQL_CREATE_WEATHER_TABLE);
        // Database: SQL_WEATHER:CREATE TABLE weather (
        // _id INTEGER PRIMARY KEY AUTOINCREMENT,
        // location_id INTEGER NOT NULL,
        // date INTEGER NOT NULL,
        // short_desc TEXT NOT NULL,
        // weather_id INTEGER NOT NULL,
        // min REAL NOT NULL,
        // max REAL NOT NULL,
        // humidity REAL NOT NULL,
        // pressure REAL NOT NULL,
        // wind REAL NOT NULL, degrees REAL NOT NULL,
        // FOREIGN KEY (location_id) REFERENCES location (_id),
        // UNIQUE (date, location_id) ON CONFLICT REPLACE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}

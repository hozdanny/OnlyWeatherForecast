package com.hozdanny.onlyweatherforecast.data;

import android.app.Application;
import android.test.AndroidTestCase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.HashSet;

/**
 * Created by hoz.danny on 7/7/16.
 */
public class TestDB extends AndroidTestCase {
    public final static String LOG_TAG=TestDB.class.getSimpleName();


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherDBContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherDBContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherDBContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherDBContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherDBContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherDBContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherDBContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

}

package com.hozdanny.onlyweatherforecast.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by hoz.danny on 7/7/16.
 */
public class WeatherContentProvider extends ContentProvider {
    private WeatherDBHelper mWeatherDBHelper;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder selectWeatherByLocation = new SQLiteQueryBuilder();
    ;

    static {
        selectWeatherByLocation.setTables(WeatherDBContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                WeatherDBContract.LocationEntry.TABLE_NAME + " ON " + WeatherDBContract.WeatherEntry.TABLE_NAME + "." +
                WeatherDBContract.WeatherEntry.COLUMN_LOC_KEY + " = " + WeatherDBContract.LocationEntry.TABLE_NAME + "." +
                WeatherDBContract.LocationEntry._ID);
    }


    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherDBContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, WeatherDBContract.PATH_WEATHER, WEATHER);
        uriMatcher.addURI(authority, WeatherDBContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        uriMatcher.addURI(authority, WeatherDBContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(authority, WeatherDBContract.PATH_LOCATION, LOCATION);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        mWeatherDBHelper = new WeatherDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            // "weather"
            case WEATHER: {
                cursor = mWeatherDBHelper.getReadableDatabase().query(
                        WeatherDBContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case WEATHER_WITH_LOCATION: {
                //weather data is form here.
                String locationSetting = WeatherDBContract.WeatherEntry.getLocationSettingFromUri(uri);
                long startDate = WeatherDBContract.WeatherEntry.getStartDateFromUri(uri);

                if (startDate == 0) {
                    //location.location_setting = ?
                    selection = WeatherDBContract.LocationEntry.TABLE_NAME +
                            "." + WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
                    selectionArgs = new String[]{locationSetting};
                } else {
                    //location.location_setting = ? AND date >= ?
                    selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
                    selection = WeatherDBContract.LocationEntry.TABLE_NAME +
                            "." + WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                            WeatherDBContract.WeatherEntry.COLUMN_DATE + " >= ? ";
                }
                //weather INNER JOIN location ON weather.location_id = location._id
                cursor = selectWeatherByLocation.query(mWeatherDBHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION_AND_DATE: {
                String locationSetting = WeatherDBContract.WeatherEntry.getLocationSettingFromUri(uri);
                long date = WeatherDBContract.WeatherEntry.getDateFromUri(uri);
                //location.location_setting = ? AND date = ?
                selection = WeatherDBContract.LocationEntry.TABLE_NAME +
                        "." + WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                        WeatherDBContract.WeatherEntry.COLUMN_DATE + " = ? ";
                cursor = selectWeatherByLocation.query(mWeatherDBHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case LOCATION: {
                cursor = mWeatherDBHelper.getReadableDatabase().query(
                        WeatherDBContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // if you don't call cursor#setNotificationUri(), your cursorLoader will not receive uri change notification.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);

        switch (match) {
            case WEATHER:
                return WeatherDBContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherDBContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherDBContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherDBContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mWeatherDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);

        Uri retUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(WeatherDBContract.WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = WeatherDBContract.WeatherEntry.buildWeatherUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LOCATION: {
                long _id = db.insert(WeatherDBContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = WeatherDBContract.LocationEntry.buildLocationUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherDBContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherDBContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherDBContract.WeatherEntry.COLUMN_DATE, WeatherDBContract.normalizeDate(dateValue));
        }
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mWeatherDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection) selection = "1";
        Uri retUri;
        switch (match) {
            case WEATHER: {
                rowsDeleted = db.delete(WeatherDBContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                rowsDeleted = db.delete(WeatherDBContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mWeatherDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WEATHER:
                normalizeDate(values);
                rowsUpdated = db.update(WeatherDBContract.WeatherEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(WeatherDBContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mWeatherDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);

        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherDBContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}

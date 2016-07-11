package com.hozdanny.onlyweatherforecast;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import com.hozdanny.onlyweatherforecast.data.WeatherDBContract;

/**
 * Created by hoz.danny on 7/5/16.
 */
public class ForecastFragment extends Fragment {
    public static final String TAG = ForecastFragment.class.getSimpleName();
    RecyclerView mRecyclerView;
    public static final String locationSetting = "foshan";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        ForecastAdapter mAdapter = new ForecastAdapter(data);
        StringBuffer buffer = new StringBuffer();
        buffer.append(mAdapter.getItemCount());
        mRecyclerView.setAdapter(mAdapter);


        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.i(TAG, "refresh clicked.");
                FetchWeatherTask fetchTask = new FetchWeatherTask();
                fetchTask.execute("foshan");
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;
            String format = "json";
            String units = "metric";
            int numOfDay = 7;
            String appID = "0d9fe4f55285434e61c29d4130a2ad82";

            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numOfDay))
                        .appendQueryParameter(APPID_PARAM, appID).build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Read response
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                    //no response;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                jsonStr = buffer.toString();
                Log.i(LOG_TAG, jsonStr);
                getDateFromJson(jsonStr, "foshan");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void getDateFromJson(String jsonStr, String location) {
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";
        final String OWM_LIST = "list";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";
        final String OWM_MESSAGE_CODE = "cod";
        try {
            JSONObject forecastJson = new JSONObject(jsonStr);
            // do we have an error?
            if (forecastJson.has(OWM_MESSAGE_CODE)) {
                int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK:
                        //no error
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        //404
                        return;
                    default:
                        //no response server down
                        return;
                }
            }

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> cVector = new Vector<ContentValues>(weatherArray.length());

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            for (int i = 0; i < weatherArray.length(); i++) {
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                JSONObject dayForecast = weatherArray.getJSONObject(i);
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherDBContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVector.add(weatherValues);
            }

            int inserted = 0;
            if (cVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVector.size()];
                cVector.toArray(cvArray);
                getActivity().getContentResolver().bulkInsert(WeatherDBContract.WeatherEntry.CONTENT_URI, cvArray);

            }
            Log.i(TAG, "bulk insert record " + cVector.size() + "into database");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId ;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = getActivity().getContentResolver().query(
                WeatherDBContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherDBContract.LocationEntry._ID},
                WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);


        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherDBContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);

        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(WeatherDBContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherDBContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherDBContract.LocationEntry.COLUMN_COORD_LONG, lon);

            // Finally, insert location data into the database.
            Uri insertedUri = getActivity().getContentResolver().insert(
                    WeatherDBContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();

        return locationId;
    }
}

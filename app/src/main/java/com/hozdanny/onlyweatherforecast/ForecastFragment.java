package com.hozdanny.onlyweatherforecast;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.hozdanny.onlyweatherforecast.data.WeatherDBContract;


/**
 * Created by hoz.danny on 7/5/16.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ForecastFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    private static final int FORECAST_LOADER = 0;

    //projection for date weather forecast in main activity
    private static final String[] FORECAST_COLUMNS = {
            WeatherDBContract.WeatherEntry.TABLE_NAME + "." + WeatherDBContract.WeatherEntry._ID,
            WeatherDBContract.WeatherEntry.COLUMN_DATE,
            WeatherDBContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherDBContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherDBContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherDBContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherDBContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherDBContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherDBContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mForecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.OnItemClickHandler() {
            @Override
            public void onItemClick(long date, ForecastAdapter.ViewHolder vh) {
                Log.i(TAG, "item clicked "+ date);
                String location = Utility.getPreferredLocation(getActivity());
                ((Callback)getActivity()).onItemSelected(WeatherDBContract.WeatherEntry.buildWeatherLocationWithDate(location
                        , date),vh);

            }
        });
        mRecyclerView.setAdapter(mForecastAdapter);
        return rootView;
    }

    //a call back that let main activity implement it to start detail activity
    public interface Callback {
        public void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder vh);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WeatherDBContract.WeatherEntry.COLUMN_DATE + " ASC";
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Log.i(TAG,"fragment title"+ locationSetting);
        Uri weatherForLocationUri = WeatherDBContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        Log.i(TAG, "Uri " + weatherForLocationUri.toString());
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }


    @Override
    public void onResume() {
        Log.i(TAG,"onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG,"onPause()");
        super.onPause();
    }


    public void onLocationChanged(){
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }
}

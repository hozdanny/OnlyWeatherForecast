package com.hozdanny.onlyweatherforecast;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hozdanny.onlyweatherforecast.sync.SyncAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, ForecastFragment.Callback {
    final static String TAG = MainActivity.class.getSimpleName();
    private AlertDialog dialog;
    private ArrayList<String> locationSet;
    private DrawerListAdapter mDrawerListAdapter;
    private static final String SELECTED_KEY = "selected_position";
    private ListView mDrawerListView;
    private Parcelable state = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //add location button
        ImageButton addLocationBtn = (ImageButton) findViewById(R.id.btn_add_location);
        addLocationBtn.setOnClickListener(this);
        ImageButton addCityBtn = (ImageButton)findViewById(R.id.btn_add_city);
        addCityBtn.setOnClickListener(this);

        //drawer
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.drawer_listview);
        locationSet = new ArrayList<>();
        locationSet.add("Beijing");
        locationSet.add("Foshan");
        mDrawerListAdapter = new DrawerListAdapter(this, R.layout.drawer_list_item, locationSet);
        mDrawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerListView.setAdapter(mDrawerListAdapter);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //set sharedPreference location and position
                SharedPreferences sp = getSharedPreferences(getString(R.string.sharedPrefName), MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                String location = mDrawerListAdapter.getPositionItem(position);
                editor.putString(getString(R.string.pref_location), location);
                editor.commit();
               // view.setSelected(true);
            }
        });
        //open and close drawer listener
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_forecast));
                forecastFragment.onLocationChanged();
                state = mDrawerListView.onSaveInstanceState();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                if (state != null) {
                    mDrawerListView.onRestoreInstanceState(state);
                }

            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //add location dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(R.layout.dialog_add_location);
        dialogBuilder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Dialog d = (Dialog) dialog;
                EditText location = (EditText) d.findViewById(R.id.dialog_edittext_location);
                if (location.getText() != null) {
                    locationSet.add(location.getText().toString());
                    mDrawerListAdapter.setLocationSet(locationSet);
                    location.setText("");
                    d.dismiss();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Dialog d = (Dialog) dialog;
                EditText location = (EditText) d.findViewById(R.id.dialog_edittext_location);
                location.setText("");
            }
        });
        dialog = dialogBuilder.create();


        SyncAdapter.initializeSyncAdapter(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_location:
                dialog.show();
                break;
            case R.id.btn_add_city:
                Intent intent = new Intent(this,SearchActivity.class);
                startActivity(intent);
                break;
            default:
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location))) {
            SyncAdapter.syncImmediately(this);
        }
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.sharedPrefName), MODE_PRIVATE);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.sharedPrefName), MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder vh) {
        Log.i(TAG, "on item selected "+ dateUri);
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(dateUri);
        startActivity(intent);
    }
}

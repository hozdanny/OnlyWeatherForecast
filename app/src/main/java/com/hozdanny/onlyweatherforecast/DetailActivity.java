package com.hozdanny.onlyweatherforecast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null){
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.weather_detail_container,detailFragment).commit();

        }
    }
}

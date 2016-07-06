package com.hozdanny.onlyweatherforecast;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by hoz.danny on 7/6/16.
 */
public class MyPreferenceFragment extends PreferenceFragment{
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}
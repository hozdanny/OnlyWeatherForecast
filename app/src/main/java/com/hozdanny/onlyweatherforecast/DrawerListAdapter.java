package com.hozdanny.onlyweatherforecast;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoz.danny on 7/19/16.
 */
public class DrawerListAdapter extends ArrayAdapter<String> {

    private int resourceId;
    private ArrayList<String> locationSet;

    public DrawerListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        resourceId = resource;
        locationSet = (ArrayList<String>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String location = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        TextView locationName = (TextView) view.findViewById(R.id.drawer_list_item_text);
        locationName.setText(location);
        return view;
    }

    public String getPositionItem(int position){
        return locationSet.get(position);
    }

    public void setLocationSet(ArrayList<String> locationSet){
        this.locationSet = locationSet;
    }
}

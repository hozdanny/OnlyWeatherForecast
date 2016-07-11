package com.hozdanny.onlyweatherforecast;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by hoz.danny on 7/7/16.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private String[] mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.list_item_forecast_textview);
        }
    }

    public ForecastAdapter(String[] mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public ForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_main, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ForecastAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset[position]);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}

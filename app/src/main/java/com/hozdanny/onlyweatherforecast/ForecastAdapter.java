package com.hozdanny.onlyweatherforecast;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hozdanny.onlyweatherforecast.data.WeatherDBContract;

/**
 * Created by hoz.danny on 7/7/16.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private OnItemClickHandler mOnItemClickHandler;

    private static final int TODAY_VIEW = 0;
    private static final int FUTURE_VIEW = 1;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;
        public final TextView mLocationView;


        public ViewHolder(View v) {
            super(v);
            mIconView = (ImageView) v.findViewById(R.id.list_item_icon);
            mDateView = (TextView) v.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) v.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) v.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) v.findViewById(R.id.list_item_low_textview);
            mLocationView = (TextView)v.findViewById(R.id.main_location_textview);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherDBContract.WeatherEntry.COLUMN_DATE);
            mOnItemClickHandler.onItemClick(mCursor.getLong(dateColumnIndex), this);
        }
    }

    //a interface for recyclerview item to do onclick action
    public static interface OnItemClickHandler {
        void onItemClick(long date, ForecastAdapter.ViewHolder vh);
    }

    public ForecastAdapter(Context mContext, ForecastAdapter.OnItemClickHandler mOnItemClickHandler) {
        this.mContext = mContext;
        this.mOnItemClickHandler = mOnItemClickHandler;
    }

    @Override
    public ForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case TODAY_VIEW: {
                    layoutId = R.layout.item_forecast_main_first;
                    break;
                }
                case FUTURE_VIEW: {
                    layoutId = R.layout.item_forecast_main;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        switch (getItemViewType(position)) {
            case TODAY_VIEW:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                holder.mIconView.setImageResource(defaultImage);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                holder.mIconView.setImageResource(defaultImage);
        }


        if (holder.mLocationView !=null){
            holder.mLocationView.setText(Utility.getPreferredLocation(mContext));
        }

        // Read date from cursor
        long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        holder.mDateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));
        String description = Utility.getStringForWeatherCondition(mContext, weatherId);
        holder.mDescriptionView.setText(description);
        holder.mDescriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highString = Utility.formatTemperature(mContext, high);
        holder.mHighTempView.setText(highString);
        holder.mHighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highString));
        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowString = Utility.formatTemperature(mContext, low);
        holder.mLowTempView.setText(lowString);
        holder.mLowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowString));
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        // mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TODAY_VIEW : FUTURE_VIEW;
    }
}

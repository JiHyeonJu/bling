package com.samsung.android.bling.history;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.samsung.android.bling.R;

public class TimeLineItemViewHolder extends HistoryViewHolder {

    private TextView mMonthView;
    private TextView mDayView;
    private TextView mWeekView;

    public TimeLineItemViewHolder(@NonNull View itemView, Context context) {
        super(itemView, context);

        mMonthView = itemView.findViewById(R.id.month_view);
        mDayView = itemView.findViewById(R.id.day_view);
        mWeekView = itemView.findViewById(R.id.week_view);
    }

    @Override
    public void setText(int position) {
        String month, day, week;

        switch (position) {
            case 0:
                month = "2020.02";
                day = "28 Today";
                week = "Fri";
                break;
            case 6:
                month = null;
                day = "27";
                week = "Thu";
                break;
            case 9:
                month = null;
                day = "10";
                week = "Mon";
                break;
            case 13:
                month = null;
                day = "9";
                week = "Sun";
                break;
            case 16:
                month = "2020.01";
                day = "22";
                week = "Wed";
                break;
            case 18:
                month = null;
                day = "7";
                week = "Tue";
                break;
            default:
                month = "";
                day = "";
                week = "";
                break;
        }

        if (month == null) {
            mMonthView.setVisibility(View.GONE);
        } else {
            mMonthView.setVisibility(View.VISIBLE);
            mMonthView.setText(month);
        }
        mDayView.setText(day);
        mWeekView.setText(week);
    }
}

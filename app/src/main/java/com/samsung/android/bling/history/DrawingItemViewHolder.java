package com.samsung.android.bling.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.samsung.android.bling.R;

public class DrawingItemViewHolder extends HistoryViewHolder {

    private Context mContext;

    private TextView mTimeView;
    private TextView mMembersView;
    private ImageView mDataImageView;

    public DrawingItemViewHolder(@NonNull View itemView, Context context) {
        super(itemView, context);

        mContext = context;

        mTimeView = itemView.findViewById(R.id.time_view);
        mMembersView = itemView.findViewById(R.id.members_view);
        mDataImageView = itemView.findViewById(R.id.data_image_view);
    }

    @Override
    public void setText(int position) {
        String time, members;
        Drawable drawable;

        switch (position) {
            case 3:
                time = "9:52";
                members = "RM, Jin";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_rmjin);
                break;
            case 4:
                time = "9:30";
                members = "Jin";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_jin);
                break;
            case 7:
                time = "11:15";
                members = "Jungkook";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_jungkook);
                break;
            case 8:
                time = "10:32";
                members = "V, Jungkook";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_vjungkook);
                break;
            case 11:
                time = "1:48";
                members = "Jimin, V";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_jiminv);
                break;
            case 12:
                time = "1:20";
                members = "V";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_v);
                break;
            case 15:
                time = "13:50";
                members = "RM, Jin";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_rmjin2);
                break;
            case 19:
                time = "1:20";
                members = "Suga";
                drawable = mContext.getDrawable(R.drawable.bling_history_drawing_suga);
                break;
            default:
                time = "";
                members = "";
                drawable = null;
                break;
        }
        mTimeView.setText(time);
        mMembersView.setText(members);
        mDataImageView.setBackground(drawable);
    }
}

package com.samsung.android.bling.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.samsung.android.bling.R;

public class TouchItemViewHolder extends HistoryViewHolder {

    private Context mContext;

    private TextView mTimeView;
    private TextView mMembersView;
    private ImageView mDataImageView;

    public TouchItemViewHolder(@NonNull View itemView, Context context) {
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
            case 1:
                time = "13:52";
                members = "RM, Jin";
                drawable = mContext.getDrawable(R.drawable.bling_history_touch_rmjin);
                break;
            case 2:
                time = "11:30";
                members = "RM, Jin";
                drawable = mContext.getDrawable(R.drawable.bling_history_touch_rmjin_2);
                break;
            case 5:
                time = "8:45";
                members = "RM";
                drawable = mContext.getDrawable(R.drawable.bling_history_touch_rm);
                break;
            case 10:
                time = "All day";
                members = "V";
                drawable = mContext.getDrawable(R.drawable.bling_history_touch_v);
                break;
            case 14:
                time = "All day";
                members = "Jin, Jungkook";
                drawable = mContext.getDrawable(R.drawable.bling_history_touch_jinjungkook);
                break;
            case 17:
                time = "All day";
                members = "J-hope";
                drawable = mContext.getDrawable(R.drawable.bling_history_touch_jhope);
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

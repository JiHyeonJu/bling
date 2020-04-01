package com.samsung.android.bling.history;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.android.bling.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "Bling/HistoryViewHolder";

    protected Context mContext;

    private View mDivider;
    private View mNewBadge;
    private View mVerticalLine;
    private LinearLayout mHistoryLayout;

    public HistoryViewHolder(@NonNull View itemView, Context context) {
        super(itemView);

        mContext = context;

        mDivider = itemView.findViewById(R.id.history_divider);
        mNewBadge = itemView.findViewById(R.id.is_new_view);
        mVerticalLine = itemView.findViewById(R.id.vertical_line);
        mHistoryLayout = itemView.findViewById(R.id.history_view_layout);
    }

    public void setText(int position) {

    }

    public void setDivider(boolean isLastDayItem, boolean isLast) {
        if (mDivider != null && mHistoryLayout != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHistoryLayout.getLayoutParams();
            if (isLastDayItem) {
                mDivider.setVisibility(View.VISIBLE);
                params.bottomMargin = mContext.getResources().getDimensionPixelSize(R.dimen.history_has_divider_item_margin_bottom);
                if (isLast) {
                    mDivider.setBackgroundColor(mContext.getColor(R.color.white));
                } else {
                    mDivider.setBackgroundColor(mContext.getColor(R.color.sectionColor));
                }
            } else {
                mDivider.setVisibility(View.GONE);
                params.bottomMargin = mContext.getResources().getDimensionPixelSize(R.dimen.history_item_margin_bottom);
            }
            Log.d(TAG, isLastDayItem + "," + isLast);
            mHistoryLayout.setLayoutParams(params);
        }
    }

    public void setNewBadge(boolean isNew) {
        if (mNewBadge != null) {
            if (isNew) {
                mNewBadge.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(R.color.new_badge_new)));
            } else {
                mNewBadge.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(R.color.new_badge_old)));
            }
        }
    }

    public void setVerticalLine(boolean isShow) {
        if (mVerticalLine != null) {
            if (isShow) {
                mVerticalLine.setVisibility(View.VISIBLE);
            } else {
                mVerticalLine.setVisibility(View.INVISIBLE);
            }
        }
    }
}

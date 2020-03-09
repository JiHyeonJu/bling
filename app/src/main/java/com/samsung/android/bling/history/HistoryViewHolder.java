package com.samsung.android.bling.history;

import android.content.Context;
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
    private View mIsNewView;
    private LinearLayout mHistoryLayout;

    public HistoryViewHolder(@NonNull View itemView, Context context) {
        super(itemView);

        mContext = context;

        mDivider = itemView.findViewById(R.id.history_divider);
        mIsNewView = itemView.findViewById(R.id.is_new_view);
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
}

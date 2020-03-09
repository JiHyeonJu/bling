package com.samsung.android.bling.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.android.bling.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private static final int TIME_LINE_VIEW_TYPE = 1;
    private static final int TOUCH_ITEM_VIEW_TYPE = 2;
    private static final int DRAWING_ITEM_VIEW_TYPE = 3;

    private Context mContext;

    public HistoryAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        HistoryViewHolder viewHolder;
        if (viewType == TIME_LINE_VIEW_TYPE) {
            view = layoutInflater.inflate(R.layout.history_time_line_view_layout, viewGroup, false);
            viewHolder = new TimeLineItemViewHolder(view, mContext);
        } else if (viewType == TOUCH_ITEM_VIEW_TYPE) {
            view = layoutInflater.inflate(R.layout.history_touch_item_view_layout, viewGroup, false);
            viewHolder = new TouchItemViewHolder(view, mContext);
        } else if (viewType == DRAWING_ITEM_VIEW_TYPE) {
            view = layoutInflater.inflate(R.layout.history_drawing_item_view_layout, viewGroup, false);
            viewHolder = new DrawingItemViewHolder(view, mContext);
        } else {
            viewHolder = null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.setText(position);
        holder.setDivider(position == 5 || position == 8 || position == 12 || position == 15 || position == 17 || position == 19,
                position == 19);
    }

    @Override
    public int getItemViewType(int position) {
        int historyViewType;
        switch (position) {
            case 0:
            case 6:
            case 9:
            case 13:
            case 16:
            case 18:
                // timeline
                historyViewType = TIME_LINE_VIEW_TYPE;
                break;
            case 1:
            case 2:
            case 5:
            case 10:
            case 14:
            case 17:
                // touch item
                historyViewType = TOUCH_ITEM_VIEW_TYPE;
                break;
            case 3:
            case 4:
            case 7:
            case 8:
            case 11:
            case 12:
            case 15:
            case 19:
                // drawing item
                historyViewType = DRAWING_ITEM_VIEW_TYPE;
                break;
            default:
                historyViewType = TOUCH_ITEM_VIEW_TYPE;
                break;
        }
        return historyViewType;
    }

    @Override
    public int getItemCount() {
        return 20;
    }
}

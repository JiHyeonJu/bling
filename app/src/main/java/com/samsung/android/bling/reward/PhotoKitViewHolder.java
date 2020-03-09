package com.samsung.android.bling.reward;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.samsung.android.bling.R;
import com.samsung.android.bling.util.Utils;

public class PhotoKitViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "Bling/PhotoKitViewHolder";

    private TextView mTitle;
    private TextView mSubTitle;
    private GridLayout mGridLayout;
    private View mDivider;

    private Context mContext;

    String[] mMemberName, mMemberUrl;

    public PhotoKitViewHolder(@NonNull View itemView, Context context, int members) {
        super(itemView);

        mContext = context;

        mTitle = itemView.findViewById(R.id.title);
        mSubTitle = itemView.findViewById(R.id.sub_text);
        mGridLayout = itemView.findViewById(R.id.grid_layout);
        mDivider = itemView.findViewById(R.id.photo_kit_divider);

        mMemberName = new String[members];
        mMemberUrl = new String[members];

        for (int i = 0; i < members; i++) {
            addDynamicView();
        }
    }

    private void addDynamicView() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.photo_kit_item, null);

        int displayWidth = Utils.getDisplayWidth((Activity) mContext)
                - mContext.getResources().getDimensionPixelSize(R.dimen.photo_kit_margin_horizontal);

        int imageSize = (int) Math.round(displayWidth * 0.3025);

        view.findViewById(R.id.image).getLayoutParams().width = imageSize;
        view.findViewById(R.id.image).getLayoutParams().height = imageSize;

        int paddingEnd = (displayWidth - (imageSize * 3)) / 2;

        Log.d(TAG, displayWidth + "," + imageSize + "," + paddingEnd);
        view.findViewById(R.id.photo_kit_layout).setPaddingRelative(0, 0, paddingEnd,
                mContext.getResources().getDimensionPixelSize(R.dimen.photo_kit_padding_bottom));

        mGridLayout.addView(view);
    }

    public void setTitle(String title, String subtitle) {
        mTitle.setText(title);
        mSubTitle.setText(subtitle);
    }

    public void setMemberName(String nameList) {
        if (nameList.length() > 0) {
            String[] names = nameList.split("\\|");

            for (int i = 0; i < names.length; i++) {
                TextView text = mGridLayout.getChildAt(i).findViewById(R.id.name);
                text.setText(names[i]);

                mMemberName[i] = names[i];
            }
        }
    }

    public void setPhotoKit(String idList, String id, String url) {
        if (idList.length() > 0) {
            String[] ids = idList.split("\\|");
            url = url.replace("_l_", "_s_");

            for (int i = 0; i < ids.length; i++) {
                //ImageView selected = mGridLayout.getChildAt(i).findViewById(R.id.selected);
                //Log.d(TAG, ids[i] + "," + id);

                if (ids[i].equals(id)) {    // 멤버아이디리스트에서 해당 포토키트의 아이디를 찾으면(등록된 포토키트 위치면)
                    mGridLayout.getChildAt(i).findViewById(R.id.locked).setVisibility(View.GONE);
                    ImageView imageView = mGridLayout.getChildAt(i).findViewById(R.id.image);
                    //selected.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(url).placeholder(R.drawable.bling_rewards_locked_bg).dontAnimate().into(imageView);

                    url = url.replace("_s_", "_m_");
                    mMemberUrl[i] = url;
                }
            }
        }
    }

    public void setPhotoKit(int memberCount) {
        for (int i = 0; i < memberCount; i++) {
            mGridLayout.getChildAt(i).findViewById(R.id.locked).setVisibility(View.VISIBLE);
            mGridLayout.getChildAt(i).findViewById(R.id.selected).setVisibility(View.GONE);
            mMemberUrl[i] = "";
        }
    }

    public void setDivider(boolean isLast) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mDivider.getLayoutParams();
        if (isLast) {
            mDivider.setBackgroundColor(mContext.getColor(R.color.white));
            params.height = mContext.getResources().getDimensionPixelSize(R.dimen.last_section_height);
        } else {
            mDivider.setBackgroundColor(mContext.getColor(R.color.sectionColor));
            params.height = mContext.getResources().getDimensionPixelSize(R.dimen.section_height);
        }
        mDivider.setLayoutParams(params);
    }

    private int getResId(Context context, String resName) {
        Resources res = context.getResources();
        return res.getIdentifier(resName, "id", context.getPackageName());
    }

    public ImageView getImageView(int index) {
        return mGridLayout.getChildAt(index).findViewById(R.id.image);
    }

    public ImageView getLockedView(int index) {
        return mGridLayout.getChildAt(index).findViewById(R.id.locked);
    }

    public String getMemberUrl(int index) {
        return mMemberUrl[index];
    }

    public String getMemberName(int index) {
        return mMemberName[index];
    }
}

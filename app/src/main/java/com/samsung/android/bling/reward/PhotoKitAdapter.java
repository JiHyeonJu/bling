package com.samsung.android.bling.reward;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.android.bling.R;
import com.samsung.android.bling.data.AlbumItemVo;
import com.samsung.android.bling.data.PhotoKitItemVo;

import java.util.ArrayList;

public class PhotoKitAdapter extends RecyclerView.Adapter<PhotoKitViewHolder> {

    private static final String TAG = "Bling/PhotoKitAdapter";

    private Context mContext;

    private int mAlbums;
    private int mMembers;

    private ArrayList<AlbumItemVo> mAlbumData;
    private ArrayList<PhotoKitItemVo> mPhotoKitData;

    public PhotoKitAdapter(Context context) {
        //Log.d(TAG + "PhotoKitAdapter()");
        mContext = context;

        Log.d(TAG, mAlbums + "," + mMembers);
    }

    @NonNull
    @Override
    public PhotoKitViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //Log.d(TAG + "onCreateViewHolder()");
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.photo_kit_view_layout, viewGroup, false);
        return new PhotoKitViewHolder(view, mContext, mMembers);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoKitViewHolder holder, int position) {
        //Log.d(TAG + "onBindViewHolder()");
        if (mAlbumData == null) {
            return;
        }

        View item = holder.itemView;

        String title = mAlbumData.get(position).getTitle();
        String subTitle = mAlbumData.get(position).getSubTitle();
        String albumUrl = mAlbumData.get(position).getAlbumImageUrl();
        String starName = mAlbumData.get(position).getStarName();
        String albumDate = mAlbumData.get(position).getAlbumDate();

        holder.setTitle(title, subTitle);
        holder.setMemberName(mAlbumData.get(position).getMemberNameList());
        holder.setPhotoKit(mAlbumData.get(position).getMemberCount());
        holder.setDivider(position == getItemCount() - 1);

        if (mPhotoKitData != null) {
            for (PhotoKitItemVo photoKit : mPhotoKitData) {
                if (position + 1 == photoKit.getAlbumCT()) {
                    //Log.d(TAG, "position:" + position);
                    holder.setPhotoKit(mAlbumData.get(position).getMemberIdList(), photoKit.getMemberId(), photoKit.getMemberImageUrl());
                }
            }

            for (int index = 0; index < mAlbumData.get(position).getMemberCount(); index++) {
                final int member = index;

                holder.getImageView(member).setOnClickListener(v -> {
                    if (holder.getLockedView(member).getVisibility() == View.VISIBLE) { // 등록 안된 놈이면
                        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
                        holder.getLockedView(member).startAnimation(shake);
                    } else {
                        Intent intent = new Intent(mContext, RewardDetailActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("subtitle", subTitle);
                        intent.putExtra("albumUrl", albumUrl);
                        intent.putExtra("memberUrl", holder.getMemberUrl(member));
                        intent.putExtra("memberName", holder.getMemberName(member));
                        intent.putExtra("starName", starName);
                        intent.putExtra("albumDate", albumDate);

                        mContext.startActivity(intent);

                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
            }
        } else {
            for (int index = 0; index < mAlbumData.get(position).getMemberCount(); index++) {
                final int member = index;

                holder.getImageView(member).setOnClickListener(v -> {
                    Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
                    holder.getLockedView(member).startAnimation(shake);
                });
            }
        }
    }

    public void setAlbumList(ArrayList<AlbumItemVo> list) {
        //Log.d(TAG + "setAlbumList()");

        if (list != null && list.size() > 0) {
            mAlbumData = list;

            mAlbums = list.size();
            mMembers = list.get(0).getMemberCount();

            notifyDataSetChanged();
        }
    }

    public void setPhotoKitList(ArrayList<PhotoKitItemVo> list) {
        if (list != null && list.size() > 0) {
            mPhotoKitData = list;

            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG + "getItemCount()");

        if (mAlbumData != null) {
            return mAlbumData.size();
        }
        return 0;
    }

    private int getResDrawable(Context context, String resName) {
        Resources res = context.getResources();
        return res.getIdentifier(resName, "drawable", context.getPackageName());
    }
}

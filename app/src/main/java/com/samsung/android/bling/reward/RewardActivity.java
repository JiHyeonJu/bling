package com.samsung.android.bling.reward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.samsung.android.bling.R;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.chart.ChartActivity;
import com.samsung.android.bling.data.AlbumItemVo;
import com.samsung.android.bling.data.AlbumVo;
import com.samsung.android.bling.data.PhotoKitItemVo;
import com.samsung.android.bling.data.PhotoKitVo;

import java.util.ArrayList;

public class RewardActivity extends Activity {
    private static final String TAG = "Bling/RewardActivity";

    private RecyclerView mRecyclerView;
    private PhotoKitAdapter mAdapter;

    private RetroClient retroClient;

    private ArrayList<AlbumItemVo> mAlbumList;
    private ArrayList<PhotoKitItemVo> mPhotoKitList;

    private String mId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reward);

        mId = getIntent().getStringExtra("ID");

        retroClient = RetroClient.getInstance(this).createBaseApi();

        getData();

        initView();
    }

    private void getData() {
        retroClient.getAlbumData(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "onCreate() onError : " + t.toString());
                t.printStackTrace();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                AlbumVo album = (AlbumVo) receivedData;
                mAlbumList = album.getList();

                mAdapter.setAlbumList(mAlbumList);

                for (AlbumItemVo item : mAlbumList) {
                    Log.d(TAG, item.getTitle());
                }
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "onCreate() onFailure : " + code);
            }
        });

        retroClient.getUserPhotoKitList(mId, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "onCreate() onError : " + t.toString());
                t.printStackTrace();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                PhotoKitVo photoKits = (PhotoKitVo) receivedData;
                mPhotoKitList = photoKits.getList();

                mAdapter.setPhotoKitList(mPhotoKitList);

                for (PhotoKitItemVo item : mPhotoKitList) {
                    Log.d(TAG, mPhotoKitList.size() + "," + item.getMemberId() + "," + item.getAlbumCT());
                }
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "onCreate() onFailure : " + code);
            }
        });
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.photo_kit_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new PhotoKitAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(this::onBackPressed, 250));

        findViewById(R.id.cheering_chart_btn).setOnClickListener(v -> {
            startActivity(new Intent(RewardActivity.this, ChartActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

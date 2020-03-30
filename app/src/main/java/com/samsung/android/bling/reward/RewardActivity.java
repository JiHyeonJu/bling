package com.samsung.android.bling.reward;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.samsung.android.bling.MyApplication;
import com.samsung.android.bling.R;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.chart.ChartActivity;
import com.samsung.android.bling.data.AlbumItemVo;
import com.samsung.android.bling.data.AlbumVo;
import com.samsung.android.bling.data.PhotoKitItemVo;
import com.samsung.android.bling.data.PhotoKitListVo;
import com.samsung.android.bling.data.PhotoKitVo;
import com.samsung.android.bling.util.Utils;

import java.util.ArrayList;

public class RewardActivity extends Activity {
    private static final String TAG = "Bling/RewardActivity";

    private static final String NO_PHOTOKIT = "-1";

    private RecyclerView mRecyclerView;
    private PhotoKitAdapter mAdapter;

    private RetroClient retroClient;

    private ArrayList<AlbumItemVo> mAlbumList;
    private ArrayList<PhotoKitItemVo> mPhotoKitList;

    int mSelectedPhotoKitAlbum, mSelectedPhotoKitMember;

    private String mId;

    private AlertDialog mPhotoKitDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reward);

        mId = getIntent().getStringExtra("ID");

        retroClient = RetroClient.getInstance(this).createBaseApi();

        initView();

        getData();
    }

    private void getData() {
        retroClient.getAlbumData(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "getData() getAlbumData onError : " + t.toString());
                t.printStackTrace();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                AlbumVo album = (AlbumVo) receivedData;
                mAlbumList = album.getList();

                mAdapter.setAlbumList(mAlbumList);

                /*for (AlbumItemVo item : mAlbumList) {
                    Log.d(TAG, item.getTitle());
                }*/
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "getData() getAlbumData onFailure : " + code);
            }
        });

        retroClient.getUserPhotoKitList(mId, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "getData() getUserPhotoKitList onError : " + t.toString());
                t.printStackTrace();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                PhotoKitListVo photoKits = (PhotoKitListVo) receivedData;
                mPhotoKitList = photoKits.getList();

                mAdapter.setPhotoKitList(mPhotoKitList);

                /*for (PhotoKitItemVo item : mPhotoKitList) {
                    Log.d(TAG, mPhotoKitList.size() + "," + item.getMemberId() + "," + item.getAlbumCT());
                }*/
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "getData() getUserPhotoKitList onFailure : " + code);
            }
        });

        setPhotoKit();
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

    private void setPhotoKit() {
        String nfcInfo = Utils.getPreference(getApplicationContext(), "nfcInfo");

        if (NO_PHOTOKIT.equals(nfcInfo)) {
            mAdapter.setSelectedPhotoKit(-1, -1);
        } else {
            retroClient.getPhotoKitDataFromNfc(nfcInfo, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "getData() getPhotoKitDataFromNfc onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    PhotoKitVo photoKit = ((PhotoKitVo) receivedData);

                    mSelectedPhotoKitAlbum = photoKit.getAlbumCT();
                    mSelectedPhotoKitMember = getIndexFromIdList(photoKit.getMemberIdList(), photoKit.getMemberId());

                    mAdapter.setSelectedPhotoKit(mSelectedPhotoKitAlbum, mSelectedPhotoKitMember);
                    //((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(mSelectedPhotoKitAlbum - 1, 200);
                    //mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), mSelectedPhotoKitAlbum - 1);
                    new Handler().postDelayed(() -> {
                        mRecyclerView.scrollToPosition(mSelectedPhotoKitAlbum - 1);
                    }, 5);

                    Log.d(TAG, "selectedPhotoKitAlbum : " + mSelectedPhotoKitAlbum + ", selectedPhotoKitMember" + mSelectedPhotoKitMember);
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "getData() getPhotoKitDataFromNfc onFailure : " + code);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        Utils.dismissDialog(mPhotoKitDialog);

        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("bling.service.action.NEW_PHOTOKIT")) {
                setPhotoKit();

                mPhotoKitDialog = Utils.showDialog(RewardActivity.this, R.layout.photo_kit_dialog);

                mPhotoKitDialog.findViewById(R.id.ok).setOnClickListener(v -> {
                    Utils.dismissDialog(mPhotoKitDialog);
                });
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter("bling.service.action.NEW_PHOTOKIT");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private int getIndexFromIdList(String idList, String id) {
        int index = 1;
        if (idList.length() > 0) {
            String[] ids = idList.split("\\|");
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].equals(id)) {    // 멤버아이디리스트에서 해당 포토키트의 아이디를 찾으면(등록된 포토키트 위치면)
                    index = i;
                    Log.d(TAG, "selected photokit memeber : " + index);
                    break;
                }
            }
        }
        return index;
    }
}
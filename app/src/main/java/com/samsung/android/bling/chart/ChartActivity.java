package com.samsung.android.bling.chart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.samsung.android.bling.R;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.data.AlbumItemVo;
import com.samsung.android.bling.data.AlbumVo;
import com.samsung.android.bling.history.HistoryActivity;
import com.samsung.android.bling.util.Utils;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {
    private static final String TAG = "Bling/ChartActivity";

    private RetroClient retroClient;

    private ArrayList<AlbumItemVo> mAlbumList;

    private boolean mIsStar;

    private AlertDialog mPhotoKitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        mIsStar = Utils.getIsStar(getApplicationContext());

        initView();

        //getData();

        // 일단 임시로 만들어놓음(시나리오용)
        addChartTest();
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

                addChart();
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "onCreate() onFailure : " + code);
            }
        });
    }

    private void initView() {
        if (mIsStar) {
            ((TextView) findViewById(R.id.cheering_description)).setText(getString(R.string.cheeringlights_description_star));
        } else {
            ((TextView) findViewById(R.id.cheering_description)).setText(getString(R.string.cheeringlights_description_fans));
        }

        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(this::onBackPressed, 250));
        // set logo height
        Double logoViewHeight = (Utils.getDisplayHeight(this)
                - getResources().getDimensionPixelSize(R.dimen.toolbar_height)
                - getResources().getDimensionPixelSize(R.dimen.toolbar_divider_height)) * 0.6;
        /*LinearLayout.LayoutParams params
                = (LinearLayout.LayoutParams) findViewById(R.id.cheering_logo_layout).getLayoutParams();
        params.height = (int) Math.round(logoViewHeight);*/
        findViewById(R.id.cheering_logo_layout).getLayoutParams().height = (int) Math.round(logoViewHeight);
        // set logo height
    }

    // add chart item dynamically
    private void addChart() {
        LinearLayout layout = findViewById(R.id.chart_layout);

        for (AlbumItemVo item : mAlbumList) {
            View view = getLayoutInflater().inflate(R.layout.chart_item, null);

            TextView titleView = view.findViewById(R.id.chart_item_title);
            titleView.setText(item.getTitle());

            TextView rateView = view.findViewById(R.id.chart_item_rate);
            rateView.setText("100%");

            SeekBar bar = view.findViewById(R.id.chart_item_bar);
            bar.setOnTouchListener((v, event) -> {
                return true;
            });
            bar.setProgress(100);

            LayerDrawable layerList = (LayerDrawable) bar.getProgressDrawable();
            ScaleDrawable scaleDrawable = (ScaleDrawable) layerList.getDrawable(0);
            GradientDrawable drawable = (GradientDrawable) scaleDrawable.getDrawable();
            drawable.setColor(Color.parseColor(item.getAlbumColor()));
            //bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(item.getAlbumColor())));

            layout.addView(view);
        }
    }

    // 나중에 지워질것
    private void addChartTest() {
        String[] title = {
                "MAP OF THE SOUL : 7",
                "MAP OF THE SOUL : PERSONA",
                "LOVE YOURSELF 結 ‘ANSWER’",
                "LOVE YOURSELF 轉 ‘TEAR’",
                "LOVE YOURSELF 承 ‘HER’"
        };
        String[] color = {"#017BCE", "#F77599", "#D2B4DA", "#151924", "#EAEAEA"};
        String[] text = {"56%", "24%", "11%", "6%", "3%"};
        int[] values = {94, 44, 22, 11, 7};

        LinearLayout layout = findViewById(R.id.chart_layout);

        for (int i = 0; i < 5; i++) {
            View view = getLayoutInflater().inflate(R.layout.chart_item, null);

            TextView titleView = view.findViewById(R.id.chart_item_title);
            titleView.setText(title[i]);

            TextView rateView = view.findViewById(R.id.chart_item_rate);
            rateView.setText(text[i]);

            SeekBar bar = view.findViewById(R.id.chart_item_bar);
            bar.setOnTouchListener((v, event) -> {
                return true;
            });
            bar.setProgress(values[i]);

            LayerDrawable layerList = (LayerDrawable) bar.getProgressDrawable();
            ScaleDrawable scaleDrawable = (ScaleDrawable) layerList.getDrawable(0);
            GradientDrawable drawable = (GradientDrawable) scaleDrawable.getDrawable();
            drawable.setColor(Color.parseColor(color[i]));

            layout.addView(view);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("bling.service.action.NEW_PHOTOKIT")) {
                mPhotoKitDialog = Utils.showDialog(ChartActivity.this, R.layout.photo_kit_dialog);

                mPhotoKitDialog.findViewById(R.id.ok).setOnClickListener(v -> {
                    Utils.dismissDialog(mPhotoKitDialog);
                });
            }
        }
    };

    @Override
    protected void onDestroy() {
        Utils.dismissDialog(mPhotoKitDialog);

        super.onDestroy();
    }

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
}

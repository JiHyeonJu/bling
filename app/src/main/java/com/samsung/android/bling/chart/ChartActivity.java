package com.samsung.android.bling.chart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.samsung.android.bling.util.Utils;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {
    private static final String TAG = "Bling/ChartActivity";

    private RetroClient retroClient;

    private ArrayList<AlbumItemVo> mAlbumList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        initView();

        getData();
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

            TextView title = view.findViewById(R.id.chart_item_title);
            title.setText(item.getTitle());

            TextView rate = view.findViewById(R.id.chart_item_rate);
            rate.setText("100%");

            SeekBar bar = view.findViewById(R.id.chart_item_bar);
            bar.setOnTouchListener((v, event) -> {
                return true;
            });
            bar.setProgress(100);
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(item.getAlbumColor())));

            layout.addView(view);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

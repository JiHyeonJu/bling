package com.samsung.android.bling.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.android.bling.R;
import com.samsung.android.bling.util.Utils;

public class HistoryActivity extends Activity {

    private RecyclerView mRecyclerView;
    private HistoryAdapter mAdapter;

    private AlertDialog mPhotoKitDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
    }

    private void initView() {
        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(this::onBackPressed, 250));

        mRecyclerView = findViewById(R.id.history_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new HistoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("bling.service.action.NEW_PHOTOKIT")) {
                mPhotoKitDialog = Utils.showDialog(HistoryActivity.this, R.layout.photo_kit_dialog);

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

package com.samsung.android.bling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.android.bling.account.SigninActivity;
import com.samsung.android.bling.util.BluetoothUtils;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = "Bling/SetupActivity";

    TextView mBtSettingView;
    ImageView mCheckParingView;
    Button mNextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mBtSettingView = findViewById(R.id.setup_bluetooth_link_view);
        mCheckParingView = findViewById(R.id.setup_paring_check_view);
        mNextBtn = findViewById(R.id.setup_next_btn);

        mBtSettingView.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        });

        mNextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SetupActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (BluetoothUtils.isAleardyPaired(this)) {
            mCheckParingView.setImageTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
            mNextBtn.setAlpha(1);
            Log.d(TAG, "already paired");
        } else {
            mCheckParingView.setImageTintList(ColorStateList.valueOf(getColor(R.color.setupUncheckedColor)));
            mNextBtn.setAlpha(0.4f);
            Log.d(TAG, "not paired");
        }
    }
}

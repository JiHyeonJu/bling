package com.samsung.android.bling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.samsung.android.bling.account.SigninActivity;
import com.samsung.android.bling.util.BluetoothUtils;

public class SplashActivity extends Activity {
    private static final String TAG = "Bling/SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //findViewById(R.id.blink1).startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink1));
        //findViewById(R.id.blink2).startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink2));

        new Handler().postDelayed(() -> {
            Intent intent;

            if (BluetoothUtils.isAleardyPaired(this)) {
                intent = new Intent(SplashActivity.this, SigninActivity.class);
                Log.d(TAG, "already paired");
            } else {
                intent = new Intent(SplashActivity.this, SetupActivity.class);
                Log.d(TAG, "not paired");
            }

            SplashActivity.this.startActivity(intent);
            SplashActivity.this.finish();

            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }, 1000);
    }
}

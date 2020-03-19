package com.samsung.android.bling;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.samsung.android.bling.service.BlingService;

public class MyApplication extends Application {

    private static final String TAG = "Bling/MyApplication";

    public static Intent sServiceIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "jjh Application start");
        sServiceIntent = new Intent(getApplicationContext(), BlingService.class);
    }

    public static Intent getServiceIntent() {
        return sServiceIntent;
    }
}

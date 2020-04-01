package com.samsung.android.bling;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {

    private static final String TAG = "Bling/MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "jjh Application start");
    }
}

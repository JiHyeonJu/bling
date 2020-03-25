package com.samsung.android.bling;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.samsung.android.bling.service.BlingService;

public class MyApplication extends Application {

    private static final String TAG = "Bling/MyApplication";

    private static Intent sServiceIntent;

    private static String sPhotoKitNfc = "-1";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "jjh Application start");
        sServiceIntent = new Intent(getApplicationContext(), BlingService.class);
    }

    public static String getPhotoKitNfc() {
        return sPhotoKitNfc;
    }

    public static void setPhotoKitNfc(String nfcInfo) {
        sPhotoKitNfc = nfcInfo;
    }

    public static Intent getServiceIntent() {
        return sServiceIntent;
    }
}

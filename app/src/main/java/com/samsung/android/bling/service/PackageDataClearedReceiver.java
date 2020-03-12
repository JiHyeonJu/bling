/*
package com.samsung.android.bling.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.android.bling.util.BluetoothUtils;

public class PackageDataClearedReceiver extends BroadcastReceiver {
    private static final String TAG = "Bling/PackageDataClearedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_PACKAGE_DATA_CLEARED:
                Log.d(TAG, "data cleared");
                BluetoothUtils.unpairDevice();
                break;
            case Intent.ACTION_PACKAGE_FULLY_REMOVED:
                Log.d(TAG, "removed app");
                break;
            case Intent.ACTION_PACKAGE_REPLACED:
                Log.d(TAG, "updated app");
                break;
        }
    }
}
*/

package com.samsung.android.bling.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.samsung.android.bling.MyApplication;
import com.samsung.android.bling.util.Utils;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Bling/BluetoothBroadcastReceiver";

    private static final String FIRST_LOGIN = "-1";

    public static final String BT_NAME = "Bling";    //7C:96:D2:25:DE:1D
    public static final String BT_ADDRESS = "D0:E3:25:85:F7:BE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, action);
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        Intent newIntent = new Intent("bling.service.action.BT_CONNECTION_CHANGED");

        switch (action) {
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled() && Utils.isMyServiceRunning(context, BlingService.class)) {
                    Log.d(TAG, "Bluetooth Off : Stop service");

                    newIntent.putExtra("bt_status", "disconnect");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(newIntent);

                    context.stopService(new Intent(context.getApplicationContext(), BlingService.class));
                }
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결
                String id = Utils.getPreference(context.getApplicationContext(), "ID");

                //if (BT_NAME.equals(device.getName()) && !FIRST_LOGIN.equals(id)) {
                if ((BT_NAME.equals(device.getName()) || "AirPods".equals(device.getName())) && !FIRST_LOGIN.equals(id)) {
                    Log.d(TAG, "ACTION_ACL_CONNECTED : Start service");

                    newIntent.putExtra("bt_status", "connect");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(newIntent);

                    context.startForegroundService(new Intent(context.getApplicationContext(), BlingService.class));
                }
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐
                //if (BT_NAME.equals(device.getName())) {
                if (BT_NAME.equals(device.getName()) || "AirPods".equals(device.getName())) {
                    Log.d(TAG, "ACTION_ACL_DISCONNECTED : Stop service");

                    newIntent.putExtra("bt_status", "disconnect");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(newIntent);

                    context.stopService(new Intent(context.getApplicationContext(), BlingService.class));
                }
                break;
        }
    }
}
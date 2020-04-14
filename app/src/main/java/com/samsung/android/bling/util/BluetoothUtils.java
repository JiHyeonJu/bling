package com.samsung.android.bling.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothUtils {
    private static final String TAG = "Bling/BluetoothUtils";

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_ANO_UUID = UUID.fromString("6e400012-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final int REQUEST_ENABLE_BT = 1;
    public static final String BT_NAME = "Bling";    //7C:96:D2:25:DE:1D
    public static final String BT_ADDRESS = "D0:E3:25:85:F7:BE";

    public static void checkBluetooth(Context context) {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(context, "Does not support.", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        } else {
            // 기기의 블루투스 모듈이 활성화됐는지 체크
            // isEnabled() 활성화면 true, 비활성화면 false 리턴
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) context).startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            } else {
                //selectDevice(context);
            }
        }
    }

    public static BluetoothDevice getTargetDevice() {
        BluetoothDevice targetDevice = null;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (BT_NAME.equals(device.getName())) {
                    targetDevice = device;
                }
                //Log.d(TAG, device.getAddress().toString());
            }
        }

        //targetDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(BT_ADDRESS);
        return targetDevice;
    }

    public static boolean isAleardyPaired(Context context) {
        BluetoothDevice mDevice = getTargetDevice();

        return mDevice != null;
    }

    public static boolean isBlingConnected() {
        BluetoothDevice mDevice = getTargetDevice();

        if (mDevice != null) {
            try {
                Method method = mDevice.getClass().getMethod("isConnected", (Class[]) null);
                boolean connected = (boolean) method.invoke(mDevice, (Object[]) null);
                return connected;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return false;
    }

    public static void unpairDevice() {
        BluetoothDevice mDevice = getTargetDevice();

        if (mDevice != null) {
            try {
                Method method = mDevice.getClass().getMethod("removeBond", (Class[]) null);
                method.invoke(mDevice, (Object[]) null);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    public static void writeCharacteristic_Data(BluetoothGatt bluetoothGatt, byte[] data) {
        if (bluetoothGatt != null) {
            BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
            if (service == null) {
                Log.d(TAG, "ERROR - no service");
                return;
            }
            BluetoothGattCharacteristic Char = service.getCharacteristic(RX_UUID);
            if (Char == null) {
                Log.d(TAG, "ERROR - no characteristic");
                return;
            }
            Char.setValue(data);

            if (!bluetoothGatt.writeCharacteristic(Char)) {
                Log.d(TAG, "write FAILED");
            }
        }
    }
}
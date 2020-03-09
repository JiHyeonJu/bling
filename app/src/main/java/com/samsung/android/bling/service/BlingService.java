package com.samsung.android.bling.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.account.SigninActivity;
import com.samsung.android.bling.util.BluetoothUtils;
import com.samsung.android.bling.R;
import com.samsung.android.bling.util.Utils;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlingService extends Service {
    private static final String TAG = "Bling/BlingService";

    BluetoothGatt mBluetoothGatt = null;
    BluetoothDevice mTargetdevice = null;

    private MqttClient mMqttClient;

    IBinder mBinder = new BTBinder();

    private RetroClient retroClient;

    private boolean mIsStar;
    private String mStarId = "1";

    public class BTBinder extends Binder {
        public BlingService getService() { // 서비스 객체를 리턴
            return BlingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTED:
                    Log.d(TAG, "onConnectionStateChange - STATE_CONNECTED");
                    mBluetoothGatt.discoverServices();
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    Log.d(TAG, "onConnectionStateChange - STATE_DISCONNECTED");
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = mBluetoothGatt.getService(BluetoothUtils.SERVICE_UUID);
                if (service == null) {
                    Log.d(TAG, "Service not available");
                    return;
                }
                List<BluetoothGattCharacteristic> charlist = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : charlist) {
                    Log.d(TAG, ("chars : " + characteristic.getUuid().toString()));
                }

                BluetoothGattCharacteristic Char = service.getCharacteristic(BluetoothUtils.TX_UUID);
                if (Char == null) {
                    Log.d(TAG, "TX Char UUID not available");
                    return;
                }

                mBluetoothGatt.setCharacteristicNotification(Char, true);
                BluetoothGattDescriptor desc = Char.getDescriptor(BluetoothUtils.CCCD);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(desc);

                Log.d(TAG, "Service discover OK");
            } else {
                Log.d(TAG, "GATT Failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(BluetoothUtils.TX_UUID)) {
                final byte[] data = characteristic.getValue();
                Log.d(TAG, "datalen : " + data.length);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (descriptor.getCharacteristic().getUuid().equals(BluetoothUtils.TX_UUID)) {
                Log.d(TAG, "onDescriptorWrite()");

                /*byte[] tx_data = new byte[8];
                tx_data[0] = (byte) 0xEA;
                tx_data[1] = (byte) 1;
                writeData(tx_data, BluetoothUtils.RX_UUID);*/
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mIsStar = Utils.getIsStar(getApplicationContext());
        Log.d(TAG, "isStar?" + mIsStar);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        mTargetdevice = BluetoothUtils.getTargetDevice();

        String msg = getString(R.string.service_notification_msg);
        NotificationCompat.Builder notificationBuilder = Utils.showNotification(this,
                1002, "Bling", msg);
        startForeground(1002, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (mMqttClient != null && mMqttClient.isConnected()) {
            try {
                mMqttClient.unsubscribe("/bling/star/" + mStarId + "/conn");
                mMqttClient.disconnect();
                mMqttClient = null;
            } catch (Exception e) {
                Log.d(TAG, "Error while mqtt disconnecting");
                e.printStackTrace();

            }
        }

        updateStarStatus(mIsStar, mStarId, "off");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        mqttSubscribe();
        updateStarStatus(mIsStar, mStarId, "on");

        if (mTargetdevice != null) {
            Log.d(TAG, "attempting connect - " + mTargetdevice.getName() + " , " + mTargetdevice.getAddress());
            mBluetoothGatt = mTargetdevice.connectGatt(getApplicationContext(), false, mGattCallback);
        } else {
            //Toast.makeText(this, "no target device", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "NO DEV");
        }

        return START_STICKY;
    }

    private void updateStarStatus(Boolean isStar, String Id, String msg) {
        if (isStar) {
            HashMap<String, Object> parameters = new HashMap<>();

            parameters.put("member_id", Id);
            parameters.put("member_conn_state", msg);

            retroClient.updateStarConnection(Id, parameters, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "setStatusView() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Log.d(TAG, "setStatusView() update success! : " + code);
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "setStatusView() jjh onFailure : " + errorData);
                }
            });
        }
    }

    private void writeData(byte[] data, UUID uuid) {
        BluetoothUtils.writeCharacteristic_Data(mBluetoothGatt, data, uuid);
    }

    public void sendColorToLed(int currentColor) {
        byte[] tx_data = new byte[8];
        byte[] isOn = {1, 1, 1, 1, 1, 1, 1, 1, 1};

        tx_data[0] = (byte) 0x20; // intensity control protocol
        tx_data[1] = (byte) (Color.red(currentColor) & 0xFF);
        tx_data[2] = (byte) (Color.green(currentColor) & 0xFF);
        tx_data[3] = (byte) (Color.blue(currentColor) & 0xFF);
        tx_data[4] = (byte) (isOn[0] | isOn[1] << 1 | isOn[2] << 2);
        tx_data[5] = (byte) (isOn[3] | isOn[4] << 1 | isOn[5] << 2);
        tx_data[6] = (byte) (isOn[6] | isOn[7] << 1 | isOn[8] << 2);

        writeData(tx_data, BluetoothUtils.RX_UUID);
    }

    public void sendColorToLight2(int currentColor) {
        byte[] b = new byte[3];
        b[0] = (byte) (currentColor & 0xFF);
        b[1] = (byte) ((currentColor >> 8) & 0xFF);
        b[2] = (byte) ((currentColor >> 16) & 0xFF);

        writeData(b, BluetoothUtils.RX_ANO_UUID);
    }

    public void sendbrightnessToLed(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) (1);
        b[1] = (byte) (value & 0xFF);

        writeData(b, BluetoothUtils.RX_ANO_UUID);
    }

    public void setOnOffLight(int value) {
        byte[] tx_data = new byte[8];
        tx_data[0] = (byte) 0xEA;
        tx_data[1] = (byte) value;

        writeData(tx_data, BluetoothUtils.RX_UUID);
    }

    public void intensityControl(int r, int g, int b) {
        byte[] tx_data = new byte[8];
        byte[] isOn = {1, 1, 1, 1, 1, 1, 1, 1, 1};

        tx_data[0] = (byte) 0x20; // intensity control protocol
        tx_data[1] = (byte) (r & 0xFF);
        tx_data[2] = (byte) (g & 0xFF);
        tx_data[3] = (byte) (b & 0xFF);
        tx_data[4] = (byte) (isOn[0] | isOn[1] << 1 | isOn[2] << 2);
        tx_data[5] = (byte) (isOn[3] | isOn[4] << 1 | isOn[5] << 2);
        tx_data[6] = (byte) (isOn[6] | isOn[7] << 1 | isOn[8] << 2);

        writeData(tx_data, BluetoothUtils.RX_UUID);
    }

    private void mqttConnect() {
        if (mMqttClient == null || !mMqttClient.isConnected()) {
            try {
                mMqttClient = new MqttClient("tcp://ec2-52-79-216-28.ap-northeast-2.compute.amazonaws.com:1883", MqttClient.generateClientId(), null);
                mMqttClient.connect();
            } catch (Exception e) {
                Log.d(TAG, "Error while mqtt connecting");
                e.printStackTrace();
            }
        }
    }

    public void mqttSubscribe() {
        Log.d("jjh", "mqttSubscribe");
        mqttConnect();

        try {
            mMqttClient.subscribe("/bling/star/" + mStarId + "/conn");
            mMqttClient.subscribe("/bling/star/" + mStarId + "/msg/touch");
            mMqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    mqttConnect();
                    Log.d(TAG, "connectionLost() Mqtt ReConnect");
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Intent newIntent;
                    if (topic.equals("/bling/star/" + mStarId + "/conn")) {
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(message.toString());

                        String data = jsonObject.get("msg_data").getAsString();

                        newIntent = new Intent("bling.service.action.STAR_CONNECTION_CHANGED");
                        newIntent.putExtra("msg", data);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);

                        Log.d(TAG, "Mqtt messageArrived() conn data : " + data);
                    } else if (topic.equals("/bling/star/" + mStarId + "/msg/touch")) {
                        if ("1".equals(message.toString())) {
                            sendColorToLed(Color.WHITE);
                        } else {
                            sendColorToLed(Color.YELLOW);
                            /*int color = ColorPickerPreferenceManager.getInstance(getApplicationContext())
                                    .getColor("blingColorPicker", getColor(R.color.colorPrimary));
                            int brightness = Integer.parseInt(Utils.getPreference(getApplicationContext(), "brightness"));

                            float[] hsv = new float[3];
                            Color.colorToHSV(color, hsv);
                            hsv[2] = (float) brightness / 100000000;

                            sendColorToLed(Color.HSVToColor(hsv));*/
                        }

                        Log.d(TAG, "Mqtt messageArrived() touch : " + message.toString());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (Exception e) {
            Log.d(TAG, "mqttSubscribe() : error");
            e.printStackTrace();
        }
    }

    public void mqttPublish(String data) {
        mqttConnect();

        try {
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/touch", new MqttMessage(data.getBytes()));
            Log.d(TAG, "star publish " + "/bling/star/" + mStarId + "/msg/touch" + data);
        } catch (Exception e) {
            Log.d(TAG, "mqttPublish() : error");
            e.printStackTrace();
        }
    }
}
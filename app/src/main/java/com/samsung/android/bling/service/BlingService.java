package com.samsung.android.bling.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samsung.android.bling.MainActivity;
import com.samsung.android.bling.MyApplication;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.util.BluetoothUtils;
import com.samsung.android.bling.R;
import com.samsung.android.bling.util.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
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
    private String mMemberId;

    public int mMemberColor;
    private int mCurrentColor;
    private int mBrightness;

    private boolean mIsDrawing = false;

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
                List<BluetoothGattService> servicelist = mBluetoothGatt.getServices();
                for (BluetoothGattService service : servicelist) {
                    Log.d(TAG, "service - " + service.getUuid().toString());

                    if (service.getUuid().equals(BluetoothUtils.SERVICE_UUID)) {
                        List<BluetoothGattCharacteristic> charlist = service.getCharacteristics();

                        for (BluetoothGattCharacteristic Char : charlist) {
                            Log.d(TAG, ("chars : " + Char.getUuid().toString()));

                            if (Char.getUuid().equals(BluetoothUtils.TX_UUID)) {
                                mBluetoothGatt.setCharacteristicNotification(Char, true);
                                BluetoothGattDescriptor desc = Char.getDescriptor(BluetoothUtils.CCCD);

                                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(desc);

                                Log.d(TAG, "BLING TX UUID OK");
                            }
                        }
                        Log.d(TAG, "BLING SERVICE UUID OK");
                    }
                }
            } else {
                Log.d(TAG, "GATT Failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(BluetoothUtils.TX_UUID)) {
                final byte[] data = characteristic.getValue();
                //Log.d(TAG, "datalen : " + data.length);

                switch (data[0]) {
                    case (byte) 0xCC:
                        // 스타의 경우 터치할때 자기꺼 조명 빛을 자기꺼 컬러로 바꾼다
                        if (mIsStar && !mIsDrawing) {
                            Log.d(TAG, "touch set my color" + Utils.getHexCode(mMemberColor));
                            byte[] tx_data = new byte[4];
                            tx_data[0] = (byte) 0xCC;

                            int color = mMemberColor;
                            tx_data[1] = (byte) (Color.red(color) & 0xFF);
                            tx_data[2] = (byte) (Color.green(color) & 0xFF);
                            tx_data[3] = (byte) (Color.blue(color) & 0xFF);

                            writeData(tx_data);
                        }
                        break;
                    case (byte) 0xAA:
                        // 스타가 손을 대면 publish 1, 떼면 publish 0
                        if (mIsStar && !mIsDrawing) {
                            Log.d(TAG, "touch data : " + data[1] + mIsStar);
                            if (1 == data[1]) {
                                mqttTouchPublish("1|" + mMemberColor);
                            } else {
                                mqttTouchPublish("0");
                            }
                        }
                        break;
                    case (byte) 0xD0:
                        if (mIsStar) {
                            if (data[1] == 1) {
                                // 드로잉 모드 시작
                                mIsDrawing = true;
                                mqttDrawingModePublish("1");
                            } else if (data[1] == 0) {
                                // 드로잉 모드 끝
                                mIsDrawing = false;
                                mqttDrawingModePublish("2");
                            }
                        }
                        break;
                    case (byte) 0xDA:
                        // 기기로부터 스타의 드로잉이 들어옴
                        if (mIsStar && mIsDrawing) {
                            int xpos, ypos, action;

                            xpos = (data[2] & 0xFF);
                            xpos <<= 8;
                            xpos |= (data[1] & 0xFF);
                            ypos = (data[4] & 0xFF);
                            ypos <<= 8;
                            ypos |= (data[3] & 0xFF);
                            action = data[5];
                            Log.d(TAG, "X" + xpos + " Y" + ypos);
                            xpos *= 2;
                            ypos *= 2;

                            // 세팅 캔버스에 그리기위해 브로드캐스트, 테스트용으로 없어져도 됨
                            Intent newIntent = new Intent("bling.service.action.STAR_DRAWING");
                            newIntent.putExtra("msg", action + "|" + xpos + "|" + ypos + "|" + mMemberId);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);

                            mqttDrawingPublish(action + "|" + xpos + "|" + ypos + "|" + mMemberId);

                            Log.d(TAG, "drawing now : " + action + "|" + xpos + "|" + ypos + "|" + mMemberId);
                        }
                        break;
                    case (byte) 0xCD:
                        // NFC 태그 새로 읽혔을때
                        int len = data[2];
                        byte[] b = new byte[255];
                        if (data[1] > 0) // detected
                        {
                            for (int i = 0; i < len; i++) b[i] = data[3 + i];
                            String str = new String(b);
                            str = str.trim();

                            Log.d(TAG, "nfc receive len: " + len + " data: " + str);
                            MyApplication.setPhotoKitNfc(str);
                        } else {
                            Log.d(TAG, "nfc not detected");
                            MyApplication.setPhotoKitNfc("-1");
                        }
                        break;
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            if (descriptor.getCharacteristic().getUuid().equals(BluetoothUtils.TX_UUID)) {
                Log.d(TAG, "onDescriptorWrite()");
            }
        }
    };


    BroadcastReceiver powerSaverChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Utils.showNotification(BlingService.this, false,
                    1004, "Bling", action + isDozing(BlingService.this));
        }
    };

    @TargetApi(23)
    private static boolean isDozing(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isDeviceIdleMode() &&
                !powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mIsStar = Utils.getIsStar(getApplicationContext());
        mMemberId = Utils.getPreference(getApplicationContext(), "ID");

        mMemberColor = Color.parseColor(Utils.getPreference(getApplicationContext(), "MemberColor"));
        mCurrentColor = Utils.getCurrentColor(getApplicationContext());
        mBrightness = Integer.parseInt(Utils.getPreference(getApplicationContext(), "brightness"));
        Log.d(TAG, "isStar? " + mIsStar + ", mMemberId :" + mMemberId + ", mMemberColor(hex) :" + Utils.getHexCode(mMemberColor) +
                ", mCurrentColor(hex) :" + Utils.getHexCode(mCurrentColor) + ", bright :" + mBrightness);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        mTargetdevice = BluetoothUtils.getTargetDevice();

        String msg;
        if (mIsStar) {
            msg = getString(R.string.service_notification_msg_star);
        } else {
            msg = getString(R.string.service_notification_msg_fans);
        }
        NotificationCompat.Builder notificationBuilder = Utils.showNotification(this,
                true, 1002, "Bling", msg);
        startForeground(1002, notificationBuilder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        registerReceiver(powerSaverChangeReceiver, filter);
        Log.d("jjh", "android.os.action.POWER_SAVE_MODE_CHANGED");
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

        updateStarStatus(mIsStar, mMemberId, "off");

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        unregisterReceiver(powerSaverChangeReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        mqttSubscribe();
        updateStarStatus(mIsStar, mMemberId, "on");

        if (mTargetdevice != null) {
            Log.d(TAG, "attempting connect - " + mTargetdevice.getName() + " , " + mTargetdevice.getAddress());
            mBluetoothGatt = mTargetdevice.connectGatt(getApplicationContext(), false, mGattCallback);
        } else {
            //Toast.makeText(this, "no target device", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "NO DEV");
        }

        //thread.start();

        return START_STICKY;
    }

    private void updateStarStatus(Boolean isStar, String Id, String msg) {
        if (isStar) {
            HashMap<String, Object> parameters = new HashMap<>();

            parameters.put("member_conn_state", msg);

            retroClient.updateStarConnection(Id, parameters, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "setStatusView() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Log.d(TAG, "jjhhh updateStarStatus() : " + Id + msg);
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "setStatusView() jjh onFailure : " + errorData);
                }
            });
        }
    }

    private void writeData(byte[] data) {
        BluetoothUtils.writeCharacteristic_Data(mBluetoothGatt, data);
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

        Log.d("jjh", "sendColor" + Utils.getHexCode(currentColor));
        writeData(tx_data);
    }

    public void constantSet(int color) {
        byte[] tx_data = new byte[4];
        tx_data[0] = (byte) 0xCA;

        // 기기쪽 constant만 RBG로 되어있을것임 -_-
        tx_data[1] = (byte) (Color.red(color) & 0xFF);
        tx_data[2] = (byte) (Color.blue(color) & 0xFF);
        tx_data[3] = (byte) (Color.green(color) & 0xFF);

        Log.d("jjh", "constantSet " + Utils.getHexCode(color) + "...." + tx_data[1] + "," + tx_data[2] + "," + tx_data[3]);
        writeData(tx_data);
    }

    public void sendDrawingMode(int mode) {
        byte[] tx_data = new byte[8];
        tx_data[0] = (byte) 0xE0;
        tx_data[1] = (byte) mode;
        writeData(tx_data);
    }

    public void sendLcdDrawing(int state, int x, int y, int memberIndex) {
        int action;
        byte[] tx_data = new byte[16];
        if (state == MotionEvent.ACTION_DOWN) {
            action = 0;
        } else if (state == MotionEvent.ACTION_MOVE) {
            action = 1;
        } else {
            action = 2;
        }

        tx_data[0] = 0x30; // lcd control
        tx_data[1] = (byte) 0xAA; // screen drawing
        tx_data[2] = (byte) action;
        tx_data[3] = (byte) (x & 0xFF);
        tx_data[4] = (byte) ((x >> 8) & 0xFF);
        tx_data[5] = (byte) (y & 0xFF);
        tx_data[6] = (byte) ((y >> 8) & 0xFF);
        tx_data[7] = (byte) (Color.red(mMemberColor) & 0xff);
        tx_data[8] = (byte) (Color.green(mMemberColor) & 0xff);
        tx_data[9] = (byte) (Color.blue(mMemberColor) & 0xff);
        tx_data[10] = (byte) (4 & 0xff);
        tx_data[11] = (byte) (memberIndex & 0xff);

        writeData(tx_data);

        Log.d(TAG, "sendLcdDrawing " + x + "," + y + "," + memberIndex);
    }

    public void sendCleanLcd() {
        byte[] tx_data = new byte[8];

        tx_data[0] = 0x30; // lcd control
        tx_data[1] = (byte) (0xCE); // screen clear

        writeData(tx_data);
    }

    /*public void sendColorToLight2(int currentColor) {
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
    }*/

    public void setOnOffLight(int value) {
        byte[] tx_data = new byte[8];
        tx_data[0] = (byte) 0xEA;
        tx_data[1] = (byte) value;

        writeData(tx_data);
    }

    MqttCallback mCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            mqttConnect();
            Log.d(TAG, "connectionLost() Mqtt ReConnect");
            cause.printStackTrace();

            Utils.showNotification(BlingService.this, false,
                    1004, "Bling", "connectionLost() Mqtt ReConnect");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            Intent newIntent;
            if (topic.equals("/bling/star/" + mStarId + "/conn")) {
                // 연결관련 메시징을 받을때
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(message.toString());

                String data = jsonObject.get("msg_data").getAsString();

                // 팬은 스타가 online 했을때 노티를 받는다
                if (!mIsStar && "on".equals(data)) {
                    Utils.showNotification(BlingService.this, false,
                            1001, "Bling", getString(R.string.star_online_notification_msg));
                }

                newIntent = new Intent("bling.service.action.STAR_CONNECTION_CHANGED");
                newIntent.putExtra("msg", data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);

                Log.d(TAG, "Mqtt messageArrived() in Service : " + data);
            } else if (topic.equals("/bling/star/" + mStarId + "/msg/touch")) {
                // 터치 관련 메시지를 받을때
                if (!mIsStar) {
                    // 팬만 받아서 동작함
                    String[] data = message.toString().split("\\|");

                    if ("1".equals(data[0])) {
                        int color = Integer.parseInt(data[1]);
                        sendColorToLed(color);
                    } else {
                        float[] hsv = new float[3];
                        Color.colorToHSV(mCurrentColor, hsv);
                        hsv[2] = (float) mBrightness / 100000000;

                        sendColorToLed(Color.HSVToColor(hsv));
                    }
                    Log.d(TAG, "Mqtt messageArrived() touch : " + message.toString());
                }
            } else if (topic.equals("/bling/star/" + mStarId + "/msg/drawing")) {
                if (!mIsStar) {
                    // 팬만 받아서 동작함
                    String[] point = message.toString().split("\\|");
                    sendLcdDrawing(Integer.parseInt(point[0]), Integer.parseInt(point[1]), Integer.parseInt(point[2]), Integer.parseInt(point[3]));

                    // 세팅 캔버스에 그리기위해 브로드캐스트, 테스트용으로 없어져도 됨
                    newIntent = new Intent("bling.service.action.STAR_DRAWING");
                    newIntent.putExtra("msg", message.toString());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);

                    Log.d(TAG, "Mqtt messageArrived() drawing : " + message.toString());
                }
            } else if (topic.equals("/bling/star/" + mStarId + "/msg/drawingmode")) {
                if (!mIsStar) {
                    sendDrawingMode(Integer.parseInt(message.toString()));
                    sendCleanLcd();

                    Log.d(TAG, "Mqtt messageArrived() drawingmode : " + message.toString());
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    private void mqttConnect() {
        if (mMqttClient == null || !mMqttClient.isConnected()) {
            try {
                mMqttClient = new MqttClient("tcp://ec2-52-79-216-28.ap-northeast-2.compute.amazonaws.com:1883", MqttClient.generateClientId(), null);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                mMqttClient.connect(options);
                mMqttClient.setCallback(mCallback);
            } catch (Exception e) {
                Log.d(TAG, "Error while mqtt connecting");
                e.printStackTrace();
            }
        }
    }

    public void mqttSubscribe() {
        Log.d(TAG, "mqttSubscribe");
        mqttConnect();

        try {
            if (mIsStar) {
                String[] topics = {"/bling/star/" + mStarId + "/conn"};
                mMqttClient.subscribe(topics);
            } else {
                String[] topics = {"/bling/star/" + mStarId + "/conn", "/bling/star/" + mStarId + "/msg/touch", "/bling/star/" + mStarId + "/msg/drawing", "/bling/star/" + mStarId + "/msg/drawingmode"};
                mMqttClient.subscribe(topics);
            }
        } catch (Exception e) {
            Log.d(TAG, "mqttSubscribe() : error");
            e.printStackTrace();
        }
    }

    public void mqttTouchPublish(String data) {
        mqttConnect();

        try {
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/touch", new MqttMessage(data.getBytes()));
            Log.d(TAG, "star publish " + "/bling/star/" + mStarId + "/msg/touch" + data);
        } catch (Exception e) {
            Log.d(TAG, "mqttTouchPublish() : error");
            e.printStackTrace();
        }
    }

    public void mqttDrawingModePublish(String data) {
        mqttConnect();

        try {
            //data = data + "|" + mMemberId;
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/drawingmode", new MqttMessage(data.getBytes()));
            Log.d(TAG, "star publish " + "/bling/star/" + mStarId + "/msg/drawingmode : " + data);
        } catch (Exception e) {
            Log.d(TAG, "mqttDrawingPublish() : error");
            e.printStackTrace();
        }
    }

    public void mqttDrawingPublish(String data) {
        mqttConnect();

        try {
            data = data + "|" + mMemberId;
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/drawing", new MqttMessage(data.getBytes()));
            Log.d(TAG, "star publish " + "/bling/star/" + mStarId + "/msg/drawing : " + data);
        } catch (Exception e) {
            Log.d(TAG, "mqttDrawingPublish() : error");
            e.printStackTrace();
        }
    }
}
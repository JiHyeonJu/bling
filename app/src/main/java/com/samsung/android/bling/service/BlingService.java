package com.samsung.android.bling.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.setting.SettingActivity;
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

public class BlingService extends Service {
    private static final String TAG = "Bling/BlingService";

    BluetoothGatt mBluetoothGatt = null;
    BluetoothDevice mTargetdevice = null;

    private MqttClient mMqttClient;

    private IBinder mBinder = new BTBinder();

    private RetroClient retroClient;

    private boolean mIsStar;
    private String mStarId = "1";
    private String mMemberId;

    public int mMemberColor;
    private int mCurrentColor;
    private int mBrightness;

    private boolean mIsDrawing = false;

    public int mBatteryPercent = 0;
    public int mBatteryTime = 0;
    public boolean mIsBatteryCharging = false;

    private String mPhotoKitNfc = "-1";

    private NotificationCompat.Builder mNotificationBuilder;

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
                        if (mIsStar && !mIsDrawing && isPhotoKitConnected()) {
                            Log.d(TAG, "touch set my color" + Utils.getHexCode(mMemberColor));
                            byte[] tx_data = new byte[4];
                            tx_data[0] = (byte) 0xCC;

                            // 이 부분 수정되어야함
                            int color = mMemberColor;
                            tx_data[1] = (byte) (Color.red(color) & 0xFF);
                            tx_data[2] = (byte) (Color.green(color) & 0xFF);
                            tx_data[3] = (byte) (Color.blue(color) & 0xFF);

                            writeData(tx_data);
                        }
                        break;
                    case (byte) 0xAA:
                        // 스타가 손을 대면 publish 1, 떼면 publish 0
                        if (mIsStar && !mIsDrawing && isPhotoKitConnected()) {
                            //Log.d(TAG, "touch data : " + data[1] + mIsStar);
                            if (1 == data[1]) {
                                mqttTouchPublish("1|" + mMemberColor);
                            } else {
                                mqttTouchPublish("0");
                            }
                        }
                        break;
                    case (byte) 0xD0:
                        if (mIsStar && isPhotoKitConnected()) {
                            if (data[1] == 1) {
                                // 드로잉 모드 시작
                                sendLcdDrawing(2, 0, 0, Integer.parseInt(mMemberId),
                                        Color.red(mMemberColor), Color.green(mMemberColor), Color.blue(mMemberColor));

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
                        if (mIsStar && mIsDrawing && isPhotoKitConnected()) {
                            int xPos, yPos, action;

                            xPos = (data[2] & 0xFF);
                            xPos <<= 8;
                            xPos |= (data[1] & 0xFF);
                            yPos = (data[4] & 0xFF);
                            yPos <<= 8;
                            yPos |= (data[3] & 0xFF);
                            action = data[5];
                            Log.d(TAG, "X" + xPos + " Y" + yPos);
                            /*xPos *= 2;
                            yPos *= 2;*/

                            mqttDrawingPublish(action + "|" + xPos + "|" + yPos + "|" + mMemberId + "|" + mMemberColor);

                            Log.d(TAG, "drawing now : " + action + "|" + xPos + "|" + yPos + "|" + mMemberId + mMemberColor);

                            /*// 세팅 캔버스에 그리기위해 브로드캐스트, 테스트용으로 없어져도 됨
                            Intent newIntent = new Intent("bling.service.action.STAR_DRAWING");
                            newIntent.putExtra("msg", action + "|" + xPos + "|" + yPos + "|" + mMemberId);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);*/
                        }
                        break;
                    case (byte) 0xCD:
                        // NFC 태그 새로 읽혔을때
                        int len = data[2];
                        byte[] b = new byte[255];

                        if (data[1] > 0) // detected
                        {
                            for (int i = 0; i < len; i++) {
                                b[i] = data[3 + i];
                            }
                            String str = new String(b);
                            str = str.trim();

                            Log.d(TAG, "nfc receive len: " + len + " data: " + str + " " + mPhotoKitNfc);

                            if (isNewPhotoKit(str)) {
                                if (mIsStar) {
                                    updateStarStatus(mMemberId, "on");
                                }
                                Intent newIntent = new Intent("bling.service.action.NEW_PHOTOKIT");
                                newIntent.putExtra("nfcInfo", str);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);

                                setPhotoKitNfc(str);

                                // 포토키트가 새로 꼽혔을때 문구 교체
                                String[] notificationText = setServiceNotification();
                                if (mNotificationBuilder != null) {
                                    mNotificationBuilder.setContentTitle(notificationText[0]);
                                    mNotificationBuilder.setContentText(notificationText[1]);
                                    startForeground(1002, mNotificationBuilder.build());
                                }
                            }
                        } else {
                            if (isPhotoKitConnected()) {
                                if (mIsStar) {
                                    updateStarStatus(mMemberId, "off");
                                }
                                Intent newIntent = new Intent("bling.service.action.NO_PHOTOKIT");
                                newIntent.putExtra("nfcInfo", "-1");
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);

                                setPhotoKitNfc("-1");

                                // 포토키트가 빠졌을때 문구 교체
                                String[] notificationText = setServiceNotification();
                                if (mNotificationBuilder != null) {
                                    mNotificationBuilder.setContentTitle(notificationText[0]);
                                    mNotificationBuilder.setContentText(notificationText[1]);
                                    startForeground(1002, mNotificationBuilder.build());
                                }
                            }

                            Log.d(TAG, "nfc not detected");
                        }
                        break;
                    case (byte) 0xBA:
                        // 배터리값 왔을때 세팅화면으로 넘겨주기
                        mBatteryPercent = data[1];
                        mBatteryTime = 240 * mBatteryPercent / 100;
                        mIsBatteryCharging = ((data[2] >> 1) & 1) == 1;
                        boolean isFull = (data[2] & 1) == 1;

                        Log.d(TAG, "Battery Percentage : " + mBatteryPercent + "%, is_charging:" + mIsBatteryCharging);
                        /*Utils.showNotification(BlingService.this, false,
                                1004, "Bling", "Battery Percentage : " + mBatteryPercent);*/

                        Intent newIntent = new Intent("bling.service.action.Battery");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);
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

            try {
                batteryRequst();
                Thread.sleep(1000);
                /*batteryRequestRepeadly(1);*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /*BroadcastReceiver powerSaverChangeReceiver = new BroadcastReceiver() {
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
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mIsStar = Utils.getIsStar(getApplicationContext());
        mMemberId = Utils.getPreference(getApplicationContext(), "ID");
        mPhotoKitNfc = Utils.getPreference(getApplicationContext(), "nfcInfo");

        String color = Utils.getPreference(getApplicationContext(), "MemberColor");
        mMemberColor = Color.parseColor(color);
        mCurrentColor = Utils.getCurrentColor(getApplicationContext());
        mBrightness = Integer.parseInt(Utils.getPreference(getApplicationContext(), "brightness"));
        Log.d(TAG, "isStar? " + mIsStar + ", mMemberId :" + mMemberId + ", mMemberColor(hex) :" + Utils.getHexCode(mMemberColor) +
                ", mCurrentColor(hex) :" + Utils.getHexCode(mCurrentColor) + ", bright :" + mBrightness);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        mTargetdevice = BluetoothUtils.getTargetDevice();

        String[] notificationText = setServiceNotification();
        mNotificationBuilder = Utils.showNotification(this,
                true, 1002, notificationText[0], notificationText[1]);
        startForeground(1002, mNotificationBuilder.build());

        /*IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        //registerReceiver(powerSaverChangeReceiver, filter);
        Log.d("jjh", "android.os.action.POWER_SAVE_MODE_CHANGED");*/
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (mMqttClient != null && mMqttClient.isConnected()) {
            try {
                if (mIsStar) {
                    String topic = "/bling/star/" + mStarId + "/conn";
                    mMqttClient.unsubscribe(topic);
                } else {
                    String[] topics = {"/bling/star/" + mStarId + "/conn", "/bling/star/" + mStarId + "/msg/touch",
                            "/bling/star/" + mStarId + "/msg/drawing", "/bling/star/" + mStarId + "/msg/drawingmode"};
                    mMqttClient.unsubscribe(topics);
                }
                mMqttClient.disconnect();
                mMqttClient = null;
            } catch (Exception e) {
                Log.d(TAG, "Error while mqtt disconnecting");
                e.printStackTrace();
            }
        }

        // 이미 포토키트가 빠진 상황이면 off 해줄 필요가 없어
        if (mIsStar && isPhotoKitConnected()) {
            updateStarStatus(mMemberId, "off");
        }

        batteryRequestRepeadly(0);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        mNotificationBuilder = null;

        //unregisterReceiver(powerSaverChangeReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        mqttSubscribe();
        if (mIsStar && isPhotoKitConnected()) {
            updateStarStatus(mMemberId, "on");
        }

        if (mTargetdevice != null) {
            Log.d(TAG, "attempting connect - " + mTargetdevice.getName() + " , " + mTargetdevice.getAddress());
            mBluetoothGatt = mTargetdevice.connectGatt(getApplicationContext(), false, mGattCallback);
        } else {
            //Toast.makeText(this, "no target device", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "NO DEV");
        }

        return START_STICKY;
    }

    private void updateStarStatus(String Id, String msg) {
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
                Log.d(TAG, "updateStarStatus() : " + Id + msg);
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "setStatusView() jjh onFailure : " + errorData);
            }
        });
    }

    private String[] setServiceNotification() {
        String[] notificationText = new String[2];

        if (mIsStar) {
            if (isPhotoKitConnected()) {
                notificationText[0] = getString(R.string.service_notification_title);
                notificationText[1] = getString(R.string.service_notification_msg_star);
            } else {
                notificationText[0] = getString(R.string.app_name);
                notificationText[1] = getString(R.string.service_notification_msg_star_no_photokit);
            }
        } else {
            if (isPhotoKitConnected()) {
                notificationText[0] = getString(R.string.service_notification_title);
                notificationText[1] = getString(R.string.service_notification_msg_fans);
            } else {
                notificationText[0] = getString(R.string.app_name);
                notificationText[1] = getString(R.string.service_notification_msg_fans_no_photokit);
            }
        }

        return notificationText;
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

    public void sendEachColorToLedAll(int[] ledrgb) // 27 개 배열 rgb(3개) * led9개
    {
        byte[] tx_data = new byte[32];

        tx_data[0] = 0x22;
        for (int i = 0; i < 27; i++) {
            tx_data[1 + i] = (byte) ledrgb[i];
        }
        writeData(tx_data);
    }

    public void sendEachColorToLed(int ledindex, int r, int g, int b) {
        byte[] tx_data = new byte[8];

        tx_data[0] = 0x21;
        tx_data[1] = (byte) (ledindex & 0xFF);
        tx_data[2] = (byte) (r & 0xFF);
        tx_data[3] = (byte) (g & 0xFF);
        tx_data[4] = (byte) (b & 0xFF);

        writeData(tx_data);
    }

    public void constantSet(int color) {
        byte[] tx_data = new byte[4];
        tx_data[0] = (byte) 0xCA;

        tx_data[1] = (byte) (Color.red(color) & 0xFF);
        tx_data[2] = (byte) (Color.green(color) & 0xFF);
        tx_data[3] = (byte) (Color.blue(color) & 0xFF);

        Log.d("jjh", "constantSet " + Utils.getHexCode(color) + "...." + tx_data[1] + "," + tx_data[2] + "," + tx_data[3]);
        writeData(tx_data);
    }

    public void sendDrawingMode(int mode) {
        byte[] tx_data = new byte[8];
        tx_data[0] = (byte) 0xE0;
        tx_data[1] = (byte) mode;
        writeData(tx_data);
    }

    public void sendLcdDrawing(int state, int x, int y, int memberIndex, int r, int b, int g) {
        byte[] tx_data = new byte[16];

        tx_data[0] = 0x30; // lcd control
        tx_data[1] = (byte) 0xAA; // screen drawing
        tx_data[2] = (byte) state;
        tx_data[3] = (byte) (x & 0xFF);
        tx_data[4] = (byte) ((x >> 8) & 0xFF);
        tx_data[5] = (byte) (y & 0xFF);
        tx_data[6] = (byte) ((y >> 8) & 0xFF);
        tx_data[7] = (byte) (r & 0xff);
        tx_data[8] = (byte) (g & 0xff);
        tx_data[9] = (byte) (b & 0xff);
        tx_data[10] = (byte) (3 & 0xff);
        tx_data[11] = (byte) (memberIndex & 0xff);

        writeData(tx_data);

        Log.d(TAG, "sendLcdDrawing " + state + "," + x + "," + y + "," + memberIndex + "," + r + "," + g + "," + b);
    }

    public void sendCleanLcd() {
        byte[] tx_data = new byte[8];

        tx_data[0] = 0x30; // lcd control
        tx_data[1] = (byte) (0xCE); // screen clear

        writeData(tx_data);
    }

    public void batteryRequst() {
        byte[] tx_data = new byte[8];
        tx_data[0] = (byte) 0xBA;
        writeData(tx_data);

        Log.d(TAG, "batteryRequest");
    }

    public void batteryRequestRepeadly(int request) {
        byte[] tx_data = new byte[8];

        tx_data[0] = (byte) 0xBC;
        tx_data[1] = (byte) request;

        writeData(tx_data); // 요청후 onCharacteristicChanged 에서 데이터를 받음

        Log.d(TAG, "battery request" + request);
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
            //mqttConnect();
            Log.d(TAG, "connectionLost() Mqtt ReConnect");
            cause.printStackTrace();

            /*// 커넥션 관련 로그용으로 나중에 지워야함
            Utils.showNotification(BlingService.this, false,
                    2001, "Bling", "connectionLost() Mqtt ReConnect");*/
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
                // 팬이고 포토키트가 껴져있을때만 작동
                if (!mIsStar && isPhotoKitConnected()) {
                    // 팬만 받아서 동작함
                    String[] data = message.toString().split("\\|");

                    if ("1".equals(data[0])) {
                        int color = Integer.parseInt(data[1]);
                        sendColorToLed(color);
                    } else {
                        float[] hsv = new float[3];
                        Color.colorToHSV(mCurrentColor, hsv);
                        hsv[2] = (float) mBrightness / SettingActivity.BRIGHT_MAX;

                        sendColorToLed(Color.HSVToColor(hsv));
                    }
                    Log.d(TAG, "Mqtt messageArrived() touch : " + message.toString());
                }
            } else if (topic.equals("/bling/star/" + mStarId + "/msg/drawing")) {
                // 드로잉 관련 메시지를 받을때
                // 팬이고 포토키트가 껴져있을때만 작동
                if (!mIsStar && isPhotoKitConnected()) {
                    String[] data = message.toString().split("\\|");

                    int color = Integer.parseInt(data[4]);
                    sendLcdDrawing(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]),
                            Color.red(color), Color.green(color), Color.blue(color));

                    /*// 세팅 캔버스에 그리기위해 브로드캐스트, 테스트용으로 없어져도 됨
                    newIntent = new Intent("bling.service.action.STAR_DRAWING");
                    newIntent.putExtra("msg", message.toString());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);*/

                    Log.d(TAG, "Mqtt messageArrived() drawing : " + message.toString());
                }
            } else if (topic.equals("/bling/star/" + mStarId + "/msg/drawingmode")) {
                // 드로잉 모드 메시지를 받을때
                // 팬이고 포토키트가 껴져있을때만 작동
                if (!mIsStar && isPhotoKitConnected()) {
                    /*int mode = Integer.parseInt(message.toString());
                    if (mode == 2) {
                        sendCleanLcd();
                        Thread.sleep(10);
                    }*/
                    sendDrawingMode(Integer.parseInt(message.toString()));

                    /*// 드로잉 모드 체크용으로 나중에 지워야할것
                    Utils.showNotification(BlingService.this, false,
                            2002, "Bling", message.toString());*/

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
                /*options.setAutomaticReconnect(true);
                options.setCleanSession(true);*/
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
                String topic = "/bling/star/" + mStarId + "/conn";
                mMqttClient.subscribe(topic, 1);
            } else {
                String[] topics = {"/bling/star/" + mStarId + "/conn", "/bling/star/" + mStarId + "/msg/touch",
                        "/bling/star/" + mStarId + "/msg/drawing", "/bling/star/" + mStarId + "/msg/drawingmode"};
                int[] qos = {1, 1, 1, 2};
                mMqttClient.subscribe(topics, qos);
            }
        } catch (Exception e) {
            Log.d(TAG, "mqttSubscribe() : error");
            e.printStackTrace();
        }
    }

    public void mqttTouchPublish(String data) {
        mqttConnect();

        try {
            //mMqttClient.publish("/bling/star/" + mStarId + "/msg/touch", new MqttMessage(data.getBytes()));
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/touch", data.getBytes(), 2, false);
            Log.d(TAG, "star publish " + "/bling/star/" + mStarId + "/msg/touch : " + data);
        } catch (Exception e) {
            Log.d(TAG, "mqttTouchPublish() : error");
            e.printStackTrace();
        }
    }

    public void mqttDrawingModePublish(String data) {
        mqttConnect();

        try {
            //data = data + "|" + mMemberId;
            //mMqttClient.publish("/bling/star/" + mStarId + "/msg/drawingmode", new MqttMessage(data.getBytes()));
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/drawingmode", data.getBytes(), 2, false);
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
            //mMqttClient.publish("/bling/star/" + mStarId + "/msg/drawing", new MqttMessage(data.getBytes()));
            mMqttClient.publish("/bling/star/" + mStarId + "/msg/drawing", data.getBytes(), 2, false);
            Log.d(TAG, "star publish " + "/bling/star/" + mStarId + "/msg/drawing : " + data);
        } catch (Exception e) {
            Log.d(TAG, "mqttDrawingPublish() : error");
            e.printStackTrace();
        }
    }

    public void setPhotoKitNfc(String nfcInfo) {
        mPhotoKitNfc = nfcInfo;

        Utils.savePreference(getApplication(), "nfcInfo", mPhotoKitNfc);
    }

    public boolean isNewPhotoKit(String nfcInfo) {
        Log.d(TAG, "nfc receive" + !mPhotoKitNfc.equals(nfcInfo));
        return !mPhotoKitNfc.equals(nfcInfo);
    }

    public boolean isPhotoKitConnected() {
        return !mPhotoKitNfc.equals("-1");
    }

    public boolean isDrawing() {
        return mIsDrawing;
    }
}
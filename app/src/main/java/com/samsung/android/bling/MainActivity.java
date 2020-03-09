package com.samsung.android.bling;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.data.StarInfoVo;
import com.samsung.android.bling.data.StarMemberInfoVo;
import com.samsung.android.bling.data.UserInfoVo;
import com.samsung.android.bling.chart.ChartActivity;
import com.samsung.android.bling.history.HistoryActivity;
import com.samsung.android.bling.reward.RewardActivity;
import com.samsung.android.bling.service.BlingService;
import com.samsung.android.bling.setting.SettingActivity;
import com.samsung.android.bling.util.BluetoothUtils;
import com.samsung.android.bling.util.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Bling/MainActivity";

    private Button mUnpairBtn;

    private TextView mMyStatusView;
    private TextView mUserNameView;

    private TextView mStarStatusView;
    private TextView mStarNameView;
    private TextView mStarDefineView;

    private ImageButton mSettingBtn;
    private Button mRewardBtn;
    private Button mHistoryBtn;
    private Button mChartBtn;

    private RetroClient retroClient;
    private String mId;
    private boolean mIsStar;
    private String mStarId = "1";    // 블링 기기로부터 starID 얻어오게끔 수정될 예정

    private MqttClient mMqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mId = getIntent().getStringExtra("ID");
        mIsStar = getIntent().getBooleanExtra("isStar", false);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        initView();

        // BT
        //BluetoothUtils.checkBluetooth(this);
    }

    private void initView() {
        mUnpairBtn = findViewById(R.id.unpair_btn);

        mMyStatusView = findViewById(R.id.my_status_view);
        mUserNameView = findViewById(R.id.user_name_view);

        mStarStatusView = findViewById(R.id.star_status_view);
        mStarNameView = findViewById(R.id.star_name_view);
        mStarDefineView = findViewById(R.id.star_define_view);

        mSettingBtn = findViewById(R.id.setting_btn);
        mHistoryBtn = findViewById(R.id.history_btn);
        mRewardBtn = findViewById(R.id.reward_btn);
        mChartBtn = findViewById(R.id.chart_btn);

        if (mIsStar) {
            mChartBtn.setVisibility(View.VISIBLE);
            mRewardBtn.setVisibility(View.GONE);
        } else {
            mRewardBtn.setVisibility(View.VISIBLE);
            mChartBtn.setVisibility(View.GONE);
        }

        mUnpairBtn.setOnClickListener(v -> BluetoothUtils.unpairDevice());

        mSettingBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));

        mHistoryBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));

        mRewardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RewardActivity.class);
            intent.putExtra("ID", mId);
            startActivity(intent);

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        mChartBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChartActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("bling.service.action.BT_CONNECTION_CHANGED");
        intentFilter.addAction("bling.service.action.STAR_CONNECTION_CHANGED");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        if (!mIsStar) {
            mqttSubscribe();
        }

        setUserName();
        setStatusView(Utils.isMyServiceRunning(this, BlingService.class), false);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        if (!mIsStar) {
            mqttUnsubscribe();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (!mIsStar && mMqttClient != null && mMqttClient.isConnected()) {
            try {
                mMqttClient.disconnect();
                mMqttClient = null;
            } catch (Exception e) {
                Log.d(TAG, "Error while mqtt disconnecting");
                e.printStackTrace();

            }
        }
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String message = "";

            if (action.equals("bling.service.action.BT_CONNECTION_CHANGED")) {
                message = intent.getStringExtra("bt_status");

                setStatusView("connect".equals(message), false);
            } else if (action.equals("bling.service.action.STAR_CONNECTION_CHANGED")) {
                message = intent.getStringExtra("msg");

                setStatusView("on".equals(message), true);
            }

            Log.d(TAG, "action: " + action + ", message" + message);
        }
    };

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

    private void mqttSubscribe() {
        if (!Utils.isMyServiceRunning(this, BlingService.class)) {
            mqttConnect();

            try {
                mMqttClient.subscribe("/bling/star/" + mStarId + "/conn");
                mMqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        mqttConnect();
                        Log.d(TAG, "connectionLost() Mqtt ReConnect");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(message.toString());

                        String data = jsonObject.get("msg_data").getAsString();

                        Log.d(TAG, "Mqtt messageArrived() data : " + data);
                        setStatusView("on".equals(data), true);
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
    }

    private void mqttUnsubscribe() {
        if (mMqttClient != null && mMqttClient.isConnected()) {
            try {
                mMqttClient.unsubscribe("/bling/star/" + mStarId + "/conn");
            } catch (Exception e) {
                Log.d(TAG, "mqttSubscribe() : error");
                e.printStackTrace();
            }
        }
    }

    private void setUserName() {
        if (mIsStar) {
            retroClient.getStarData(mId, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "create() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    StarMemberInfoVo data = (StarMemberInfoVo) receivedData;

                    mUserNameView.setText(data.getMemberName());
                    mStarNameView.setText(data.getStarName());
                    mStarDefineView.setText(getString(R.string.my_team));
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "create() onFailure : " + code);
                }
            });
        } else {
            retroClient.getUserData(mId, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "create() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    UserInfoVo data = (UserInfoVo) receivedData;

                    mUserNameView.setText(data.getNickName());
                    mStarDefineView.setText(getString(R.string.my_star));
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "create() onFailure : " + code);
                }
            });
        }
    }

    private void setStatusView(boolean isOn, boolean isStarView) {
        String msg = isOn ? "on" : "off";

        Log.d(TAG, "ison : " + isOn + ", isStarView : " + isStarView);

        if (isStarView) {
            updateView(isOn, mStarStatusView);
        } else {
            updateView(isOn, mMyStatusView);

            if (mIsStar) {
                updateView(isOn, mStarStatusView);
            } else {
                setStarStatus();
            }
        }
    }

    private void updateView(boolean isOn, TextView view) {
        if (isOn) {
            view.setText(getString(R.string.on_line));
            view.setTextColor(getColor(R.color.white));
            view.setBackground(getDrawable(R.drawable.main_online_view));
        } else {
            view.setText(getString(R.string.off_line));
            view.setTextColor(getColor(R.color.textColor));
            view.setBackground(getDrawable(R.drawable.main_offline_view));
        }
    }

    private void setStarStatus() {
        retroClient.getStarConnection(mStarId, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "setStarStatus() onError : " + t.toString());
                t.printStackTrace();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                StarInfoVo data = (StarInfoVo) receivedData;

                setStatusView("on".equals(data.getStarStatus()), true);

                mStarNameView.setText(data.getStarName());
            }

            @Override
            public void onFailure(int code, Object errorData) {
                Log.d(TAG, "setStarStatus() onFailure : " + code);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothUtils.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Turn on Bluetooth", Toast.LENGTH_SHORT).show();
                if (BluetoothUtils.isAleardyPaired(this)) {
                    Toast.makeText(this, "already paired", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "not paired");
                    startActivity(new Intent(this, SetupActivity.class));
                }
            } else {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
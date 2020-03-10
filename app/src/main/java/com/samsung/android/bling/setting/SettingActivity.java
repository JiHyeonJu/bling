package com.samsung.android.bling.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.samsung.android.bling.R;
import com.samsung.android.bling.util.Utils;
import com.samsung.android.bling.account.AccountActivity;
import com.samsung.android.bling.service.BlingService;
import com.samsung.android.bling.service.BlingService.BTBinder;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;

import java.util.LinkedList;
import java.util.Queue;

public class SettingActivity extends Activity {
    private static final String TAG = "Bling/SettingActivity";

    private LinearLayout mColorPickerLayout;
    private ColorPickerPreferenceManager mColorManager;

    private ImageButton mAccountBtn;

    private TextView mLightModeTitle;
    private Button mGeneralModeBtn;
    private Button mCheeringModeBtn;
    private boolean mIsCheeringMode = false;

    private TextView mBrightnessTitle;
    private ArcSeekBar mBrightnessSeekBar;
    private LinearLayout mBrightnessImageLayout;
    private int mBrightness = 0;

    private TextView mColorTitle;
    private LinearLayout mColorScrollView;
    private ImageButton mColorPickerBtn;

    private AlertDialog mPickerDialog = null;
    private int mCurrentColor;
    private String mCurrentColorHex;
    private Queue<String> colorQueue = new LinkedList<>();

    private SeekBar mBatterySeekBar;
    private TextView mBatteryPercent;
    private TextView mBatteryTime;

    private boolean mBound = false;
    BlingService mService;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 4.
            Log.d(TAG, "onServiceConnected()");

            BTBinder binder = (BTBinder) service;
            mService = binder.getService();
            mBound = true;
            setEnableView(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected()");

            mBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        if (Utils.isMyServiceRunning(this, BlingService.class)) {
            Intent Service = new Intent(getApplicationContext(), BlingService.class);
            bindService(Service, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound && Utils.isMyServiceRunning(this, BlingService.class)) {
            unbindService(mConnection);
        }
        mBound = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        setEnableView(Utils.isMyServiceRunning(this, BlingService.class));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("bling.service.action.BT_CONNECTION_CHANGED"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("bt_status");

            if (message.equals("connect")) {
                Log.d("jjh", "connect");
                Intent Service = new Intent(getApplicationContext(), BlingService.class);
                bindService(Service, mConnection, Context.BIND_AUTO_CREATE);
                // setEnableView(true); is in onServiceConnected()
            } else {
                if (mBound && Utils.isMyServiceRunning(SettingActivity.this, BlingService.class)) {
                    Log.d("jjh", "disconnect");
                    unbindService(mConnection);
                    mBound = false;
                    setEnableView(false);
                }
            }
        }
    };

    private void initView() {
        mAccountBtn = findViewById(R.id.setting_account_btn);

        mLightModeTitle = findViewById(R.id.light_mode_title);
        mGeneralModeBtn = findViewById(R.id.general_light_btn);
        mCheeringModeBtn = findViewById(R.id.cheering_light_btn);

        mBrightnessTitle = findViewById(R.id.brightness_title);
        mBrightnessSeekBar = findViewById(R.id.brightness_arc_seek_bar);
        mBrightnessImageLayout = findViewById(R.id.brightness_image_layout);

        mColorManager = ColorPickerPreferenceManager.getInstance(getApplicationContext());
        mCurrentColor = mColorManager.getColor("blingColorPicker", getColor(R.color.colorPrimary));

        mColorTitle = findViewById(R.id.setting_color_text);
        mColorScrollView = findViewById(R.id.color_scroll_view);
        setColorScrollView();
        setColorCheckbox(0);
        setColorScrollOnClickListener();

        mColorPickerLayout = findViewById(R.id.color_picker_layout);
        mColorPickerBtn = findViewById(R.id.setting_color_picker_btn);

        mBatterySeekBar = findViewById(R.id.battery_seek_bar);
        mBatteryPercent = findViewById(R.id.battery_percent);
        mBatteryTime = findViewById(R.id.batter_time);

        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(() -> onBackPressed(), 250));

        mGeneralModeBtn.setOnClickListener(v -> {
            if (mIsCheeringMode) {
                mColorPickerLayout.setVisibility(View.VISIBLE);
                mIsCheeringMode = false;

                mCheeringModeBtn.setTextColor(getColor(R.color.textColor));
                mCheeringModeBtn.setBackground(getResources().getDrawable(R.drawable.setting_nonselected_lgiht_btn));

                mGeneralModeBtn.setTextColor(getColor(R.color.white));
                mGeneralModeBtn.setBackground(getResources().getDrawable(R.drawable.setting_selected_lgiht_btn));
            }
        });

        mCheeringModeBtn.setOnClickListener(v -> {
            if (!mIsCheeringMode) {
                mColorPickerLayout.setVisibility(View.GONE);
                mIsCheeringMode = true;

                mGeneralModeBtn.setTextColor(getColor(R.color.textColor));
                mGeneralModeBtn.setBackground(getResources().getDrawable(R.drawable.setting_nonselected_lgiht_btn));

                mCheeringModeBtn.setTextColor(getColor(R.color.white));
                mCheeringModeBtn.setBackground(getResources().getDrawable(R.drawable.setting_selected_lgiht_btn));
            }
        });

        mAccountBtn.setOnClickListener(v -> {
            startActivity(new Intent(SettingActivity.this, AccountActivity.class));
        });

        mColorPickerBtn.setOnClickListener(v -> {
            // todo : show dialog
            showDialog();
        });

        mBrightness = Integer.parseInt(Utils.getPreference(getApplicationContext(), "brightness"));
        mBrightnessSeekBar.setProgress(mBrightness);

        mBrightnessSeekBar.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            // Handle Seekbar touch events.
            v.onTouchEvent(event);

            return true;
        });

        mBrightnessSeekBar.setOnProgressChangedListener(i -> {
            mBrightness = mBrightnessSeekBar.getProgress();

            Utils.savePreference(this, "brightness", String.valueOf(mBrightness));

            if (mBound) {
                float[] hsv = new float[3];
                Color.colorToHSV(mCurrentColor, hsv);
                hsv[2] = (float) mBrightness / 100000000;

                mService.sendColorToLed(Color.HSVToColor(hsv));
                Log.d(TAG, "Send mBrightness : " + mBrightness + ",hsv[2] : " + hsv[2]);
            }
        });

        mBatterySeekBar.setOnTouchListener((v, event) -> {
            return true;
        });
        mBatterySeekBar.setProgress(38);


        // [[ todo: will be removed
        findViewById(R.id.action_1).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mBound) {
                        mService.mqttPublish("1");

                        mService.sendColorToLed(Color.WHITE);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mBound) {
                        mService.mqttPublish("0");

                        float[] hsv = new float[3];
                        Color.colorToHSV(mCurrentColor, hsv);
                        hsv[2] = (float) mBrightness / 100000000;

                        mService.sendColorToLed(Color.HSVToColor(hsv));
                    }
                    break;
            }
            return true;
        });

        findViewById(R.id.action_2).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mBound) {
                        mService.setOnOffLight(1);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mBound) {
                        mService.setOnOffLight(0);
                    }
                    break;
            }
            return true;
        });

        findViewById(R.id.action_4).setOnClickListener(v -> {
            if (mBound) {
                mService.intensityControl(1, 0, 25);
            }
        });
        // ]] will be removed
    }

    private void setEnableView(boolean enable) {
        if (enable) {
            mLightModeTitle.setAlpha(1);
            mGeneralModeBtn.setAlpha(1);
            mCheeringModeBtn.setAlpha(1);
            mGeneralModeBtn.setEnabled(true);
            mCheeringModeBtn.setEnabled(true);

            mBrightnessTitle.setAlpha(1);
            mBrightnessImageLayout.setAlpha(1);
            mBrightnessSeekBar.setAlpha(1);
            mBrightnessSeekBar.setEnabled(true);

            mColorTitle.setAlpha(1);
            mColorScrollView.setAlpha(1);
            mColorPickerBtn.setAlpha(1);
            mBrightnessSeekBar.setEnabled(true);
            mColorPickerBtn.setEnabled(true);

            mBatterySeekBar.setProgress(38);
            mBatteryPercent.setText("38%");
            mBatteryTime.setVisibility(View.VISIBLE);

            Toast.makeText(this, getString(R.string.setting_connected_toast), Toast.LENGTH_SHORT).show();
        } else {
            // set Alpha
            mLightModeTitle.setAlpha(0.4f);
            mGeneralModeBtn.setAlpha(0.4f);
            mCheeringModeBtn.setAlpha(0.4f);
            mGeneralModeBtn.setEnabled(false);
            mCheeringModeBtn.setEnabled(false);

            mBrightnessTitle.setAlpha(0.4f);
            mBrightnessImageLayout.setAlpha(0.4f);
            mBrightnessSeekBar.setAlpha(0.4f);
            mBrightnessSeekBar.setEnabled(false);

            mColorTitle.setAlpha(0.4f);
            mColorScrollView.setAlpha(0.4f);
            mColorPickerBtn.setAlpha(0.4f);
            mBrightnessSeekBar.setEnabled(false);
            mColorPickerBtn.setEnabled(false);

            mBatterySeekBar.setProgress(0);
            mBatteryPercent.setText(getString(R.string.battery_disconnected));
            mBatteryTime.setVisibility(View.GONE);

            Toast.makeText(this, getString(R.string.setting_disconnected_toast), Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog() {
        mPickerDialog = Utils.showDialog(this, R.layout.setting_color_picker_dialog);

        initColorPickerView();
    }

    private void initColorPickerView() {
        ColorPickerView colorPickerView = mPickerDialog.findViewById(R.id.color_picker_view);
        colorPickerView.setPreferenceName("blingColorPicker");

        mColorManager.restoreColorPickerData(colorPickerView);
        colorPickerView.setPureColor(mCurrentColor);
        colorPickerView.selectByHsv(mCurrentColor);

        View newColorView = mPickerDialog.findViewById(R.id.new_color);
        Utils.setDrawableColor(mPickerDialog.findViewById(R.id.prev_color), mCurrentColor);

        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                mCurrentColorHex = "#" + envelope.getHexCode();

                float[] hsv = new float[3];
                if (mBound) {
                    Color.colorToHSV(envelope.getColor(), hsv);
                    hsv[2] = (float) mBrightness / 100000000;

                    mService.sendColorToLed(Color.HSVToColor(hsv));
                }
                Utils.setDrawableColor(newColorView, envelope.getColor());
                //newColorView.setBackgroundTintList(ColorStateList.valueOf(mCurrentColor));
                Log.d(TAG, "color : #" + envelope.getHexCode());
            }
        });

        mPickerDialog.findViewById(R.id.cancel).setOnClickListener((v -> {
            if (mBound) {
                float[] hsv = new float[3];
                Color.colorToHSV(mCurrentColor, hsv);
                hsv[2] = (float) mBrightness / 100000000;

                mService.sendColorToLed(Color.HSVToColor(hsv));
            }

            Utils.dismissDialog(mPickerDialog);
        }));

        mPickerDialog.findViewById(R.id.done).setOnClickListener((v -> {
            if (true) {
                mColorManager.saveColorPickerData(colorPickerView);

                if (colorQueue.size() == 6) {
                    colorQueue.remove();
                }

                colorQueue.offer(mCurrentColorHex);
                Utils.setList(getApplicationContext(), "savedColor", colorQueue);

                setColorScrollView();
                setColorCheckbox(0);
            }
            Utils.dismissDialog(mPickerDialog);
        }));
    }

    private void setColorScrollView() {
        colorQueue = Utils.getList(getApplicationContext(), "savedColor");
        int savedColorCount = colorQueue.size();

        for (int i = 7; i >= 0; i--) {
            if (i < savedColorCount) {
                int color = Color.parseColor(colorQueue.poll());
                Utils.setDrawableColor(((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(0), color);
            } else {
                Utils.setDrawableColor(((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(0), getColor(R.color.white));
            }
        }

        Utils.setDrawableColor(((FrameLayout) mColorScrollView.getChildAt(savedColorCount)).getChildAt(0), getColor(R.color.colorPrimary));
        Utils.setDrawableColor(((FrameLayout) mColorScrollView.getChildAt(savedColorCount + 1)).getChildAt(0), Color.parseColor("#2FB1FE"));
        colorQueue = Utils.getList(getApplicationContext(), "savedColor");
    }

    private void setColorScrollOnClickListener() {
        for (int i = 7; i >= 0; i--) {
            final int index = i;
            ((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(0).setOnClickListener(v -> {
                setColorCheckbox(index);
            });
        }
    }

    private void setColorCheckbox(int index) {
        colorQueue = Utils.getList(getApplicationContext(), "savedColor");

        for (int i = 7; i >= 0; i--) {
            if (i == index) {
                ((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);
                if (i < colorQueue.size()) {
                    mCurrentColor = Color.parseColor(colorQueue.poll());
                } else if (i == colorQueue.size()) {
                    ((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);
                    mCurrentColor = getColor(R.color.colorPrimary);
                } else if (i == colorQueue.size() + 1) {
                    ((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);
                    mCurrentColor = Color.parseColor("#2FB1FE");
                }
            } else {
                ((FrameLayout) mColorScrollView.getChildAt(i)).getChildAt(1).setVisibility(View.GONE);
                if (i < colorQueue.size()) {
                    colorQueue.poll();
                }
            }
        }
        colorQueue = Utils.getList(getApplicationContext(), "savedColor");
        mColorManager.setColor("blingColorPicker", mCurrentColor);

        ImageView checkboxView = (ImageView) ((FrameLayout) mColorScrollView.getChildAt(index)).getChildAt(1);

        if (Utils.canDisplayOnBackground(Color.WHITE, mCurrentColor)) {
            checkboxView.setImageTintList(ColorStateList.valueOf(getColor(R.color.pureWhite)));
        } else {
            checkboxView.setImageTintList(ColorStateList.valueOf(getColor(R.color.darkCheckbox)));
        }

        if (mBound) {
            float[] hsv = new float[3];
            Color.colorToHSV(mCurrentColor, hsv);
            hsv[2] = (float) mBrightness / 100000000;

            mService.sendColorToLed(Color.HSVToColor(hsv));
        }
    }
}
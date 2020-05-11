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
import android.widget.HorizontalScrollView;
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

import java.util.LinkedList;
import java.util.Queue;

import cn.cricin.colorpicker.CircleColorPicker;

public class SettingActivity extends Activity {
    private static final String TAG = "Bling/SettingActivity";

    public static int BRIGHT_MAX = 1000000;

    private TextView mLightModeTitle;
    private Button mGeneralModeBtn;
    private Button mCheeringModeBtn;
    private boolean mIsCheeringMode = false;

    private TextView mBrightnessTitle;
    private ArcSeekBar mBrightnessSeekBar;
    private LinearLayout mBrightnessImageLayout;
    private int mBrightness = 0;

    private TextView mColorTitle;
    private LinearLayout mColorPickerLayout;
    private HorizontalScrollView mColorHorizontalScrollView;
    private LinearLayout mColorScrollLayout;
    private ImageView mColorPickerBtn;
    private View mScrollViewStartDivider;
    private View mScrollViewEndDivider;

    private AlertDialog mPickerDialog = null;
    private int mCurrentColor;
    private String mCurrentColorHex;
    private Queue<String> colorQueue = new LinkedList<>();

    private SeekBar mBatterySeekBar;
    private TextView mBatteryPercent;
    private TextView mBatteryTime;

    private AlertDialog mPhotoKitDialog;

    // will be removed
    private BlingCanvas mCanvas;

    private boolean mIsStar;

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
            mService.batteryRequestRepeadly(1);
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
        mIsStar = Utils.getIsStar(getApplicationContext());
        mIsCheeringMode = Boolean.parseBoolean(Utils.getPreference(getApplicationContext(), "cheeringMode"));

        setContentView(R.layout.activity_setting);

        initView();

        setViewByCheeringMode();
    }

    @Override
    protected void onDestroy() {
        Utils.dismissDialog(mPhotoKitDialog);
        Utils.dismissDialog(mPickerDialog);

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
            mService.batteryRequestRepeadly(0);

            unbindService(mConnection);
            mService = null;
        }
        mBound = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        setEnableView(Utils.isMyServiceRunning(this, BlingService.class));

        /*LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("bling.service.action.BT_CONNECTION_CHANGED"));*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("bling.service.action.BT_CONNECTION_CHANGED");
        intentFilter.addAction("bling.service.action.Battery");
        intentFilter.addAction("bling.service.action.NEW_PHOTOKIT");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("bling.service.action.BT_CONNECTION_CHANGED")) {
                // 블루투스 통신에 의해 나 자신의 연결상태 확인
                String message = intent.getStringExtra("bt_status");

                if ("connect".equals(message)) {
                    Log.d(TAG, "connect");
                    Intent Service = new Intent(getApplicationContext(), BlingService.class);
                    bindService(Service, mConnection, Context.BIND_AUTO_CREATE);
                    // setEnableView(true); is in onServiceConnected()
                } else {
                    // 블루투스 연결이 끊기고 현재 서비스가 돌고있다면
                    if (mBound && Utils.isMyServiceRunning(SettingActivity.this, BlingService.class)) {
                        Log.d(TAG, "disconnect");
                        unbindService(mConnection);
                        mService = null;

                        setEnableView(false);
                    }
                    mBound = false;
                }
            } else if (action.equals("bling.service.action.Battery")) {
                if (mBound && mService != null) {
                    mBatterySeekBar.setProgress(mService.mBatteryPercent);
                    mBatteryPercent.setText((mService.mIsBatteryCharging ? "Charging " : "") + mService.mBatteryPercent + "%");
                    mBatteryTime.setText(getBatteryTime());

                    Log.d(TAG, "Battery Percentage in Setting : " + mService.mBatteryPercent);
                }
            } else if (action.equals("bling.service.action.NEW_PHOTOKIT")) {
                mPhotoKitDialog = Utils.showDialog(SettingActivity.this, R.layout.photo_kit_dialog);

                mPhotoKitDialog.findViewById(R.id.ok).setOnClickListener(v -> {
                    Utils.dismissDialog(mPhotoKitDialog);
                });
            }
        }
    };

    private void initView() {
        mLightModeTitle = findViewById(R.id.light_mode_title);
        mGeneralModeBtn = findViewById(R.id.general_light_btn);
        mCheeringModeBtn = findViewById(R.id.cheering_light_btn);

        mBrightnessTitle = findViewById(R.id.brightness_title);
        mBrightnessSeekBar = findViewById(R.id.brightness_arc_seek_bar);
        mBrightnessImageLayout = findViewById(R.id.brightness_image_layout);

        mColorTitle = findViewById(R.id.setting_color_text);
        mColorPickerLayout = findViewById(R.id.color_picker_layout);
        mColorHorizontalScrollView = findViewById(R.id.color_horizontal_scroll_view);
        mColorScrollLayout = findViewById(R.id.color_scroll_layout);
        mColorPickerBtn = findViewById(R.id.setting_color_picker_btn);
        mScrollViewStartDivider = findViewById(R.id.scroll_view_start_divider);
        mScrollViewEndDivider = findViewById(R.id.scroll_view_end_divider);

        mColorHorizontalScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollX > 0) {
                mScrollViewStartDivider.setVisibility(View.VISIBLE);
            } else {
                mScrollViewStartDivider.setVisibility(View.INVISIBLE);
            }
            //Log.d(TAG, "jjh!" + scrollX + "," + scrollY + "," + oldScrollX + "," + oldScrollY);
        });

        int selectedColorIndex = Integer.parseInt(Utils.getPreference(getApplicationContext(), "selectedColorIndex"));
        if (selectedColorIndex == -1) {
            selectedColorIndex = 0;
        }
        setColorScrollView();
        setColorCheckbox(selectedColorIndex);

        mBatterySeekBar = findViewById(R.id.battery_seek_bar);
        mBatteryPercent = findViewById(R.id.battery_percent);
        mBatteryTime = findViewById(R.id.batter_time);

        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(this::onBackPressed, 250));

        mGeneralModeBtn.setOnClickListener(v -> {
            if (mIsCheeringMode) {
                mColorPickerLayout.setVisibility(View.VISIBLE);
                mIsCheeringMode = false;
                Utils.savePreference(getApplicationContext(), "cheeringMode", "false");

                mCheeringModeBtn.setTextColor(getColor(R.color.textColor));
                mCheeringModeBtn.setBackground(getDrawable(R.drawable.setting_nonselected_lgiht_btn));

                mGeneralModeBtn.setTextColor(getColor(R.color.white));
                mGeneralModeBtn.setBackground(getDrawable(R.drawable.setting_selected_lgiht_btn));

                if (mBound && mService != null) {
                    float[] hsv = new float[3];
                    Color.colorToHSV(mCurrentColor, hsv);
                    hsv[2] = (float) mBrightness / BRIGHT_MAX;

                    int color = Color.HSVToColor(hsv);
                    mService.sendColorToLed(color);
                }
            }
        });

        mCheeringModeBtn.setOnClickListener(v -> {
            if (!mIsCheeringMode) {
                mColorPickerLayout.setVisibility(View.GONE);
                mIsCheeringMode = true;
                Utils.savePreference(getApplicationContext(), "cheeringMode", "true");

                mGeneralModeBtn.setTextColor(getColor(R.color.textColor));
                mGeneralModeBtn.setBackground(getDrawable(R.drawable.setting_nonselected_lgiht_btn));

                mCheeringModeBtn.setTextColor(getColor(R.color.white));
                mCheeringModeBtn.setBackground(getDrawable(R.drawable.setting_selected_lgiht_btn));

                if (mBound && mService != null) {
                    setCheeringLight();
                }
            }
        });

        findViewById(R.id.setting_account_btn).setOnClickListener(v -> {
            startActivity(new Intent(SettingActivity.this, AccountActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        mColorPickerBtn.setOnClickListener(v -> {
            // todo : show dialog
            showDialog();
        });

        mBrightness = Integer.parseInt(Utils.getPreference(getApplicationContext(), "brightness"));
        if (mBrightness == -1) {
            mBrightness = 50000000;
        }
        mBrightnessSeekBar.setProgress(mBrightness);

        mBatterySeekBar.setOnTouchListener((v, event) -> {
            return true;
        });
    }

    View.OnTouchListener mBrightnessBarTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    mBrightness = mBrightnessSeekBar.getProgress();
                    break;

                case MotionEvent.ACTION_MOVE:
                    int progress = (int) (mBrightnessSeekBar.getMaxProgress() * event.getX() / mBrightnessSeekBar.getWidth());
                    // Ensure progress stays within boundaries
                    if (progress < 0) {
                        progress = 0;
                    }
                    if (progress > mBrightnessSeekBar.getMaxProgress()) {
                        progress = mBrightnessSeekBar.getMaxProgress();
                    }

                    if (mBrightness != progress) {
                        mBrightness = progress;
                        Utils.savePreference(getApplicationContext(), "brightness", String.valueOf(mBrightness));

                        //Log.d("jjh", "move" + progress);
                        mBrightnessSeekBar.setProgress(progress);  // Draw progress

                        if (mBound && mService != null) {
                            if (mIsCheeringMode) {
                                setCheeringLight();
                            } else {
                                float[] hsv = new float[3];
                                Color.colorToHSV(mCurrentColor, hsv);
                                hsv[2] = (float) mBrightness / BRIGHT_MAX;

                                int color = Color.HSVToColor(hsv);
                                mService.sendColorToLed(color);
                                // 이때는 led 밝기만 바꾸고 touch 리스너에서 constant 컬러를 바꿔줌
                            }
                            Log.d(TAG, "Send mBrightness : " + mBrightness);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    //Log.d("jjh", "up");
                    // Allow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);

                    if (mBound && mService != null) {
                        float[] hsv = new float[3];
                        Color.colorToHSV(mCurrentColor, hsv);
                        hsv[2] = (float) mBrightness / BRIGHT_MAX;

                        try {
                            int color = Color.HSVToColor(hsv);
                            Thread.sleep(10);
                            mService.constantSet(color);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

            // Handle Seekbar touch events.
            v.onTouchEvent(event);
            return true;
        }
    };

    private void setViewByCheeringMode() {
        if (mIsCheeringMode) {
            mColorPickerLayout.setVisibility(View.GONE);

            mGeneralModeBtn.setTextColor(getColor(R.color.textColor));
            mGeneralModeBtn.setBackground(getDrawable(R.drawable.setting_nonselected_lgiht_btn));

            mCheeringModeBtn.setTextColor(getColor(R.color.white));
            mCheeringModeBtn.setBackground(getDrawable(R.drawable.setting_selected_lgiht_btn));
        } else {
            mColorPickerLayout.setVisibility(View.VISIBLE);

            mCheeringModeBtn.setTextColor(getColor(R.color.textColor));
            mCheeringModeBtn.setBackground(getDrawable(R.drawable.setting_nonselected_lgiht_btn));

            mGeneralModeBtn.setTextColor(getColor(R.color.white));
            mGeneralModeBtn.setBackground(getDrawable(R.drawable.setting_selected_lgiht_btn));
        }
    }

    private String getBatteryTime() {
        String str;

        int hour = mService.mBatteryTime / 60;
        int min = mService.mBatteryTime % 60;

        if (hour > 1) {
            str = hour + " hours";
        } else if (hour == 1) {
            str = hour + " hour";
        } else {
            str = "";
        }

        if (min > 1) {
            str = str + " " + min + " mins";
        } else if (min <= 1) {
            str = str + " " + min + " min";
        }

        return str;
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
            mBrightnessSeekBar.setOnTouchListener(mBrightnessBarTouchListener);

            mColorTitle.setAlpha(1);
            mColorScrollLayout.setAlpha(1);
            mScrollViewStartDivider.setAlpha(1);
            mScrollViewEndDivider.setAlpha(1);
            mColorPickerBtn.setImageAlpha(255);
            mColorPickerBtn.setEnabled(true);
            for (int i = 0; i < 8; i++) {
                setColorScrollOnClickListener(i, true);
            }
            mColorHorizontalScrollView.setOnTouchListener(null);

            if (mBound && mService != null) {
                mBatterySeekBar.setProgress(mService.mBatteryPercent);
                mBatteryPercent.setText((mService.mIsBatteryCharging ? "Charging " : "") + mService.mBatteryPercent + "%");
                mBatteryTime.setText(getBatteryTime());
            } else {
                mBatterySeekBar.setProgress(0);
                mBatteryPercent.setText("Importing data from Bling...");
                mBatteryTime.setText("");
            }
            mBatteryTime.setVisibility(View.VISIBLE);
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
            mBrightnessSeekBar.setOnTouchListener((v, event) -> {
                return true;
            });

            mColorTitle.setAlpha(0.4f);
            mColorScrollLayout.setAlpha(0.4f);
            mScrollViewStartDivider.setAlpha(0.4f);
            mScrollViewEndDivider.setAlpha(0.4f);
            mColorPickerBtn.setImageAlpha(102);
            mColorPickerBtn.setEnabled(false);
            mColorScrollLayout.setEnabled(false);
            for (int i = 0; i < 8; i++) {
                setColorScrollOnClickListener(i, false);
            }
            mColorHorizontalScrollView.setOnTouchListener((v, event) -> {
                return true;
            });

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
        View newColorView = mPickerDialog.findViewById(R.id.new_color);
        Utils.setDrawableColor(mPickerDialog.findViewById(R.id.prev_color), mCurrentColor);
        Utils.setDrawableColor(newColorView, mCurrentColor);

        CircleColorPicker circleColorPicker = mPickerDialog.findViewById(R.id.color_picker_circle);
        circleColorPicker.setColor(mCurrentColor);
        circleColorPicker.setOnValueChangeListener((view, newColor) -> {
            mCurrentColorHex = "#" + Utils.getHexCode(newColor);

            float[] hsv = new float[3];
            if (mBound && mService != null) {
                Color.colorToHSV(newColor, hsv);
                hsv[2] = (float) mBrightness / BRIGHT_MAX;

                mService.sendColorToLed(Color.HSVToColor(hsv));
            }
            Utils.setDrawableColor(newColorView, newColor);
            Log.d(TAG, "color : " + mCurrentColorHex);
        });

        mPickerDialog.findViewById(R.id.cancel).setOnClickListener((v -> {
            if (mBound && mService != null) {
                float[] hsv = new float[3];
                Color.colorToHSV(mCurrentColor, hsv);
                hsv[2] = (float) mBrightness / BRIGHT_MAX;

                mService.sendColorToLed(Color.HSVToColor(hsv));
            }

            Utils.dismissDialog(mPickerDialog);
        }));

        mPickerDialog.findViewById(R.id.done).setOnClickListener((v -> {
            if (mBound && mService != null) {
                if (colorQueue.size() == 6) {
                    colorQueue.remove();
                }
                colorQueue.offer(mCurrentColorHex);
                Utils.setColorList(getApplicationContext(), colorQueue);

                setColorScrollView();
                setColorCheckbox(0);
            }
            Utils.dismissDialog(mPickerDialog);
        }));
    }

    private void setColorScrollView() {
        colorQueue = Utils.getColorList(getApplicationContext());
        int savedColorCount = colorQueue.size();

        for (int i = 7; i >= 0; i--) {
            if (i < savedColorCount) {
                int color = Color.parseColor(colorQueue.poll());
                Utils.setDrawableColor(((FrameLayout) mColorScrollLayout.getChildAt(i)).getChildAt(0), color);
                setColorScrollOnClickListener(i, true);
            } else {
                Utils.setDrawableColor(((FrameLayout) mColorScrollLayout.getChildAt(i)).getChildAt(0), getColor(R.color.lightGray));
                setColorScrollOnClickListener(i, false);
            }
        }

        Utils.setDrawableColor(((FrameLayout) mColorScrollLayout.getChildAt(savedColorCount)).getChildAt(0), Color.WHITE);
        Utils.setDrawableColor(((FrameLayout) mColorScrollLayout.getChildAt(savedColorCount + 1)).getChildAt(0), Color.parseColor("#FFF8DA"));
        setColorScrollOnClickListener(savedColorCount, true);
        setColorScrollOnClickListener(savedColorCount + 1, true);
        colorQueue = Utils.getColorList(getApplicationContext());
    }

    private void setColorScrollOnClickListener(int index, boolean clickable) {
        if (clickable) {
            ((FrameLayout) mColorScrollLayout.getChildAt(index)).getChildAt(0).setOnClickListener(v -> {
                Log.d(TAG, "color clicked" + index);
                setColorCheckbox(index);
            });
        } else {
            ((FrameLayout) mColorScrollLayout.getChildAt(index)).getChildAt(0).setOnClickListener(v -> {
                // do nothing
                Log.d(TAG, "do not acting" + index);
            });
        }
    }

    private void setColorCheckbox(int index) {
        colorQueue = Utils.getColorList(getApplicationContext());

        for (int i = 7; i >= 0; i--) {
            if (i == index) {
                ((FrameLayout) mColorScrollLayout.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);
                if (i < colorQueue.size()) {
                    mCurrentColor = Color.parseColor(colorQueue.poll());
                } else if (i == colorQueue.size()) {
                    ((FrameLayout) mColorScrollLayout.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);
                    mCurrentColor = Color.WHITE;
                } else if (i == colorQueue.size() + 1) {
                    ((FrameLayout) mColorScrollLayout.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);
                    mCurrentColor = Color.parseColor("#FFF8DA");
                }
            } else {
                ((FrameLayout) mColorScrollLayout.getChildAt(i)).getChildAt(1).setVisibility(View.GONE);
                if (i < colorQueue.size()) {
                    colorQueue.poll();
                }
            }
        }
        colorQueue = Utils.getColorList(getApplicationContext());
        Utils.savePreference(this, "selectedColorIndex", String.valueOf(index));

        ImageView checkboxView = (ImageView) ((FrameLayout) mColorScrollLayout.getChildAt(index)).getChildAt(1);

        if (Utils.canDisplayOnBackground(Color.WHITE, mCurrentColor)) {
            checkboxView.setImageTintList(ColorStateList.valueOf(getColor(R.color.pureWhite)));
        } else {
            checkboxView.setImageTintList(ColorStateList.valueOf(getColor(R.color.darkCheckbox)));
        }

        if (mBound && mService != null) {
            float[] hsv = new float[3];
            Color.colorToHSV(mCurrentColor, hsv);
            hsv[2] = (float) mBrightness / BRIGHT_MAX;

            try {
                int color = Color.HSVToColor(hsv);
                mService.sendColorToLed(color);
                Thread.sleep(10);
                mService.constantSet(color);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setCheeringLight() {
        int[] color = {Color.parseColor("#0100FF"), Color.parseColor("#D9418C"),
                Color.parseColor("#8041D9"), Color.rgb(1, 100, 100)/*Color.parseColor("#151924")*/};
        int[] ledRgb = new int[27];

        for (int i = 0; i < 4; i++) {
            float[] hsv = new float[3];
            Color.colorToHSV(color[i], hsv);
            hsv[2] = (float) mBrightness / BRIGHT_MAX;
            color[i] = Color.HSVToColor(hsv);
        }

        /*mService.sendEachColorToLed(0, Color.red(color[0]), Color.green(color[0]), Color.blue(color[0]));
        mService.sendEachColorToLed(1, Color.red(color[0]), Color.green(color[0]), Color.blue(color[0]));
        mService.sendEachColorToLed(2, Color.red(color[0]), Color.green(color[0]), Color.blue(color[0]));
        mService.sendEachColorToLed(3, Color.red(color[0]), Color.green(color[0]), Color.blue(color[0]));
        mService.sendEachColorToLed(4, Color.red(color[0]), Color.green(color[0]), Color.blue(color[0]));
        mService.sendEachColorToLed(5, Color.red(color[1]), Color.green(color[1]), Color.blue(color[1]));
        mService.sendEachColorToLed(6, Color.red(color[1]), Color.green(color[1]), Color.blue(color[1]));
        mService.sendEachColorToLed(7, Color.red(color[2]), Color.green(color[2]), Color.blue(color[2]));
        mService.sendEachColorToLed(8, Color.red(color[3]), Color.green(color[3]), Color.blue(color[2]));*/

        // 9번째 LED
        ledRgb[0] = Color.red(color[3]);
        ledRgb[1] = Color.red(color[3]);
        ledRgb[2] = Color.red(color[3]);

        // 8번째 LED
        ledRgb[3] = Color.red(color[2]);
        ledRgb[4] = Color.green(color[2]);
        ledRgb[5] = Color.blue(color[2]);

        // 7번째 LED
        ledRgb[6] = Color.red(color[1]);
        ledRgb[7] = Color.green(color[1]);
        ledRgb[8] = Color.blue(color[1]);

        // 6번째 LED
        ledRgb[9] = Color.red(color[1]);
        ledRgb[10] = Color.green(color[1]);
        ledRgb[11] = Color.blue(color[1]);

        // 5번째 LED
        ledRgb[12] = Color.red(color[0]);
        ledRgb[13] = Color.green(color[0]);
        ledRgb[14] = Color.blue(color[0]);

        // 4번째 LED
        ledRgb[15] = Color.red(color[0]);
        ledRgb[16] = Color.green(color[0]);
        ledRgb[17] = Color.blue(color[0]);

        // 3번째 LED
        ledRgb[18] = Color.red(color[0]);
        ledRgb[19] = Color.green(color[0]);
        ledRgb[20] = Color.blue(color[0]);

        // 2번째 LED
        ledRgb[21] = Color.red(color[0]);
        ledRgb[22] = Color.green(color[0]);
        ledRgb[23] = Color.blue(color[0]);

        // 1번째 LED
        ledRgb[24] = Color.red(color[0]);
        ledRgb[25] = Color.green(color[0]);
        ledRgb[26] = Color.blue(color[0]);

        mService.sendEachColorToLedAll(ledRgb);
    }
}
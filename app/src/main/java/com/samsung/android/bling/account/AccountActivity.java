package com.samsung.android.bling.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.bling.MyApplication;
import com.samsung.android.bling.R;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.data.StarMemberInfoVo;
import com.samsung.android.bling.data.UserInfoVo;
import com.samsung.android.bling.service.BlingService;
import com.samsung.android.bling.util.Utils;

import java.util.HashMap;

import cn.cricin.colorpicker.CircleColorPicker;
import cn.cricin.colorpicker.OnValueChangeListener;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "Bling/AccountActivity";

    private static final String FIRST_LOGIN = "-1";

    private TextView mUserIdView;
    private TextView mUserNameView;
    private TextView mChangeUserNameView;
    private TextView mChangeColorView;
    private TextView mChangePasswordView;
    private View mColorView;
    private TextView mRemoveAccountView;
    private Button mSignoutBtn;

    private AlertDialog mAlertDialog;
    private int mCurrentColor;
    private String mCurrentColorHex;

    private RetroClient retroClient;
    private String mId, mUserId, mPassword, mName, mColor, mStarId;

    private boolean mIsStar, mIsShownPassword, mIsShownPasswordCheck, isPasswordConfirmed, isNextClicked;

    private AlertDialog mPhotoKitDialog;

    private boolean mBound = false;
    BlingService mService;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 4.
            Log.d(TAG, "onServiceConnected()");

            BlingService.BTBinder binder = (BlingService.BTBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected()");

            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mIsStar = Utils.getIsStar(getApplicationContext());
        mId = Utils.getPreference(getApplicationContext(), "ID");

        initView();

        retroClient = RetroClient.getInstance(this).createBaseApi();

        if (mIsStar) {
            findViewById(R.id.account_color_view).setVisibility(View.VISIBLE);

            retroClient.getStarData(mId, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "create() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    StarMemberInfoVo data = (StarMemberInfoVo) receivedData;

                    mUserId = data.getMemberID();
                    mPassword = data.getPassword();
                    mName = data.getMemberName();
                    mColor = data.getMemberColor();
                    mStarId = data.getStarID();

                    mUserIdView.setText(mUserId);
                    mUserNameView.setText(mName);
                    mCurrentColor = Color.parseColor(mColor);
                    mColorView.setBackgroundTintList(ColorStateList.valueOf(mCurrentColor));
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    // 서버와 연결은 되었으나, 오류 발생
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

                    mUserId = data.getUserID();
                    mPassword = data.getPassword();
                    mName = data.getNickName();

                    mUserIdView.setText(mUserId);
                    mUserNameView.setText(mName);
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    // 서버와 연결은 되었으나, 오류 발생
                    Log.d(TAG, "create() onFailure : " + code);
                }
            });
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("bling.service.action.NEW_PHOTOKIT")) {
                mPhotoKitDialog = Utils.showDialog(AccountActivity.this, R.layout.photo_kit_dialog);

                mPhotoKitDialog.findViewById(R.id.ok).setOnClickListener(v -> {
                    Utils.dismissDialog(mPhotoKitDialog);
                });
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter("bling.service.action.NEW_PHOTOKIT");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
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
    protected void onDestroy() {
        Utils.dismissDialog(mPhotoKitDialog);
        Utils.dismissDialog(mAlertDialog);

        super.onDestroy();
    }

    private void initView() {
        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(this::onBackPressed, 250));

        mUserIdView = findViewById(R.id.user_id_view);
        mUserNameView = findViewById(R.id.user_name_view);
        mChangeUserNameView = findViewById(R.id.change_user_name_view);
        mChangeColorView = findViewById(R.id.change_color_view);
        mChangePasswordView = findViewById(R.id.change_password_view);
        mColorView = findViewById(R.id.color_view);
        mRemoveAccountView = findViewById(R.id.remove_account_link_view);
        mSignoutBtn = findViewById(R.id.sign_out_btn);

        mChangeUserNameView.setOnClickListener(v -> {
            changeUserName();
        });

        mChangePasswordView.setOnClickListener(v -> {
            changePassword();
        });

        mChangeColorView.setOnClickListener(v -> {
            if (mBound && mService != null && mService.isDrawing()) {
                // 드로잉 중일때는 색상 변경 못하도록
                Toast.makeText(this, "Please change the color after exiting drawing mode", Toast.LENGTH_SHORT).show();
            } else if (!mBound || mService == null) {
                Toast.makeText(this, "Please change the color after connecting with the Bling", Toast.LENGTH_SHORT).show();
            } else {
                changeColor();
            }
        });

        mRemoveAccountView.setOnClickListener(v -> {
            removeAccount();
        });

        mSignoutBtn.setOnClickListener(v -> {
            signOut();
        });
    }

    private void changeUserName() {
        Utils.dismissDialog(mAlertDialog);

        mAlertDialog = Utils.showDialog(this, R.layout.edit_user_info_pop_up_view);
        EditText editUserNameView = mAlertDialog.findViewById(R.id.edit_user_name_view);

        mAlertDialog.findViewById(R.id.done).setOnClickListener(v -> {
            mUserNameView.setText(editUserNameView.getText().toString());

            updateData(setParameters(mUserId, mPassword, editUserNameView.getText().toString(), mColor, mStarId));

            Utils.dismissDialog(mAlertDialog);

            Toast.makeText(this, "The username has been changed.", Toast.LENGTH_SHORT).show();
        });

        mAlertDialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            Utils.dismissDialog(mAlertDialog);
        });
    }

    private void changePassword() {
        Utils.dismissDialog(mAlertDialog);

        mAlertDialog = Utils.showDialog(this, R.layout.edit_password_pop_up_view);

        mIsShownPassword = false;
        mIsShownPasswordCheck = false;
        isPasswordConfirmed = false;
        isNextClicked = false;

        TextView passwordView = mAlertDialog.findViewById(R.id.password_view);
        TextView passwordCheckView = mAlertDialog.findViewById(R.id.password_check_view);
        EditText editPasswordView = mAlertDialog.findViewById(R.id.edit_password_view);
        EditText editPasswordCheckView = mAlertDialog.findViewById(R.id.edit_password_check_view);
        TextView passwordError = mAlertDialog.findViewById(R.id.password_error_view);
        TextView passwordCheckError = mAlertDialog.findViewById(R.id.password_check_error_view);

        ImageButton showPasswordBtn = mAlertDialog.findViewById(R.id.show_password_btn);
        ImageButton showPasswordCheckBtn = mAlertDialog.findViewById(R.id.show_password_check_btn);

        Button doneBtn = mAlertDialog.findViewById(R.id.done);
        Button cancelBtn = mAlertDialog.findViewById(R.id.cancel);

        editPasswordView.addTextChangedListener(new TextWatcher() {
            Drawable drawable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();

                if (text.length() > 0) {
                    if (isNextClicked ? Utils.isCorrectPassword(text) : text.equals(mPassword)) {
                        drawable = getDrawable(R.drawable.bling_setup_check);
                        passwordError.setVisibility(View.INVISIBLE);

                        if (isNextClicked) {
                            passwordCheckView.setVisibility(View.VISIBLE);
                            editPasswordCheckView.setHint("");
                        } else {
                            isPasswordConfirmed = true;
                            doneBtn.setAlpha(1);
                            doneBtn.setEnabled(true);
                        }
                    } else {
                        drawable = getDrawable(R.drawable.bling_setup_error);
                        passwordError.setVisibility(View.VISIBLE);

                        if (isNextClicked) {
                            passwordCheckView.setVisibility(View.INVISIBLE);
                            editPasswordCheckView.setHint(R.string.password_confirm);
                        } else {
                            isPasswordConfirmed = false;
                            doneBtn.setAlpha(0.4f);
                            doneBtn.setEnabled(false);
                        }
                    }

                    showPasswordBtn.setVisibility(View.VISIBLE);
                    editPasswordView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
                } else {
                    if (isNextClicked) {
                        passwordCheckView.setVisibility(View.INVISIBLE);
                        editPasswordCheckView.setHint(R.string.password_confirm);
                    } else {
                        isPasswordConfirmed = false;
                        doneBtn.setAlpha(0.4f);
                        doneBtn.setEnabled(false);
                    }

                    showPasswordBtn.setVisibility(View.GONE);
                    editPasswordView.setCompoundDrawables(null, null, null, null);
                    passwordError.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editPasswordCheckView.addTextChangedListener(new TextWatcher() {
            Drawable drawable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();

                if (text.length() > 0) {
                    if (text.equals(editPasswordView.getText().toString())) {
                        passwordCheckError.setVisibility(View.INVISIBLE);
                        drawable = getDrawable(R.drawable.bling_setup_check);

                        doneBtn.setAlpha(1);
                        doneBtn.setEnabled(true);
                    } else {
                        passwordCheckError.setVisibility(View.VISIBLE);
                        drawable = getDrawable(R.drawable.bling_setup_error);

                        doneBtn.setAlpha(0.4f);
                        doneBtn.setEnabled(false);
                    }
                    showPasswordCheckBtn.setVisibility(View.VISIBLE);
                } else {
                    showPasswordCheckBtn.setVisibility(View.GONE);
                    passwordCheckError.setVisibility(View.GONE);
                    drawable = null;

                    doneBtn.setAlpha(0.4f);
                    doneBtn.setEnabled(false);
                }
                editPasswordCheckView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        showPasswordBtn.setOnClickListener(v -> {
            mIsShownPassword = Utils.showPassword(this, mIsShownPassword, showPasswordBtn, editPasswordView);
        });

        showPasswordCheckBtn.setOnClickListener(v -> {
            mIsShownPasswordCheck = Utils.showPassword(this, mIsShownPasswordCheck, showPasswordCheckBtn, editPasswordCheckView);
        });

        doneBtn.setOnClickListener(v -> {
            if (isNextClicked) {
                updateData(setParameters(mUserId, editPasswordView.getText().toString(), mName, mColor, mStarId));

                Utils.dismissDialog(mAlertDialog);

                Toast.makeText(this, "The password has been changed.", Toast.LENGTH_SHORT).show();
            } else if (isPasswordConfirmed) {
                isNextClicked = true;

                mAlertDialog.findViewById(R.id.password_check_layout).setVisibility(View.VISIBLE);
                passwordView.setText(R.string.new_password);
                passwordError.setText(R.string.password_error);
                editPasswordView.setText("");
                showPasswordBtn.setVisibility(View.GONE);
                doneBtn.setAlpha(0.4f);
                doneBtn.setEnabled(false);
                doneBtn.setText("Done");
                editPasswordView.setCompoundDrawables(null, null, null, null);
            }
        });

        cancelBtn.setOnClickListener(v -> {
            Utils.dismissDialog(mAlertDialog);
        });
    }

    private void changeColor() {
        mAlertDialog = Utils.showDialog(this, R.layout.setting_color_picker_dialog);

        initColorPickerView();
    }

    private void initColorPickerView() {
        View newColorView = mAlertDialog.findViewById(R.id.new_color);
        Utils.setDrawableColor(mAlertDialog.findViewById(R.id.prev_color), mCurrentColor);
        Utils.setDrawableColor(newColorView, mCurrentColor);

        CircleColorPicker circleColorPicker = mAlertDialog.findViewById(R.id.color_picker_circle);
        circleColorPicker.setColor(mCurrentColor);
        circleColorPicker.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public void onValueChanged(View view, int newColor) {
                mCurrentColorHex = "#" + Utils.getHexCode(newColor);
                mCurrentColor = newColor;

                Utils.setDrawableColor(newColorView, mCurrentColor);

                Log.d(TAG, "color : " + mCurrentColorHex);
            }
        });

        mAlertDialog.findViewById(R.id.cancel).setOnClickListener((v -> {
            Utils.dismissDialog(mAlertDialog);
        }));

        mAlertDialog.findViewById(R.id.done).setOnClickListener((v -> {
            mColorView.setBackgroundTintList(ColorStateList.valueOf(mCurrentColor));
            if (mBound && mService != null) {
                mService.mMemberColor = mCurrentColor;
                mService.sendLcdDrawing(2, 0, 0, Integer.parseInt(mId),
                        Color.red(mService.mMemberColor), Color.green(mService.mMemberColor), Color.blue(mService.mMemberColor));
            }
            Utils.savePreference(getApplicationContext(), "MemberColor", mCurrentColorHex);

            updateData(setParameters(mUserId, mPassword, mName, mCurrentColorHex, mStarId));

            Utils.dismissDialog(mAlertDialog);

            Toast.makeText(this, "The color has been changed.", Toast.LENGTH_SHORT).show();
        }));
    }

    private void removeAccount() {
        Utils.dismissDialog(mAlertDialog);

        mAlertDialog = Utils.showDialog(this, R.layout.remove_account_pop_up_view);

        ((TextView) mAlertDialog.findViewById(R.id.body)).setText(getString(R.string.remove_account_dialog_q));

        mAlertDialog.findViewById(R.id.yes).setOnClickListener(v -> {
            Utils.savePreference(getApplicationContext(), "ID", FIRST_LOGIN);
            Utils.savePreference(getApplicationContext(), "MemberColor", "#FFFFFF");

            Utils.dismissDialog(mAlertDialog);

            if (Utils.isMyServiceRunning(this, BlingService.class)) {
                Log.d(TAG, "stop service");
                stopService(new Intent(getApplicationContext(), BlingService.class));
            }
            startActivity(new Intent(this, SigninActivity.class));
            ActivityCompat.finishAffinity(this);
        });

        mAlertDialog.findViewById(R.id.no).setOnClickListener(v -> {
            Utils.dismissDialog(mAlertDialog);
        });
    }

    private void signOut() {
        Utils.dismissDialog(mAlertDialog);

        mAlertDialog = Utils.showDialog(this, R.layout.remove_account_pop_up_view);

        mAlertDialog.findViewById(R.id.yes).setOnClickListener(v -> {
            Utils.savePreference(getApplicationContext(), "ID", FIRST_LOGIN);
            Utils.savePreference(getApplicationContext(), "MemberColor", "#FFFFFF");

            Utils.dismissDialog(mAlertDialog);

            if (Utils.isMyServiceRunning(this, BlingService.class)) {
                Log.d(TAG, "stop service");
                stopService(new Intent(getApplicationContext(), BlingService.class));
            }
            startActivity(new Intent(this, SigninActivity.class));
            ActivityCompat.finishAffinity(this);
        });

        mAlertDialog.findViewById(R.id.no).setOnClickListener(v -> {
            Utils.dismissDialog(mAlertDialog);
        });
    }

    private void updateData(HashMap<String, Object> parameters) {
        if (mIsStar) {
            retroClient.updateStarMemberData(mId, parameters, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "Star's updateData() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    StarMemberInfoVo data = (StarMemberInfoVo) receivedData;

                    mUserId = data.getMemberID();
                    mPassword = data.getPassword();
                    mName = data.getMemberName();
                    mColor = data.getMemberColor();
                    mStarId = data.getStarID();
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "Star's updateData() onFailure : " + code);
                }
            });
        } else {
            retroClient.updateUserData(mId, parameters, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "fan's updateData() onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    UserInfoVo data = (UserInfoVo) receivedData;

                    mUserId = data.getUserID();
                    mPassword = data.getPassword();
                    mName = data.getNickName();
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "fan's updateData() onFailure : " + code);
                }
            });
        }
    }

    private HashMap<String, Object> setParameters(String userId, String password, String name, String color, String starId) {
        HashMap<String, Object> parameters = new HashMap<>();

        if (mIsStar) {
            parameters.put("member_uid", userId);
            parameters.put("member_password", password);
            parameters.put("member_name", name);
            parameters.put("member_color", color);
            parameters.put("star_id", starId);
        } else {
            parameters.put("fan_uid", userId);
            parameters.put("fan_password", password);
            parameters.put("fan_nickname", name);
        }

        return parameters;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
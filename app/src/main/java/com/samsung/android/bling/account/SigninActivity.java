package com.samsung.android.bling.account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.bling.MainActivity;
import com.samsung.android.bling.MyApplication;
import com.samsung.android.bling.R;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.data.StarMemberInfoVo;
import com.samsung.android.bling.data.UserInfoVo;
import com.samsung.android.bling.service.BlingService;
import com.samsung.android.bling.util.BluetoothUtils;
import com.samsung.android.bling.util.Utils;

import java.util.HashMap;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "Bling/SigninActivity";

    private static final String FIRST_LOGIN = "-1";

    private static final int FAIL = -1;
    private static final int FAIL_LOGIN_ID = -2;
    private static final int FAIL_LOGIN_PWD = -3;

    private EditText mEditUserId;
    private EditText mEditPassword;
    private ImageButton mShowPasswordBtn;

    private Button mSigninBtn;
    private TextView mSignupView;

    private RetroClient retroClient;

    private boolean mIsShownPasswd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = Utils.getPreference(getApplicationContext(), "ID");
        boolean isStar = Utils.getIsStar(getApplicationContext());

        Intent intent = new Intent(SigninActivity.this, MainActivity.class);

        if (!FIRST_LOGIN.equals(id)) {
            if (!Utils.isMyServiceRunning(SigninActivity.this, BlingService.class)
                    && BluetoothUtils.isBlingConnected()) {
                Log.d(TAG, "start service");
                startForegroundService(MyApplication.getServiceIntent());
            }

            intent.putExtra("ID", id);
            intent.putExtra("isStar", isStar);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_signin);

        mEditUserId = findViewById(R.id.edit_user_id);
        mEditPassword = findViewById(R.id.edit_password_view);
        mShowPasswordBtn = findViewById(R.id.show_password_btn);
        mSigninBtn = findViewById(R.id.sign_in_btn);
        mSignupView = findViewById(R.id.sign_up_link_view);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        mEditUserId.setOnFocusChangeListener((view, hasfocus) -> {
            if (hasfocus) {
                findViewById(R.id.user_id_view).setVisibility(View.VISIBLE);
                mEditUserId.setHint("");
            } else {
                findViewById(R.id.user_id_view).setVisibility(View.INVISIBLE);
                mEditUserId.setHint("ID");
            }
        });

        mEditPassword.setOnFocusChangeListener((view, hasfocus) -> {
            if (hasfocus) {
                findViewById(R.id.password_view).setVisibility(View.VISIBLE);
                mEditPassword.setHint("");
            } else {
                findViewById(R.id.password_view).setVisibility(View.INVISIBLE);
                mEditPassword.setHint("Password");
            }
        });

        mEditUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setEnableSigninBtn(mEditUserId.length() > 0 && mEditPassword.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mEditPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.length() > 0) {
                    mShowPasswordBtn.setVisibility(View.VISIBLE);
                } else {
                    mShowPasswordBtn.setVisibility(View.GONE);
                }
                setEnableSigninBtn(mEditUserId.length() > 0 && mEditPassword.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mShowPasswordBtn.setOnClickListener(v -> {
            mIsShownPasswd = Utils.showPassword(this, mIsShownPasswd, mShowPasswordBtn, mEditPassword);
        });

        mSigninBtn.setOnClickListener(v -> {
            String userId = mEditUserId.getText().toString();
            String password = mEditPassword.getText().toString();

            HashMap<String, Object> parameters = new HashMap<>();

            if (Utils.isStarId(userId)) {
                Utils.saveIsStar(getApplicationContext(), true);

                parameters.put("member_uid", userId);
                parameters.put("member_password", password);

                retroClient.getStarDataFromLogin(parameters, new RetroCallback() {
                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "sign in btn clicked onError : " + t.toString());
                        t.printStackTrace();
                    }

                    @Override
                    public void onSuccess(int code, Object receivedData) {
                        StarMemberInfoVo data = (StarMemberInfoVo) receivedData;

                        if (data.isSuccess()) {
                            onSignInClick(intent, data.getID(), data.getMemberColor(), true);
                        } else {
                            Toast.makeText(SigninActivity.this, data.getMsg(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(int code, Object errorData) {
                        Log.d(TAG, "sign in btn clicked onFailure : " + code);

                        UserInfoVo data = (UserInfoVo) errorData;
                        Toast.makeText(SigninActivity.this, data.getMsg(), Toast.LENGTH_LONG).show();

                        switch (data.getCode()) {
                            case FAIL:
                                //
                                break;
                            case FAIL_LOGIN_ID:
                                //
                                break;
                            case FAIL_LOGIN_PWD:
                                //
                                break;
                        }
                    }
                });
            } else {
                Utils.saveIsStar(getApplicationContext(), false);

                parameters.put("fan_uid", userId);
                parameters.put("fan_password", password);

                retroClient.getUserDataFromLogin(parameters, new RetroCallback() {
                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "sign in btn clicked onError : " + t.toString());
                        t.printStackTrace();
                    }

                    @Override
                    public void onSuccess(int code, Object receivedData) {
                        UserInfoVo data = (UserInfoVo) receivedData;

                        if (data.isSuccess()) {
                            onSignInClick(intent, data.getID(), "#FFFFFF", false);
                        } else {
                            Toast.makeText(SigninActivity.this, data.getMsg(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(int code, Object errorData) {
                        Log.d(TAG, "sign in btn clicked onFailure : " + code);

                        UserInfoVo data = (UserInfoVo) errorData;
                        Toast.makeText(SigninActivity.this, getString(R.string.login_error_toast), Toast.LENGTH_LONG).show();

                        switch (data.getCode()) {
                            case FAIL:
                                //
                                break;
                            case FAIL_LOGIN_ID:
                                //
                                break;
                            case FAIL_LOGIN_PWD:
                                //
                                break;
                        }
                    }
                });
            }
        });

        mSignupView.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void onSignInClick(Intent intent, String id, String color, boolean isStar) {
        // todo: must be changed
        if (!Utils.isMyServiceRunning(SigninActivity.this, BlingService.class)
                && BluetoothUtils.isBlingConnected()) {
            Log.d(TAG, "start service");
            startForegroundService(MyApplication.getServiceIntent());
        }

        Utils.savePreference(getApplicationContext(), "ID", id);
        Utils.savePreference(getApplicationContext(), "MemberColor", color);
        //Log.d(TAG, id);

        intent.putExtra("ID", id);
        intent.putExtra("isStar", isStar);
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setEnableSigninBtn(boolean isEnabled) {
        if (isEnabled) {
            mSigninBtn.setEnabled(true);
            mSigninBtn.setAlpha(1);
        } else {
            mSigninBtn.setEnabled(false);
            mSigninBtn.setAlpha(0.4f);
        }
    }
}
package com.samsung.android.bling.account;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
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

import com.samsung.android.bling.R;
import com.samsung.android.bling.Retrofit.RetroCallback;
import com.samsung.android.bling.Retrofit.RetroClient;
import com.samsung.android.bling.util.Utils;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "Bling/SignupActivity";

    private EditText mEditUserId;
    private EditText mEditUserName;
    private EditText mEditPassword;
    private EditText mEditPasswordCheck;

    private TextView mPasswordErrorView;

    private ImageButton mShowPasswordBtn;
    private ImageButton mShowPasswordCheckBtn;

    Button mNextBtn;

    private boolean mIsShownPassword, mIsShownPasswordCheck;
    private boolean mIsIdCorrect, mIsNameCorrect, mIsPasswordCorrect, mIsPasswordCheckCorrect;

    private RetroClient retroClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initView();
    }

    private void initView() {
        mEditUserId = findViewById(R.id.edit_user_id);
        mEditUserName = findViewById(R.id.edit_user_name_view);
        mEditPassword = findViewById(R.id.edit_password_view);
        mEditPasswordCheck = findViewById(R.id.edit_password_check_view);
        mPasswordErrorView = findViewById(R.id.password_error_view);
        mShowPasswordBtn = findViewById(R.id.show_password_btn);
        mShowPasswordCheckBtn = findViewById(R.id.show_password_check_btn);
        mNextBtn = findViewById(R.id.sing_up_next_btn);

        mIsShownPassword = false;
        mIsShownPasswordCheck = false;

        mIsIdCorrect = false;
        mIsNameCorrect = false;
        mIsPasswordCorrect = false;
        mIsPasswordCheckCorrect = false;

        retroClient = RetroClient.getInstance(this).createBaseApi();

        mEditUserId.setOnFocusChangeListener((view, hasfocus) -> {
            if (hasfocus) {
                findViewById(R.id.user_id_view).setVisibility(View.VISIBLE);
                mEditUserId.setHint("");
            } else {
                findViewById(R.id.user_id_view).setVisibility(View.INVISIBLE);
                mEditUserId.setHint(getString(R.string.id));
            }
        });

        mEditUserId.addTextChangedListener(new TextWatcher() {
            Drawable drawable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();

                if (text.length() > 0) {
                    if (Utils.isCorrectId(text)) {
                        findViewById(R.id.id_error_view).setVisibility(View.INVISIBLE);
                        drawable = getDrawable(R.drawable.bling_setup_check);

                        mIsIdCorrect = true;
                    } else {
                        findViewById(R.id.id_error_view).setVisibility(View.VISIBLE);
                        drawable = getDrawable(R.drawable.bling_setup_error);

                        mIsIdCorrect = false;
                    }
                } else {
                    findViewById(R.id.id_error_view).setVisibility(View.INVISIBLE);
                    drawable = null;

                    mIsIdCorrect = false;
                }
                mEditUserId.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
                setEnableNextBtn(mIsIdCorrect && mIsNameCorrect && mIsPasswordCorrect && mIsPasswordCheckCorrect);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mEditUserName.setOnFocusChangeListener((view, hasfocus) -> {
            if (hasfocus) {
                findViewById(R.id.user_name_view).setVisibility(View.VISIBLE);
                mEditUserName.setHint("");
                mEditUserName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            } else {
                findViewById(R.id.user_name_view).setVisibility(View.INVISIBLE);
                mEditUserName.setHint(getString(R.string.user_name));

                if (mEditUserName.getText().length() > 0) {
                    mEditUserName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getDrawable(R.drawable.bling_setup_check), null);

                    mIsNameCorrect = true;
                } else {
                    mIsNameCorrect = false;
                }
                setEnableNextBtn(mIsIdCorrect && mIsNameCorrect && mIsPasswordCorrect && mIsPasswordCheckCorrect);
            }
        });

        mEditPassword.setOnFocusChangeListener((view, hasfocus) -> {
            if (hasfocus) {
                findViewById(R.id.password_view).setVisibility(View.VISIBLE);
                mEditPassword.setHint("");
                mPasswordErrorView.setText(getString(R.string.password_error));
            } else {
                findViewById(R.id.password_view).setVisibility(View.INVISIBLE);
                mEditPassword.setHint(getString(R.string.password));
                mPasswordErrorView.setText("");
            }
        });

        mEditPassword.addTextChangedListener(new TextWatcher() {
            Drawable drawable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();

                if (text.length() > 0) {
                    mShowPasswordBtn.setVisibility(View.VISIBLE);

                    if (Utils.isCorrectPassword(text)) {
                        mPasswordErrorView.setTextColor(getColor(R.color.textColor));
                        drawable = getDrawable(R.drawable.bling_setup_check);
                        findViewById(R.id.password_check_layout).setVisibility(View.VISIBLE);

                        mIsPasswordCorrect = true;
                    } else {
                        mPasswordErrorView.setTextColor(getColor(R.color.errorTextColor));
                        drawable = getDrawable(R.drawable.bling_setup_error);
                        findViewById(R.id.password_check_layout).setVisibility(View.GONE);

                        mIsPasswordCorrect = false;
                    }
                } else {
                    mShowPasswordBtn.setVisibility(View.GONE);
                    mPasswordErrorView.setTextColor(getColor(R.color.textColor));
                    drawable = null;

                    mIsPasswordCorrect = false;
                }
                mEditPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
                setEnableNextBtn(mIsIdCorrect && mIsNameCorrect && mIsPasswordCorrect && mIsPasswordCheckCorrect);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mEditPasswordCheck.setOnFocusChangeListener((view, hasfocus) -> {
            if (hasfocus) {
                findViewById(R.id.password_check_view).setVisibility(View.VISIBLE);
                mEditPasswordCheck.setHint("");
            } else {
                findViewById(R.id.password_check_view).setVisibility(View.INVISIBLE);
                mEditPasswordCheck.setHint(getString(R.string.password_confirm));
            }
        });

        mEditPasswordCheck.addTextChangedListener(new TextWatcher() {
            Drawable drawable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();

                if (text.length() > 0) {
                    mShowPasswordCheckBtn.setVisibility(View.VISIBLE);

                    if (text.equals(mEditPassword.getText().toString())) {
                        findViewById(R.id.password_check_error_view).setVisibility(View.INVISIBLE);
                        drawable = getDrawable(R.drawable.bling_setup_check);

                        mIsPasswordCheckCorrect = true;
                    } else {
                        findViewById(R.id.password_check_error_view).setVisibility(View.VISIBLE);
                        drawable = getDrawable(R.drawable.bling_setup_error);

                        mIsPasswordCheckCorrect = false;
                    }
                } else {
                    mShowPasswordCheckBtn.setVisibility(View.GONE);
                    findViewById(R.id.password_check_error_view).setVisibility(View.INVISIBLE);
                    drawable = null;

                    mIsPasswordCheckCorrect = false;
                }
                mEditPasswordCheck.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
                setEnableNextBtn(mIsIdCorrect && mIsNameCorrect && mIsPasswordCorrect && mIsPasswordCheckCorrect);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mShowPasswordBtn.setOnClickListener(v -> {
            mIsShownPassword = Utils.showPassword(this, mIsShownPassword, mShowPasswordBtn, mEditPassword);
        });

        mShowPasswordCheckBtn.setOnClickListener(v -> {
            mIsShownPasswordCheck = Utils.showPassword(this, mIsShownPasswordCheck, mShowPasswordCheckBtn, mEditPasswordCheck);
        });

        mNextBtn.setOnClickListener(v -> {
            //Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            //startActivity(intent);

            HashMap<String, Object> parameters = new HashMap<>();

            parameters.put("fan_uid", mEditUserId.getText().toString());
            parameters.put("fan_password", mEditPassword.getText().toString());
            parameters.put("fan_nickname", mEditUserName.getText().toString());

            retroClient.createUserData(parameters, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG, "sign up btn clicked onError : " + t.toString());
                    t.printStackTrace();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Toast.makeText(SignupActivity.this, "Successfully registered", Toast.LENGTH_LONG).show();

                    finish();
                }

                @Override
                public void onFailure(int code, Object errorData) {
                    Log.d(TAG, "sign up btn clicked onFailure : " + code);
                }
            });
        });
    }

    private void setEnableNextBtn(boolean isEnabled) {
        if (isEnabled) {
            mNextBtn.setEnabled(true);
            mNextBtn.setAlpha(1);
        } else {
            mNextBtn.setEnabled(false);
            mNextBtn.setAlpha(0.4f);
        }
    }
}

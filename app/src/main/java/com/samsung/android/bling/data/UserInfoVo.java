package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

public class UserInfoVo {
    @SerializedName("msg")
    private String msg;

    @SerializedName("code")
    private int code;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Data data;

    class Data {
        @SerializedName("userID")
        String ID;

        @SerializedName("userUID")
        String userID;

        @SerializedName("user_NICKNAME")
        String nickName;

        @SerializedName("user_PASSWD")
        String password;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getID() {
        return data.ID;
    }

    public String getUserID() {
        return data.userID;
    }

    public String getNickName() {
        return data.nickName;
    }

    public String getPassword() {
        return data.password;
    }
}

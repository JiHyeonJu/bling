package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

public class StarMemberInfoVo {
    @SerializedName("msg")
    private String msg;

    @SerializedName("code")
    private int code;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Data data;

    class Data {
        @SerializedName("member_ID")
        String ID;

        @SerializedName("memberUID")
        String memberID;

        @SerializedName("member_NAME")
        String memberName;

        @SerializedName("member_PASSWD")
        String password;

        @SerializedName("member_COLOR")
        String memberColor;

        @SerializedName("starID")
        private Star star;

        class Star {
            @SerializedName("star_ID")
            String ID;

            @SerializedName("star_NAME")
            String starName;
        }
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

    public String getMemberID() {
        return data.memberID;
    }

    public String getMemberName() {
        return data.memberName;
    }

    public String getPassword() {
        return data.password;
    }

    public String getMemberColor() {
        return data.memberColor;
    }

    public String getStarID() {
        return data.star.ID;
    }

    public String getStarName() {
        return data.star.starName;
    }
}

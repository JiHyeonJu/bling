package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AlbumVo {
    @SerializedName("msg")
    private String msg;

    @SerializedName("code")
    private int code;

    @SerializedName("success")
    private boolean success;

    @SerializedName("list")
    private ArrayList<AlbumItemVo> list;

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public ArrayList<AlbumItemVo> getList() {
        return list;
    }
}

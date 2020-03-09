package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PhotoKitVo {
    @SerializedName("msg")
    private String msg;
    private ArrayList<PhotoKitItemVo> list;

    public String getMsg() {
        return msg;
    }

    public ArrayList<PhotoKitItemVo> getList() {
        return list;
    }
}

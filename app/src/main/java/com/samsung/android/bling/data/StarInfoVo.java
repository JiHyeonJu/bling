package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

public class StarInfoVo {
    @SerializedName("data")
    Data data;

    class Data {
        @SerializedName("star_CONN_STATE")
        String connState;

        @SerializedName("star_NAME")
        String starName;
    }

    public String getStarStatus() {
        return data.connState;
    }

    public String getStarName() {
        return data.starName;
    }
}

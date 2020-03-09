package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

public class AlbumItemVo {
    @SerializedName("ab_TITLE")
    String title;

    @SerializedName("ab_SUB_TITLE")
    String subTitle;

    @SerializedName("ab_MEMBER_CNT")
    int memberCount;

    @SerializedName("ab_IMGURL")
    String albumImageUrl;

    @SerializedName("ab_COLOR")
    String albumColor;

    @SerializedName("ab_ISSUE_DATE")
    String albumDate;

    @SerializedName("ab_MEMBER_NAME_LIST")
    String memberNameList;

    @SerializedName("ab_MEMBER_ID_LIST")
    String memberIdList;

    @SerializedName("starID")
    Star star;

    class Star {
        @SerializedName("star_NAME")
        String starName;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getAlbumImageUrl() {
        return albumImageUrl;
    }

    public String getStarName() {
        return star.starName;
    }

    public String getAlbumColor() {
        return albumColor;
    }

    public String getAlbumDate() {
        return albumDate;
    }

    public String getMemberNameList() {
        return memberNameList;
    }

    public String getMemberIdList() {
        return memberIdList;
    }
}

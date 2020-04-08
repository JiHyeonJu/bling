package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

public class PhotoKitVo {
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private Data data;

    class Data {
        @SerializedName("ptk_ID")
        PhotoKit photoKit;
    }

    class PhotoKit {
        @SerializedName("member_ID")
        Member member;

        @SerializedName("ab_ID")
        Album album;
    }

    class Member {
        @SerializedName("member_ID")
        String memberId;
    }

    class Album {
        @SerializedName("ab_CT")
        int abCT;

        @SerializedName("ab_MEMBER_ID_LIST")
        String memberIdList;
    }

    public String getMemberId() {
        return data.photoKit.member.memberId;
    }

    public int getAlbumCT() {
        return data.photoKit.album.abCT;
    }

    public String getMemberIdList() {
        return data.photoKit.album.memberIdList;
    }
}

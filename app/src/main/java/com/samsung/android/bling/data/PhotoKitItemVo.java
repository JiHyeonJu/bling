package com.samsung.android.bling.data;

import com.google.gson.annotations.SerializedName;

public class PhotoKitItemVo {

    @SerializedName("ptk_ID")
    private PhotoKit photoKit;

    class PhotoKit {
        @SerializedName("ptkNfcInfo")
        String photoKitNfc;

        @SerializedName("pkt_AB_MEMBER_IMGURL")
        String memberImageUrl;

        @SerializedName("member_ID")
        Member member;

        /*@SerializedName("starID")
        Star star;*/

        @SerializedName("ab_ID")
        Album album;
    }

    class Member {
        @SerializedName("member_ID")
        String memberId;
    }

    /*class Star {
        @SerializedName("star_NAME")
        String starName;
    }*/

    class Album {
        @SerializedName("ab_CT")
        int abCT;

        @SerializedName("ab_MEMBER_ID_LIST")
        String memberIdList;
    }

    public String getMemberId() {
        return photoKit.member.memberId;
    }

    /*public String getStarName() {
        return photoKit.star.starName;
    }*/

    public String getMemberImageUrl() {
        return photoKit.memberImageUrl;
    }

    public int getAlbumCT() {
        return photoKit.album.abCT;
    }

    public String getMemberIdList() {
        return photoKit.album.memberIdList;
    }
}

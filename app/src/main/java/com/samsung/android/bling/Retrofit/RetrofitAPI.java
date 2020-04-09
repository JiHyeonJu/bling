package com.samsung.android.bling.Retrofit;

import com.samsung.android.bling.data.AlbumVo;
import com.samsung.android.bling.data.PhotoKitVo;
import com.samsung.android.bling.data.StarInfoVo;
import com.samsung.android.bling.data.StarMemberInfoVo;
import com.samsung.android.bling.data.UserInfoVo;
import com.samsung.android.bling.data.PhotoKitListVo;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RetrofitAPI {

    final String Base_URL = "https://52.79.216.28:8080/v1/";

    // 02. User : User Controller
    @FormUrlEncoded
    @POST("user")
    Call<UserInfoVo> createUserData(@FieldMap HashMap<String, Object> parameters);

    @FormUrlEncoded
    @PUT("user/{fan_id}")
    Call<UserInfoVo> updateUserData(@Path("fan_id") String id, @FieldMap HashMap<String, Object> parameters);

    @GET("user/{fan_id}")
    Call<UserInfoVo> getUserData(@Path("fan_id") String id);


    // 04. Member : Member Controller
    @GET("member/{member_id}")
    Call<StarMemberInfoVo> getStarData(@Path("member_id") String id);

    @FormUrlEncoded
    @PUT("member/{member_id}")
    Call<StarMemberInfoVo> updateStarMemberData(@Path("member_id") String id, @FieldMap HashMap<String, Object> parameters);


    // 05. Album : Album Controller
    @GET("albums")
    Call<AlbumVo> getAlbumData();


    // 06. PhotoKit : Photo Kit Controller
    /*@GET("photoKit/{ptk_nfc_info}")
    Call<PhotoKitVo> getPhotoKitDataFromNfc(@Path("ptk_nfc_info") String id);*/


    // 08. Login : Login Controller
    @FormUrlEncoded
    @POST("login/user")
    Call<UserInfoVo> getUserDataFromLogin(@FieldMap HashMap<String, Object> parameters);

    @FormUrlEncoded
    @POST("login/member")
    Call<StarMemberInfoVo> getStarDataFromLogin(@FieldMap HashMap<String, Object> parameters);


    // 09. PhotoKitReg : Photo Kit Reg Controller
    @GET("photoKitReg/user/{user_id}")
    Call<PhotoKitListVo> getUserPhotoKitList(@Path("user_id") String id);

    @FormUrlEncoded
    @POST("photoKitRegByNFCTag/{ptk_nfc_info}")
    Call<PhotoKitVo> registerPhotoKitFromNfc(@FieldMap HashMap<String, Object> parameter, @Path("ptk_nfc_info") String id);


    // 10. Connection : Conn Controller
    @GET("conn/star/{star_id}")
    Call<StarInfoVo> getStarConnection(@Path("star_id") String id);

    @FormUrlEncoded
    @PUT("conn/member/{member_id}")
    Call<StarMemberInfoVo> updateStarConnection(@Path("member_id") String id, @FieldMap HashMap<String, Object> parameters);
}
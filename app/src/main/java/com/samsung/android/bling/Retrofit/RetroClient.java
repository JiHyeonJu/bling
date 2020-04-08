package com.samsung.android.bling.Retrofit;

import android.content.Context;

import com.samsung.android.bling.data.AlbumVo;
import com.samsung.android.bling.data.PhotoKitItemVo;
import com.samsung.android.bling.data.PhotoKitListVo;
import com.samsung.android.bling.data.PhotoKitVo;
import com.samsung.android.bling.data.StarInfoVo;
import com.samsung.android.bling.data.StarMemberInfoVo;
import com.samsung.android.bling.data.UserInfoVo;

import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Callback;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {

    public static String baseUrl = RetrofitAPI.Base_URL;

    private RetrofitAPI apiService;
    private static Retrofit retrofit;
    private static Context mContext;

    private static class SingletonHolder {
        private static RetroClient INSTANCE = new RetroClient(mContext);
    }

    public static RetroClient getInstance(Context context) {
        if (context != null) {
            mContext = context;
        }
        return SingletonHolder.INSTANCE;
    }

    private RetroClient(Context context) {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .baseUrl(baseUrl)
                .build();
    }

    public RetroClient createBaseApi() {
        apiService = create(RetrofitAPI.class);
        return this;
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }

    // [[ 02. User : User Controller
    public void createUserData(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.createUserData(parameters).enqueue(new Callback<UserInfoVo>() {
            @Override
            public void onResponse(Call<UserInfoVo> call, Response<UserInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<UserInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void updateUserData(String id, HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.updateUserData(id, parameters).enqueue(new Callback<UserInfoVo>() {
            @Override
            public void onResponse(Call<UserInfoVo> call, Response<UserInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<UserInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void getUserData(String id, final RetroCallback callback) {
        apiService.getUserData(id).enqueue(new Callback<UserInfoVo>() {
            @Override
            public void onResponse(Call<UserInfoVo> call, Response<UserInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<UserInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    // ]] 02. User : User Controller


    // [[ 04. Member : Member Controller
    public void getStarData(String id, final RetroCallback callback) {
        apiService.getStarData(id).enqueue(new Callback<StarMemberInfoVo>() {
            @Override
            public void onResponse(Call<StarMemberInfoVo> call, Response<StarMemberInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<StarMemberInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void updateStarMemberData(String id, HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.updateStarMemberData(id, parameters).enqueue(new Callback<StarMemberInfoVo>() {
            @Override
            public void onResponse(Call<StarMemberInfoVo> call, Response<StarMemberInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<StarMemberInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    // ]] 04. Member : Member Controller


    // [[ 05. Album : Album Controller
    public void getAlbumData(final RetroCallback callback) {
        apiService.getAlbumData().enqueue(new Callback<AlbumVo>() {
            @Override
            public void onResponse(Call<AlbumVo> call, Response<AlbumVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<AlbumVo> call, Throwable t) {

            }
        });
    }
    // ]] 05. Album : Album Controller


    // [[ 06. PhotoKit : Photo Kit Controller
    /*public void getPhotoKitDataFromNfc(String nfcInfo, final RetroCallback callback) {
        apiService.getPhotoKitDataFromNfc(nfcInfo).enqueue(new Callback<PhotoKitVo>() {
            @Override
            public void onResponse(Call<PhotoKitVo> call, Response<PhotoKitVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<PhotoKitVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }*/
    // ]] 06. PhotoKit : Photo Kit Controller

    // [[ 08. Login : Login Controller
    public void getUserDataFromLogin(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.getUserDataFromLogin(parameters).enqueue(new Callback<UserInfoVo>() {
            @Override
            public void onResponse(Call<UserInfoVo> call, Response<UserInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<UserInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void getStarDataFromLogin(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.getStarDataFromLogin(parameters).enqueue(new Callback<StarMemberInfoVo>() {
            @Override
            public void onResponse(Call<StarMemberInfoVo> call, Response<StarMemberInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<StarMemberInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    // ]] 08. Login : Login Controller


    // [[ 09. PhotoKitReg : Photo Kit Reg Controller
    public void getUserPhotoKitList(String id, final RetroCallback callback) {
        apiService.getUserPhotoKitList(id).enqueue(new Callback<PhotoKitListVo>() {
            @Override
            public void onResponse(Call<PhotoKitListVo> call, Response<PhotoKitListVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<PhotoKitListVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void registerPhotoKitFromNfc(HashMap<String, Object> parameter, String id, final RetroCallback callback) {
        apiService.registerPhotoKitFromNfc(parameter, id).enqueue(new Callback<PhotoKitVo>() {
            @Override
            public void onResponse(Call<PhotoKitVo> call, Response<PhotoKitVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<PhotoKitVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    // ]] 09. PhotoKitReg : Photo Kit Reg Controller


    // [[ 10. Connection : Conn Controller
    public void getStarConnection(String id, final RetroCallback callback) {
        apiService.getStarConnection(id).enqueue(new Callback<StarInfoVo>() {
            @Override
            public void onResponse(Call<StarInfoVo> call, Response<StarInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<StarInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void updateStarConnection(String id, HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.updateStarConnection(id, parameters).enqueue(new Callback<StarMemberInfoVo>() {
            @Override
            public void onResponse(Call<StarMemberInfoVo> call, Response<StarMemberInfoVo> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<StarMemberInfoVo> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    // ]] 10. Connection : Conn Controller
}

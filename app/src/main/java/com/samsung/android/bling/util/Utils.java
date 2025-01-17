package com.samsung.android.bling.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.ColorUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samsung.android.bling.MainActivity;
import com.samsung.android.bling.R;
import com.samsung.android.bling.account.SigninActivity;

import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import cn.cricin.colorpicker.ThumbDrawable;
import okhttp3.OkHttpClient;

public class Utils {
    private static final String TAG = "Bling/Utils";

    public static NotificationCompat.Builder showNotification(Context context, boolean isService, int notificationId, String channelId, String title, String messageBody) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.bling_status_icon)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!isService) {
            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());
        }
        return notificationBuilder;
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

    public static void saveIsStar(Context context, boolean isStar) {
        Log.d(TAG, "isStar: " + isStar);

        // save sharedpreference
        SharedPreferences pref = context.getSharedPreferences("blingData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isStar", isStar);
        editor.commit();
    }

    public static boolean getIsStar(Context context) {
        SharedPreferences pref = context.getSharedPreferences("blingData", Activity.MODE_PRIVATE);

        return pref.getBoolean("isStar", false);
    }

    public static void savePreference(Context context, String tag, String value) {
        Log.d(TAG, tag + ", " + value);

        // save sharedpreference
        SharedPreferences pref = context.getSharedPreferences("blingData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(tag, value);
        editor.commit();
    }

    public static String getPreference(Context context, String tag) {
        // get info from sharedpreference
        SharedPreferences pref = context.getSharedPreferences("blingData", Activity.MODE_PRIVATE);
        String str = pref.getString(tag, "-1");

        if (str.equals("-1") && tag.equals("MemberColor")) {
            str = "#FFFFFF";
        } else if (str.equals("-1") && tag.equals("cheeringMode")) {
            str = "false";
        }

        return str;
    }

    public static void setColorList(Context context, Queue<String> data) {
        // int array to string
        String str = "";
        while (!data.isEmpty()) {
            str = str + data.poll() + " ";
        }
        Log.d(TAG + "setList", str);

        // save sharedpreference
        SharedPreferences pref = context.getSharedPreferences("blingData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("savedColor", str);
        editor.commit();
    }

    public static Queue<String> getColorList(Context context) {
        // get info from sharedpreference
        SharedPreferences pref = context.getSharedPreferences("blingData", Activity.MODE_PRIVATE);
        String str = pref.getString("savedColor", "");

        Queue<String> data = new LinkedList<>();

        if (str.length() > 0) {
            // string to int array
            String[] arrayData = str.split("\\s+");

            for (int i = 0; i < arrayData.length; i++) {
                data.offer(arrayData[i]);
                Log.d(TAG, "data[" + i + "] = " + arrayData[i]);
            }
        }

        return data;
    }

    public static int getCurrentColor(Context context) {
        int currentColor = Color.WHITE;

        Queue<String> colorQueue = new LinkedList<>();
        colorQueue = Utils.getColorList(context);

        int selectedColorIndex = Integer.parseInt(Utils.getPreference(context, "selectedColorIndex"));
        if (selectedColorIndex == -1) {
            selectedColorIndex = 0;
        }

        for (int i = 7; i >= 0; i--) {
            if (i == selectedColorIndex) {
                if (i < colorQueue.size()) {
                    currentColor = Color.parseColor(colorQueue.poll());
                } else if (i == colorQueue.size()) {
                    currentColor = Color.WHITE;
                } else if (i == colorQueue.size() + 1) {
                    currentColor = Color.parseColor("#FFF8DA");
                }
            } else {
                if (i < colorQueue.size()) {
                    colorQueue.poll();
                }
            }
        }

        return currentColor;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d(TAG, serviceClass.getName() + ", " + service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void setBarDark(Activity activity, int color) {
        // set background dark
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(activity.getResources().getColor(color));
        window.setNavigationBarColor(activity.getResources().getColor(color));

        // set icon light
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(0);
    }

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    public static int getDisplayHeight(Activity activity) {
        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;*/

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        int statusBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }

        int bottomBarHeight = 0;
        int resourceIdBottom = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceIdBottom > 0) {
            bottomBarHeight = activity.getResources().getDimensionPixelSize(resourceIdBottom);
        }

        //int realDeviceWidth = displayMetrics.widthPixels;
        int realDeviceHeight = displayMetrics.heightPixels - statusBarHeight - bottomBarHeight;

        return realDeviceHeight;
    }

    public static boolean canDisplayOnBackground(int color, int backgroundColor) {
        return ColorUtils.calculateContrast(color, backgroundColor) > 1.5f;
    }

    public static void setDrawableColor(View view, int Color) {
        GradientDrawable drawable = (GradientDrawable) view.getBackground();
        drawable.setColor(Color);
    }

    public static String getHexCode(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "%02X%02X%02X%02X", a, r, g, b);
    }

    public static AlertDialog showDialog(Activity activity, int resource) {
        View view = LayoutInflater.from(activity).inflate(resource, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setView(view)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static void dismissDialog(AlertDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public static boolean showPassword(Context context, boolean isShown, ImageButton imgBtn, EditText text) {
        if (isShown) {
            isShown = false;
            imgBtn.setImageDrawable(context.getDrawable(R.drawable.bling_setup_password_show));
            text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            isShown = true;
            imgBtn.setImageDrawable(context.getDrawable(R.drawable.bling_setup_password_hide));
            text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        text.setSelection(text.length());

        return isShown;
    }

    public static boolean isStarId(String id) {
        if (id.contains("star#")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCorrectId(String id) {
        if (!isStarId(id)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCorrectPassword(String password) {
        if (password.length() > 3) {
            return true;
        } else {
            return false;
        }
    }

    public static String jsonParser(String str) {
        // Json parsing
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(str);

        return jsonObject.toString();
    }

    public static void setFinalStatic() {
        /*field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);*/

        /*try {
            Class clazz = ThumbDrawable.class;
            Field fld = clazz.getDeclaredField("DEFAULT_INNER_SIZE");
            fld.setAccessible(true);
            fld.set(null, 100);
            Log.d("jjh~", "Class name: " + clazz.getName());
            Log.d("jjh~", "" + fld.get(null));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*try {
            ThumbDrawable t = new ThumbDrawable(context);
            Class clazz = ThumbDrawable.class;
            Field fld = clazz.getDeclaredField("mPressed");
            fld.setAccessible(true);
            fld.set(t, true);

            *//*Method method = clazz.getDeclaredMethod("draw", Canvas.class);
            method.invoke(t, (Object[]) null);*//*

            Log.d("jjh~", "Class name: " + clazz.getName());
            Log.d("jjh~", "" + fld.get(t));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
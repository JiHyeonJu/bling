package com.samsung.android.bling;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = "Bling/MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "jjh Application start");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        createNotificationChannel(notificationManager, "service_channel_id", "Bling Service", "");
        createNotificationChannel(notificationManager, "star_status_channel_id", "Star On-line", "");
    }

    private void createNotificationChannel(NotificationManager notificationManager, String channelId, String name, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(false);
            //channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }
    }
}

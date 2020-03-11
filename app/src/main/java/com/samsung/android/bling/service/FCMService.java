package com.samsung.android.bling.service;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.samsung.android.bling.util.Utils;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "Bling/FCMService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "received");
        sendNotification(remoteMessage.getData().get("message"));
        sendMessageToActivity(remoteMessage.getData().get("message"));
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /*
     *//**
     * Schedule async work using WorkManager.
     *//*
        private void scheduleJob() {
            // [START dispatch_job]
            OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                    .build();
            WorkManager.getInstance().beginWith(work).enqueue();
            // [END dispatch_job]
        }

        *//**
     * Handle time allotted to BroadcastReceivers.
     *//*
        private void handleNow() {
            Log.d(TAG, "Short lived task is done.");
        }
        */

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Utils.showNotification(this, false, 1001, "FCM_test", messageBody);
    }

    private void sendMessageToActivity(String messages) {
        Intent new_intent = new Intent();
        new_intent.setAction("ACTION_STRING_ACTIVITY");
        new_intent.putExtra("Status", messages);

        sendBroadcast(new_intent);
    }
}

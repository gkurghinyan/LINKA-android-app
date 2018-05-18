package com.linka.lockapp.aos.module.gcm;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.module.helpers.LogHelper;

/**
 * Created by kyle on 4/29/18.
 */

public class myFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        LogHelper.e("Firebase", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            LogHelper.e("Firebase", "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            LogHelper.e("Firebase", "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            LogHelper.e("Firebase", "Body: " + remoteMessage.getNotification().getBody());
        }

        //If new lock is added, then we will notify the user
        AppMainActivity.getInstance().getLocks();

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}

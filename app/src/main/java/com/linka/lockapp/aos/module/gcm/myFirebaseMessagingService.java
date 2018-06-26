package com.linka.lockapp.aos.module.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.Constants;
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

        String title = "Request For Permission";
        String text = remoteMessage.getData().get("message");
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            LogHelper.e("Firebase", "Message Notification Title: " + title);
            LogHelper.e("Firebase", "Body: " + text);
        }

        //If new lock is added, then we will notify the user
        Intent resultIntent = new Intent(this, AppMainActivity.class);
        resultIntent.putExtra(Constants.IS_IT_OPEN_FROM_NOTIFICATION,true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent);

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }

//        AppMainActivity.getInstance().getLocks();

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}

package com.linka.lockapp.aos.module.helpers;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaActivity.LinkaActivityType;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;

import org.greenrobot.eventbus.EventBus;

import br.com.goncalves.pugnotification.notification.Load;
import br.com.goncalves.pugnotification.notification.PugNotification;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Vanson on 3/4/16.
 */
public class NotificationsHelper {
    public static final String LINKA_NOT_LOCKED = "LinkaNotLocked";
    public static final String LINKA_NOTIFICATION_ACTION = "Linka Notification";

    private MediaPlayer mediaPlayer;

    private int AUDIO_TAMPERALERT = R.raw.audio_tamper_alert;
    private int AUDIO_BACKINRANGE = R.raw.audio_back_in_range;
    private int AUDIO_OUTOFRANGE = R.raw.audio_out_of_range;
    private int AUDIO_BATTERYLOW = R.raw.audio_battery_low;
    private int AUDIO_STALL = R.raw.audio_stall;

    public static NotificationsHelper notificationsHelper;
    public static NotificationsHelper getInstance() {
        if (notificationsHelper == null) {
            notificationsHelper = new NotificationsHelper();
        }
        return notificationsHelper;
    }

    public boolean CreateLinkaNotificationMessage(LinkaActivity activity) {
        return CreateLinkaNotificationMessage(activity, AppBluetoothService.getInstance().getContext());
    }

    public boolean CreateLinkaNotificationMessage(LinkaActivity activity, Context context) {
        String title = "";
        String message = "";
        int audio = 0;
        boolean loop = false;

        LinkaNotificationSettings settings = LinkaNotificationSettings.getSettings();

        if (activity.linka_activity_status == LinkaActivityType.isBatteryLow.getValue()
                && settings.settings_linka_battery_low_alert) {
            title = _.i(R.string.notif_battery_low);
            message = _.i(R.string.battery_low_notification_massage_part1) + " " +_.i(R.string.battery_low_notification_massage_part2);
            audio = AUDIO_BATTERYLOW;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isBatteryCriticallyLow.getValue()
                && settings.settings_linka_battery_critically_low_alert) {
            title = _.i(R.string.notif_battery_low);
            message = _.i(R.string.notif_below) + " " + activity.batteryPercent + "% "+_.i(R.string.notif_battery_remaining)+" "+_.i(R.string.notif_please_charge_soon)+"";
            audio = AUDIO_BATTERYLOW;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isTamperAlert.getValue()) {
            title = _.i(R.string.notif_tamper_alert);
            message = _.i(R.string.notif_check_your_bike);
            audio = AUDIO_TAMPERALERT;
            loop = true;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isAutoUnlocked.getValue()){
            title = _.i(R.string.auto_unlock_notif_title);
            message = _.i(R.string.auto_unlock_notif_message);
            audio = AUDIO_BACKINRANGE;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isSleep.getValue()){
            title = _.i(R.string.sleep_notification);
            message = _.i(R.string.sleep_notification_desc);
            audio = AUDIO_BACKINRANGE;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isOutOfRange.getValue()
                && settings.settings_out_of_range_alert && SleepNotificationService.getInstance().lastSleepTime != 0) {
            title = _.i(R.string.out_of_range_alert);
            message = _.i(R.string.your_lock_is) + " " + _.i(R.string.out_of_range_alert);
            audio = AUDIO_OUTOFRANGE;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isBackInRange.getValue()
                && settings.settings_back_in_range_alert) {
            title = _.i(R.string.back_in_range_alert);
            message = _.i(R.string.your_lock_is) + " " + _.i(R.string.back_in_range_alert);
            audio = AUDIO_BACKINRANGE;
        }
        else if (activity.linka_activity_status == LinkaActivityType.isStalled.getValue()) {
//            title = _.i(R.string.warning);
//            message = _.i(R.string.stall);
//            audio = AUDIO_STALL;
            EventBus.getDefault().post(LINKA_NOT_LOCKED);
            return false;
        }
        else {
            return false;
        }

        return CreateLinkaNotificationMessage(title, message, audio, loop, activity, context);
    }

    public boolean CreateLinkaNotificationMessage(final String title, final String message, final int audio, final boolean loop, LinkaActivity linkaActivity, final Context context) {
//        if (AppDelegate.isActivityVisible()) {
//            // show alert
//            if (linkaActivity.alarm) {
//
//                if (mediaPlayer == null && audio != 0) {
//                    mediaPlayer = MediaPlayer.create(AppDelegate.getInstance(), audio);
//                    mediaPlayer.setLooping(loop);
//                    mediaPlayer.start();
//                }
//            }
//
//            if (context != null) {
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        new AlertDialog.Builder(context)
//                                .setTitle(title)
//                                .setMessage(message)
//                                .setNegativeButton(R.string.dismiss, null)
//                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                    @Override
//                                    public void onDismiss(DialogInterface dialog) {
//                                        if (mediaPlayer != null) {
//                                            mediaPlayer.stop();
//                                            mediaPlayer.release();
//                                            mediaPlayer = null;
//                                        }
//                                    }
//                                })
//                                .show();
//                    }
//                });
//            }
//
//
//        } else {
            // create notification
        Intent intent = new Intent(context, AppMainActivity.class);
        intent.setAction(LINKA_NOTIFICATION_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Load load = PugNotification.with(AppDelegate.getInstance())
                .load()
                .autoCancel(true)
                .identifier(linkaActivity.linka_activity_status)
                .title(title)
                .message(message)
                .smallIcon(R.drawable.ic_action_name)
                .largeIcon(R.mipmap.ic_launcher)
                .flags(Notification.DEFAULT_ALL)
                .click(pendingIntent);

        if (linkaActivity.alarm) {
            if (audio != 0) {
                load = load.sound(Uri.parse("android.resource://com.linka.lockapp.aos/" + audio));
            }
        }

        load.simple().build();


        return true;
    }

    public static void createNotification(Context context,String title,String massage){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(massage)
                        .setAutoCancel(true);

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }

}

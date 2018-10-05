package com.linka.lockapp.aos.module.model;

import android.location.Address;
import android.location.Geocoder;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.LinkaActivity.LinkaActivityType;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vanson on 18/2/16.
 */

//This class implements the LIST of activities displayed on the "Activities" Page

public class Notification implements Serializable {

    public long id;
    public String body = "";
    public String time = "";
    public LinkaActivityType type = LinkaActivityType.isUnknown;
    public String from = "";
    public String latitude = "";
    public String longitude = "";
    private String address = "";
    public boolean isRead = false;
    private List<Address> addresses;

    private Geocoder geocoder = new Geocoder(AppDelegate.getInstance(), Locale.getDefault());


    public static List<Notification> fromLinkaActivities(List<LinkaActivity> activities) {
        List<Notification> notifications = new ArrayList<>();
        for (LinkaActivity activity : activities) {
            Notification notification = new Notification();
            LinkaActivityType type = LinkaActivityType.fromInt(activity.linka_activity_status);

            notification.time = activity.getFormattedDate();
            notification.from = activity.lock_name;
            notification.type = type;
            notification.isRead = activity.isRead;
            notification.id = activity.getId();

            if (type == LinkaActivityType.isLocked) {

                if ((activity.longitude.equals("") && activity.latitude.equals(""))
                        ||
                        (activity.longitude.equals("0") && activity.latitude.equals("0"))) {
                    notification.body = _.i(R.string.act_locked);
                } else {
                    LinkaAddress linkaAddress = LinkaAddress.getAddressForLatLng(activity.latitude, activity.longitude);
                    if (linkaAddress != null) {
                        notification.body = _.i(R.string.act_locked_at) + " " + linkaAddress.address;
                    } else {
                        try {
                            notification.addresses = notification.geocoder.getFromLocation(Double.parseDouble(activity.latitude), Double.parseDouble(activity.longitude), 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(notification.addresses != null) {
                            notification.address = notification.addresses.get(0).getAddressLine(0);
                        }
                        notification.body = _.i(R.string.act_locked_at) + " " + notification.address;
                    }
                    notification.latitude = activity.latitude;
                    notification.longitude = activity.longitude;
                }

            } else if (type == LinkaActivityType.isUnlocked || type == LinkaActivityType.isAutoUnlocked) {

                if(type == LinkaActivityType.isAutoUnlocked) {
                    notification.body = _.i(R.string.auto_unlock_notif_title)+"\n"+_.i(R.string.auto_unlock_notif_message);
                }else {
                    notification.body = _.i(R.string.act_unlocked);
                }

                Linka linka = Linka.getLinkaById(activity.linka_id);
                if (linka != null && linka.timestamp_locked != null && !linka.timestamp_locked.equals("")) {
                    Date lockedDate = activity.getTimestampLockedDate();
                    if (lockedDate != null) {
                        Date activityDate = activity.getDate();


                        long diffMs = activityDate.getTime() - lockedDate.getTime();
                        long diffSec = TimeUnit.MILLISECONDS.toSeconds(diffMs);



                        long after_sec = diffSec % 60;
                        long after_min = (diffSec / 60) % 60;
                        long after_hours = ((diffSec / 60) / 60) % 24;
                        long after_days = (((diffSec / 60) / 60) / 24);

                        String after = "";
                        long after_depth = 0;

                        if (after_days > 0 && after_depth < 2) {
                            after += "" + after_days + " " + (after_days != 1 ? _.i(R.string.days) : _.i(R.string.day)) + " ";
                            after_depth += 1;
                        }
                        if (after_hours > 0 && after_depth < 2) {
                            after += "" + after_hours + " " + (after_hours != 1 ? _.i(R.string.hrs) : _.i(R.string.hr)) + " ";
                            after_depth += 1;
                        }
                        if (after_min > 0 && after_depth < 2) {
                            after += "" + after_min + " " + (after_min != 1 ? _.i(R.string.mins) : _.i(R.string.min)) + " ";
                            after_depth += 1;
                        }
                        if (after_sec > 0 && after_depth < 2) {
                            after += "" + after_sec + " " + (after_sec != 1 ? _.i(R.string.secs) : _.i(R.string.sec)) + " ";
                            after_depth += 1;
                        }

                        if (after.equals("")) {
                            after = "1 " +  _.i(R.string.sec);
                        }

                        if(type == LinkaActivityType.isUnlocked) {
                            notification.body = _.i(R.string.act_unlocked_after) + " " + after;
                        }
                    }

                }

            } else if (type == LinkaActivityType.isBatteryLow) {

                notification.body =_.i(R.string.notif_battery_low)+ _.i(R.string.battery_low_notification_massage_part1) + "\n" +_.i(R.string.battery_low_notification_massage_part2);

            } else if (type == LinkaActivityType.isBatteryCriticallyLow) {

                notification.body = _.i(R.string.notif_battery_low)+_.i(R.string.notif_below) + " " + activity.batteryPercent + "% "+_.i(R.string.notif_battery_remaining)+" "+_.i(R.string.notif_please_charge_soon)+"";

            } else if (type == LinkaActivityType.isTamperAlert) {

                notification.body = _.i(R.string.act_tamper_alert)+"\n"+_.i(R.string.notif_check_your_bike);;

            } else if (type == LinkaActivityType.isRenamed) {

                notification.body = "Lock renamed from ["+activity.old_lock_name+"] to ["+activity.new_lock_name+"]";

            } else if (type == LinkaActivityType.isBackInRange) {

                notification.body = _.i(R.string.back_in_range_alert)+"\n"+_.i(R.string.your_lock_is) + " " + _.i(R.string.back_in_range_alert);

            } else if (type == LinkaActivityType.isOutOfRange) {

                notification.body = _.i(R.string.out_of_range_alert)+"\n"+_.i(R.string.your_lock_is) + " " + _.i(R.string.out_of_range_alert);

            }else if(type == LinkaActivityType.isAutoUnlockEnabled){
                notification.body = "Auto-unlocking is now enabled";
            }else if (type == LinkaActivityType.isSleep){
                notification.body = _.i(R.string.sleep_notification)+"\n"+ _.i(R.string.sleep_notification_desc);
            }


            if (type != LinkaActivityType.isUnknown
                    // && type != LinkaActivityType.isBackInRange
                    //&& type != LinkaActivityType.isOutOfRange
                    && type != LinkaActivityType.isRenamed
                    && type != LinkaActivityType.isStalled
                    ) {
                notifications.add(notification);
            }
        }
        return notifications;
    }
}


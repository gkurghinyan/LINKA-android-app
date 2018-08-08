package com.linka.lockapp.aos.module.model;

import android.location.Location;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.Settings;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.BuildConfig;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.helpers.NotificationsHelper;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 30/3/16.
 */


@Table(name = "LinkaActivities", id = BaseColumns._ID)
public class LinkaActivity extends Model implements Serializable {

    public static final String LINKA_ACTIVITY_ON_CHANGE = "LINKA_ACTIVITY_ON_CHANGE";

    public static enum LinkaActivityType {

        isUnknown(0),
        isConnected(1),
        isDisconnected(2),
        isRenamed(3),
        isLocked(4),
        isUnlocked(5),
        isBatteryLow(6),
        isBatteryCriticallyLow(7),
        isTamperAlert(8),
        isSystem(9),
        isOutOfRange(10),
        isBackInRange(11),
        isStalled(12),
        isAutoUnlocked(13),
        isAutoUnlockEnabled(14);

        private int _value;

        LinkaActivityType(int Value) {
            this._value = Value;
        }

        public int getValue() {
            return _value;
        }

        public static LinkaActivityType fromInt(int i) {
            for (LinkaActivityType type : LinkaActivityType.values()) {
                if (type.getValue() == i) {
                    return type;
                }
            }
            return null;
        }
    }


    @Column(name = "linka_id")
    public long linka_id = 0;
    @Column(name = "lock_address")
    public String lock_address = "";
    @Column(name = "lock_name")
    public String lock_name = "";

    @Column(name = "batteryPercent")
    public int batteryPercent = 0;
    @Column(name = "lockState")
    public int lockState = 0;


    @Column(name = "linka_activity_status")
    public int linka_activity_status = 0;
    @Column(name = "old_lock_name")
    public String old_lock_name = "";
    @Column(name = "new_lock_name")
    public String new_lock_name = "";

    @Column(name = "latitude")
    public String latitude = "";
    @Column(name = "longitude")
    public String longitude = "";

    @Column(name = "timestamp")
    public String timestamp = "";

    @Column(name = "timestamp_locked")
    public String timestamp_locked = "";

    @Column(name = "alarm")
    public boolean alarm = false;

    @Column(name = "isRead")
    public boolean isRead = false;

    // Telemetry
    public String linka_uuid = "";
    public String platform = "Android " + Build.MANUFACTURER + " " + Build.MODEL;
    public String os_version = "";
    public String fw_version = "";
    public String api_version = "";
    public int pac = 0;
    public long actuations = 0;
    public double temperature = -100;
    public int sleep_lock_sec = 0;
    public int sleep_unlock_sec = 0;


    public Date getDate() {
        if (timestamp != null && !timestamp.equals("")) {
            Date date = new Date(Long.parseLong(timestamp));
            return date;
        }
        return null;
    }

    public Date getTimestampLockedDate() {
        if (timestamp_locked != null && !timestamp_locked.equals("")) {
            Date date = new Date(Long.parseLong(timestamp_locked));
            return date;
        }
        return null;
    }


    public String getFormattedDate() {
        if (timestamp != null && !timestamp.equals("")) {
            Date date = new Date(Long.parseLong(timestamp));
            SimpleDateFormat format = new SimpleDateFormat("MMM dd', ' yyyy', 'h:mm a", Locale.ENGLISH);
            String date_string = format.format(date);
            String time = date_string;
//            time = time.toLowerCase();
            return time;
        }
        return "";
    }


    public LinkaActivity() {
        super();
    }


    public static List<LinkaActivity> getLinkaActivities() {
        List<LinkaActivity> activities = new Select().from(LinkaActivity.class).orderBy("timestamp DESC").execute();
        return activities;
    }

    public static List<LinkaActivity> getLinkaActivitiesByLockAddress(String lock_address) {
        List<LinkaActivity> activities = new Select().from(LinkaActivity.class).where("lock_address = ?", lock_address).orderBy("timestamp DESC").execute();
        return activities;
    }

    public static LinkaActivity getLatestLinkaActivitiesByLockAddress(String lock_address) {
        List<LinkaActivity> activities = getLinkaActivitiesByLockAddress(lock_address);
        if (activities.size() > 0) {
            return activities.get(activities.size() - 1);
        }
        return null;
    }


    public static void saveAndOverwriteActivities(List<LinkaActivity> activities, Linka linka) {
        List<LinkaActivity> activities_reverse = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            activities_reverse.add(0, activities.get(i));
        }

        List<LinkaActivity> old_activities = getLinkaActivitiesByLinka(linka);
        for (int i = old_activities.size() - 1; i >= 0; i--) {
            old_activities.get(i).delete();
        }

        for (int i = 0; i < activities_reverse.size(); i++) {
            activities_reverse.get(i).save();
        }
    }

    public static void updateReadState(final List<Long> ids) {
        LinkaActivity activity;
        for (Long id : ids) {
            activity = getActivityById(id);
            if (activity != null) {
                activity.isRead = true;
                activity.save();
            }
        }
    }

    public static LinkaActivity getActivityById(Long id) {
        return new Select().from(LinkaActivity.class).where("_id = ?", id).executeSingle();
    }

    public static LinkaActivity getActivityByTimestamp(Long time) {
        return new Select().from(LinkaActivity.class).where("timestamp = ?", time).executeSingle();
    }


    public static List<LinkaActivity> getLinkaActivitiesByLinka(Linka linka) {
        List<LinkaActivity> activities = new Select().from(LinkaActivity.class).where("linka_id = ?", linka.getId()).orderBy("timestamp DESC").execute();
        return activities;
    }

    public static boolean saveLinkaActivity(Linka linka, LinkaActivityType linkaActivityType, String old_lock_name, String new_lock_name, Double latitude, Double longitude, boolean create_notification) {
        LinkaActivity activity = new LinkaActivity();
        activity.linka_id = linka.getId();
        activity.lock_address = linka.lock_address;
        activity.lock_name = linka.getName();
        activity.batteryPercent = linka.batteryPercent;
        activity.lockState = linka.lockState;
        activity.old_lock_name = old_lock_name;
        activity.new_lock_name = new_lock_name;
        activity.sleep_lock_sec = linka.settings_locked_sleep;
        activity.sleep_unlock_sec = linka.settings_unlocked_sleep;
        activity.isRead = false;

        // Handle Android Specific Telemetry
        // UUID is Secure.ANDROID_ID - Should exist over the life of the phone unless factory reset occurs
        activity.linka_uuid = Settings.Secure.getString(AppDelegate.getInstance().getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        activity.os_version = Build.VERSION.RELEASE;
        activity.fw_version = linka.fw_version;
        activity.api_version = BuildConfig.VERSION_NAME;

        activity.pac = linka.pac;
        activity.actuations = linka.actuations;
        activity.temperature = linka.temperature;

        if (latitude == 0 && longitude == 0) {
            activity.latitude = "";
            activity.longitude = "";
        } else {
            activity.latitude = latitude + "";
            activity.longitude = longitude + "";
        }
        activity.linka_activity_status = linkaActivityType.getValue();
        Date date = new Date();
        activity.timestamp = Long.toString(date.getTime());
        if (linkaActivityType == LinkaActivityType.isUnlocked) {
            activity.timestamp_locked = linka.timestamp_locked;
        }

        if (linkaActivityType == LinkaActivityType.isTamperAlert) {
            if (linka.settings_tamper_siren) {
                activity.alarm = true;
            }
        }

        if (linkaActivityType == LinkaActivityType.isBatteryLow) {
            activity.alarm = true;
        }

        if (linkaActivityType == LinkaActivityType.isBatteryCriticallyLow) {
            activity.alarm = true;
        }

        if (linkaActivityType == LinkaActivityType.isBackInRange) {
            activity.alarm = true;
        }

        if (linkaActivityType == LinkaActivityType.isOutOfRange) {
            activity.alarm = true;
        }

        if (linkaActivityType == LinkaActivityType.isStalled) {
            activity.alarm = true;
        }

        if (linkaActivityType == LinkaActivityType.isAutoUnlocked) {
            activity.timestamp_locked = linka.timestamp_locked;
        }

        if (linkaActivityType == LinkaActivityType.isAutoUnlockEnabled) {
            activity.alarm = true;
        }

        activity.save();

        if (create_notification) {
            // if linka is not the active one, don't show notification!
            Linka _linka = LinkaNotificationSettings.get_latest_linka();
            if (_linka != null && _linka.getUUIDAddress().equals(linka.getUUIDAddress())) {
                NotificationsHelper.getInstance().CreateLinkaNotificationMessage(activity);
            }
        }

        EventBus.getDefault().post(LINKA_ACTIVITY_ON_CHANGE);


        // sync to network
        if (linkaActivityType == LinkaActivityType.isLocked) {
            LinkaAPIServiceImpl.upsert_lock(AppDelegate.getInstance(), linka, new Callback<LinkaAPIServiceResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {

                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

                }
            });
        } else if (linkaActivityType == LinkaActivityType.isUnlocked) {
            LinkaAPIServiceImpl.upsert_lock(AppDelegate.getInstance(), linka, new Callback<LinkaAPIServiceResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {

                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

                }
            });
        }


        LinkaAPIServiceImpl.add_activity(AppDelegate.getInstance(), linka, activity, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                EventBus.getDefault().post(MainTabBarPageFragment.UPDATE_NOTIFICATIONS);
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });


        return true;
    }


    public static boolean saveLinkaActivity(Linka linka, LinkaActivityType linkaActivityType) {
        double latitude = 0.0;
        double longitude = 0.0;

        Location location = AppDelegate.getInstance().getCurLocation();
        if (linkaActivityType == LinkaActivityType.isLocked) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            Date date = new Date();
            linka.timestamp_locked = Long.toString(date.getTime());
            if (latitude == 0 && longitude == 0) {
                linka.latitude = "";
                linka.longitude = "";
            } else {
                linka.latitude = "" + latitude;
                linka.longitude = "" + longitude;
            }
            linka.save();

        } else if (linkaActivityType == LinkaActivityType.isUnlocked || linkaActivityType == LinkaActivityType.isAutoUnlocked) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            Date date = new Date();
            linka.timestamp_unlocked = Long.toString(date.getTime());
            if (latitude == 0 && longitude == 0) {
                linka.latitude = "";
                linka.longitude = "";
            } else {
                linka.latitude = "" + latitude;
                linka.longitude = "" + longitude;
            }
            linka.save();

        } else {
            linka.save();
        }

        return saveLinkaActivity(linka, linkaActivityType, "", "", latitude, longitude, true);
    }

    public static boolean saveLinkaActivity(Linka linka, LinkaActivityType linkaActivityType, int batteryPercent) {
        double latitude = 0.0;
        double longitude = 0.0;
        linka.batteryPercent = batteryPercent;
        return saveLinkaActivity(linka, linkaActivityType, "", "", latitude, longitude, true);
    }


//    public static boolean saveLinkaActivityRenameLinka(Linka linka, String old_lock_name, String new_lock_name) {
//        double latitude = 0.0;
//        double longitude = 0.0;
//        return saveLinkaActivity(linka, LinkaActivityType.isRenamed, old_lock_name, new_lock_name, latitude, longitude, true);
//    }


    public static boolean removeAllActivitiesForLinka(Linka linka) {
        new Delete().from(LinkaActivity.class).where("linka_id = ?", linka.getId());
        return true;
    }
}



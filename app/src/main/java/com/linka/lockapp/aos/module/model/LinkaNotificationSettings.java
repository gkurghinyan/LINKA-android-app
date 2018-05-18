package com.linka.lockapp.aos.module.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Vanson on 30/3/16.
 */

@Table(name = "LinkaNotificationSettings", id = BaseColumns._ID)
public class LinkaNotificationSettings extends Model implements Serializable {
    @Column(name = "settings_out_of_range_alert")
    public boolean settings_out_of_range_alert = false;
    @Column(name = "settings_back_in_range_alert")
    public boolean settings_back_in_range_alert = false;
    @Column(name = "settings_linka_battery_low_alert")
    public boolean settings_linka_battery_low_alert = true;
    @Column(name = "settings_linka_battery_critically_low_alert")
    public boolean settings_linka_battery_critically_low_alert = true;
    @Column(name = "settings_sleep_notification")
    public boolean settings_sleep_notification= false;

    @Column(name = "status_linka_active_id")
    public long status_linka_active_id = -1;

    public LinkaNotificationSettings() {
        super();
    }

    public static LinkaNotificationSettings getSettings() {
        LinkaNotificationSettings settings = new Select().from(LinkaNotificationSettings.class).executeSingle();
        if (settings != null) {
            return settings;
        }


        long _id = -1;
        if (settings == null) {
            LinkaNotificationSettings newSettings = new LinkaNotificationSettings();
            _id = newSettings.save();
        }


        LinkaNotificationSettings settings_ = new Select().from(LinkaNotificationSettings.class).where("_id = ?", _id).executeSingle();
        if (settings_ != null) {
            return settings_;
        }

        return null;
    }

    public boolean saveSettings() {
        this.save();
        return true;
    }

    public static boolean isEnabled_out_of_range_alert() {
        return getSettings() != null ? getSettings().settings_out_of_range_alert : false;
    }
    public static boolean isEnabled_back_in_range_alert() {
        return getSettings() != null ? getSettings().settings_back_in_range_alert : false;
    }
    public static boolean isEnabled_linka_battery_low_alert() {
        return getSettings() != null ? getSettings().settings_linka_battery_low_alert : false;
    }
    public static boolean isEnabled_linka_battery_critically_low_alert() {
        return getSettings() != null ? getSettings().settings_linka_battery_critically_low_alert : false;
    }

    public static long get_latest_linka_id() {
        return getSettings() != null ? getSettings().status_linka_active_id : -1;
    }

    public static Linka get_latest_linka() {
        long linka_id = get_latest_linka_id();
        if (linka_id != -1) {
            return Linka.getLinkaById(linka_id);
        }
        return null;
    }

    public static boolean save_as_latest_linka(Linka linka) {
        long linka_id = linka.getId();
        LinkaNotificationSettings settings = getSettings();
        settings.status_linka_active_id = linka_id;
        return settings.saveSettings();
    }



    public static Linka refresh_for_latest_linka() {
        if (get_latest_linka() == null) {
            List<Linka> linkas = Linka.getLinkas();


            if (linkas.size() > 0) {
                Linka linka = linkas.get(0);
                //only connect user selected device
//                AppBluetoothService.instance.is_user_selected_device_to_connect = true;
                save_as_latest_linka(linka);
                return linka;
            }
        }
        return null;
    }











    public static void disconnect_all_linka() {
        LocksController.getInstance().getLockController().doDisconnectDevice();
    }


    public static void disconnect_if_not_latest_linka()
    {

    }

}



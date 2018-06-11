package com.linka.lockapp.aos.module.pages.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.rey.material.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 30/3/16.
 */
public class NotificationSettingsPageFragment extends CoreFragment {


    @BindView(R.id.row_out_of_range_alert)
    LinearLayout rowOutOfRangeAlert;
    @BindView(R.id.row_back_in_range_alert)
    LinearLayout rowBackInRangeAlert;
    @BindView(R.id.row_battery_low)
    LinearLayout rowBatteryLow;
    @BindView(R.id.row_battery_critically_low)
    LinearLayout rowBatteryCriticallyLow;


    @BindView(R.id.settings_out_of_range_alert)
    Switch switchOutOfRangeAlert;
    @BindView(R.id.settings_back_in_range_alert)
    Switch switchBackInRangeAlert;
    @BindView(R.id.settings_battery_low)
    Switch switchBatteryLow;
    @BindView(R.id.settings_battery_critically_low)
    Switch switchBatteryCriticallyLow;

    @BindView(R.id.row_sleep_notification)
    LinearLayout rowSleepNotification;

    @BindView(R.id.settings_sleep_notification)
    Switch switchSleepNotification;

    Unbinder unbinder;

    public static NotificationSettingsPageFragment newInstance() {
        Bundle bundle = new Bundle();
        NotificationSettingsPageFragment fragment = new NotificationSettingsPageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    public NotificationSettingsPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification_settings_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            init(savedInstanceState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    LinkaNotificationSettings linkaNotificationSettings;


    void init(Bundle savedInstanceState) {

        linkaNotificationSettings = LinkaNotificationSettings.getSettings();
        refreshDisplay();

    }



    void refreshDisplay() {
        switchOutOfRangeAlert.setChecked(linkaNotificationSettings.settings_out_of_range_alert);
        switchBackInRangeAlert.setChecked(linkaNotificationSettings.settings_back_in_range_alert);
        switchBatteryLow.setChecked(linkaNotificationSettings.settings_linka_battery_low_alert);
        switchBatteryCriticallyLow.setChecked(linkaNotificationSettings.settings_linka_battery_critically_low_alert);
        switchSleepNotification.setChecked(linkaNotificationSettings.settings_sleep_notification);

        switchOutOfRangeAlert.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                linkaNotificationSettings.settings_out_of_range_alert = checked;
                linkaNotificationSettings.saveSettings();
            }
        });

        switchBackInRangeAlert.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                linkaNotificationSettings.settings_back_in_range_alert = checked;
                linkaNotificationSettings.saveSettings();
            }
        });

        switchBatteryLow.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                linkaNotificationSettings.settings_linka_battery_low_alert = checked;
                linkaNotificationSettings.saveSettings();
            }
        });

        switchBatteryCriticallyLow.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                linkaNotificationSettings.settings_linka_battery_critically_low_alert = checked;
                linkaNotificationSettings.saveSettings();
            }
        });

        switchSleepNotification.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                linkaNotificationSettings.settings_sleep_notification = checked;
                linkaNotificationSettings.saveSettings();
            }
        });
    }






    @OnClick(R.id.row_out_of_range_alert)
    void onClick_row_out_of_range_alert() {
        switchOutOfRangeAlert.toggle();
    }

    @OnClick(R.id.row_back_in_range_alert)
    void onClick_row_back_in_range_alert() {
        switchBackInRangeAlert.toggle();
    }

    @OnClick(R.id.row_battery_low)
    void onClick_row_battery_low() {
        switchBatteryLow.toggle();
    }

    @OnClick(R.id.row_battery_critically_low)
    void onClick_row_battery_critically_low() {
        switchBatteryCriticallyLow.toggle();
    }

    @OnClick(R.id.row_sleep_notification)
    void onClick_row_sleep_notification() {
        switchSleepNotification.toggle();
    }


}
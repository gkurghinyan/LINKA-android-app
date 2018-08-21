package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.pages.prelogin.ForgotPasswordPage1;
import com.pixplicity.easyprefs.library.Prefs;
import com.rey.material.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AppSettingsFragment extends CoreFragment {

    @BindView(R.id.user_first_name)
    TextView firstName;

    @BindView(R.id.user_last_name)
    TextView lastName;

    @BindView(R.id.reset_password)
    LinearLayout resetPassword;

    @BindView(R.id.switch_out_of_range)
    Switch outOfRangeSwitch;

    @BindView(R.id.switch_back_in_range)
    Switch backInRangeSwitch;

    @BindView(R.id.switch_battery_low)
    Switch batteryLowSwitch;

    @BindView(R.id.switch_battery_critically_low)
    Switch batteryCriticLowSwitch;

    @BindView(R.id.switch_sleep_notification)
    Switch sleepSwitch;

    private Unbinder unbinder;

    public static AppSettingsFragment newInstance() {
        Bundle args = new Bundle();
        AppSettingsFragment fragment = new AppSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        getAppMainActivity().setTitle(getString(R.string.account_settings));
        init();
        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setTitle("");
        unbinder.unbind();
    }

    private void init() {
        if (LinkaAPIServiceImpl.isLoggedIn()) {
            firstName.setText(Prefs.getString("user-first-name", ""));
            lastName.setText(Prefs.getString("user-last-name", ""));
        }

        outOfRangeSwitch.setChecked(Prefs.getBoolean(Constants.SHOW_OUT_OF_RANGE_NOTIFICATION, false));
        backInRangeSwitch.setChecked(Prefs.getBoolean(Constants.SHOW_BACK_IN_RANGE_NOTIFICATION,false));
        batteryLowSwitch.setChecked(Prefs.getBoolean(Constants.SHOW_BATTERY_LOW_NOTIFICATION,false));
        batteryCriticLowSwitch.setChecked(Prefs.getBoolean(Constants.SHOW_BATTERY_CRITICALLY_LOW_NOTIFICATION,false));
        sleepSwitch.setChecked(Prefs.getBoolean(Constants.SHOW_SLEEP_NOTIFICATION,false));
    }

    private void setListeners(){
        Switch.OnCheckedChangeListener onCheckedChangeListener = new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                switch (view.getId()){
                    case R.id.switch_out_of_range:
                        Prefs.edit().putBoolean(Constants.SHOW_OUT_OF_RANGE_NOTIFICATION,checked).apply();
                        break;
                    case R.id.switch_back_in_range:
                        Prefs.edit().putBoolean(Constants.SHOW_BACK_IN_RANGE_NOTIFICATION,checked).apply();
                        break;
                    case R.id.switch_battery_low:
                        Prefs.edit().putBoolean(Constants.SHOW_BATTERY_LOW_NOTIFICATION,checked).apply();
                        break;
                    case R.id.switch_battery_critically_low:
                        Prefs.edit().putBoolean(Constants.SHOW_BATTERY_CRITICALLY_LOW_NOTIFICATION,checked).apply();
                        break;
                    case R.id.switch_sleep_notification:
                        Prefs.edit().putBoolean(Constants.SHOW_SLEEP_NOTIFICATION,checked).apply();
                        break;
                }
            }
        };
        outOfRangeSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        backInRangeSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        batteryLowSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        batteryCriticLowSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        sleepSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @OnClick(R.id.reset_password)
    void onResetPasswordClicked() {
        getAppMainActivity().pushFragment(ForgotPasswordPage1.newInstance());
    }

}

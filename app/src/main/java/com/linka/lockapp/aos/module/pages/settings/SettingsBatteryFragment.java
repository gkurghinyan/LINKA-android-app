package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.rey.material.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class SettingsBatteryFragment extends CoreFragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.enable_switch)
    Switch enableAutoSleep;

    @BindView(R.id.set_sleep_in_linear)
    LinearLayout setSleepIn;

    private Unbinder unbinder;
    private Linka linka;

    public static SettingsBatteryFragment newInstance(Linka linka) {
        Bundle args = new Bundle();
        args.putSerializable(LINKA_ARGUMENT, linka);
        SettingsBatteryFragment fragment = new SettingsBatteryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_battery, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        linka = ((Linka) getArguments().getSerializable(LINKA_ARGUMENT));
        getAppMainActivity().setBackIconVisible(true);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setBackIconVisible(false);
        unbinder.unbind();
    }

    private void init() {
        if (linka.isAutoSleepEnabled) {
            enableAutoSleep.setChecked(true);
            setSleepIn.setVisibility(View.VISIBLE);
        }else {
            enableAutoSleep.setChecked(false);
            setSleepIn.setVisibility(View.GONE);
        }
        enableAutoSleep.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                LockController lockController = LocksController.getInstance().getLockController();
                if (checked) {
                    setSleepIn.setVisibility(View.VISIBLE);
                    linka.isAutoSleepEnabled = true;
                    linka.saveSettings();

                    lockController.doAction_SetLockSleep(linka.settings_locked_sleep);
                    if(linka.settings_unlocked_sleep == 960){
                        linka.settings_unlocked_sleep = 1800;
                        linka.save();
                    }
                    lockController.doAction_SetUnlockSleep(linka.settings_unlocked_sleep);
                } else {
                    setSleepIn.setVisibility(View.GONE);
                    linka.isAutoSleepEnabled = false;
                    linka.saveSettings();

                    lockController.doAction_SetLockSleep(AppDelegate.default_lock_sleep_time);
                    lockController.doAction_SetUnlockSleep(AppDelegate.default_unlock_sleep_time);
                }
            }
        });
    }

    @OnClick(R.id.set_sleep_in_linear)
    void onSetSleepInLinearClicked() {
        getAppMainActivity().pushFragment(SettingsSleepSettingsFragment.newInstance((linka)));
    }
}

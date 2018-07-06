package com.linka.lockapp.aos.module.pages.settings;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.SleepNotificationService;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class SettingsSleepSettingsFragment extends CoreFragment {

    @BindView(R.id.unlock_hour_picker)
    NumberPicker unlockHourPicker;

    @BindView(R.id.unlock_minute_picker)
    NumberPicker unlockMinutePicker;

    @BindView(R.id.lock_hour_picker)
    NumberPicker lockHourPicker;

    @BindView(R.id.lock_minute_picker)
    NumberPicker lockMinutePicker;

    private Handler pickerHandler = null;
    private Runnable pickerRunnable = new Runnable() {
        @Override
        public void run() {
            if(unlockHourPicker != null) {
                if (unlockHourPicker.getValue() == 0 && unlockMinutePicker.getValue() == 0) {
                    unlockMinutePicker.setValue(1);
                }
                if (lockHourPicker.getValue() == 0 && lockMinutePicker.getValue() == 0) {
                    lockMinutePicker.setValue(1);
                }
            }
            pickerHandler = null;
        }
    };

    private int lockTime;
    private int unlockTime;

    private Unbinder unbinder;

    private String[] minuteValues = new String[12];
    private HashMap<String, Integer> minutes;


    public static SettingsSleepSettingsFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SettingsSleepSettingsFragment fragment = new SettingsSleepSettingsFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public SettingsSleepSettingsFragment() {
    }

    Linka linka;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_sleep_settings, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            if (bundle.get("linka") != null) {
                linka = (Linka) bundle.getSerializable("linka");
            }
            getAppMainActivity().setBackIconVisible(true);
            init();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(unlockTime == 0 || lockTime == 0){
            pickerHandler = new Handler();
            pickerHandler.post(pickerRunnable);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(pickerHandler != null){
            pickerHandler.removeCallbacks(pickerRunnable);
            pickerHandler = null;
        }
        if(unlockTime != 0 && lockTime != 0) {
            save(unlockTime, lockTime);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setBackIconVisible(false);
        unbinder.unbind();
    }

    void init() {
        initPickers();

//        int batteryPercent = linka.batteryPercent;

//        setEstimatedBatteryRemaining(linka.settings_unlocked_sleep, linka.settings_locked_sleep);
//        battery_percent.setText("(" + batteryPercent + "%)");
//
//        if (batteryPercent < AppDelegate.battery_mid && batteryPercent >= AppDelegate.battery_low_below) {
//            battery_icon.setImageResource(R.drawable.icon_activity_battery_mid_x);
//        } else if (batteryPercent < AppDelegate.battery_critically_low_below) {
//            battery_icon.setImageResource(R.drawable.icon_activity_battery_low_critical_x);
//        } else if (batteryPercent < AppDelegate.battery_low_below && batteryPercent >= AppDelegate.battery_critically_low_below) {
//            battery_icon.setImageResource(R.drawable.icon_activity_battery_low_x);
//        } else {
//            battery_icon.setImageResource(R.drawable.icon_activity_battery_high_x);
//        }

        //lock
        lockTime = linka.settings_locked_sleep;
//        lockText.setText(String.valueOf(lockTime));
        if (getHourFromSeconds(lockTime) != 0) {
            lockHourPicker.setValue(getHourFromSeconds(lockTime));
        } else {
            lockHourPicker.setValue(0);
        }
        if (getMinutesFromSeconds(lockTime) != 0) {
            lockMinutePicker.setValue(minutes.get(String.valueOf(getMinutesFromSeconds(lockTime))));
        } else {
            lockMinutePicker.setValue(0);
        }

        //unlock
        unlockTime = linka.settings_unlocked_sleep;
//        unlockText.setText(String.valueOf(unlockTime));
        if (getHourFromSeconds(unlockTime) > 0) {
            unlockHourPicker.setValue(getHourFromSeconds(unlockTime));
        } else {
            unlockHourPicker.setValue(0);
        }
        if (getMinutesFromSeconds(unlockTime) > 0) {
            unlockMinutePicker.setValue(minutes.get(String.valueOf(getMinutesFromSeconds(unlockTime))));
        } else {
            if (unlockHourPicker.getValue() == 0) {
                unlockMinutePicker.setValue(1);
            } else {
                unlockMinutePicker.setValue(0);
            }
        }

//        updateTimeTexts();
    }

    private void initPickers() {
        minutes = new HashMap<>();
        for (int i = 0; i < minuteValues.length; i++) {
            String minute = Integer.toString(i * 5);
            minuteValues[i] = minute;
            minutes.put(minute, i);
        }

        NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                switch (picker.getId()) {
                    case R.id.unlock_hour_picker:
                        unlockTime = unlockTime + (newVal - oldVal) * 3600;
                        break;
                    case R.id.unlock_minute_picker:
                        int newUnlockVal = Integer.parseInt(minuteValues[newVal]);
                        int oldUnlockVal = Integer.parseInt(minuteValues[oldVal]);
                        unlockTime = unlockTime + (newUnlockVal - oldUnlockVal) * 60;
                        break;
                    case R.id.lock_hour_picker:
                        lockTime = lockTime + (newVal - oldVal) * 3600;
                        break;
                    case R.id.lock_minute_picker:
                        int newLockVal1 = Integer.parseInt(minuteValues[newVal]);
                        int oldLockVal1 = Integer.parseInt(minuteValues[oldVal]);
                        lockTime = lockTime + (newLockVal1 - oldLockVal1) * 60;
                        break;
                }
//                setEstimatedBatteryRemaining(unlockTime, lockTime);
//                updateTimeTexts();
            }
        };

        NumberPicker.OnScrollListener onScrollListener = new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if(pickerHandler != null){
                        pickerHandler.removeCallbacks(pickerRunnable);
                    }
                    pickerHandler = new Handler();
                    pickerHandler.postDelayed(pickerRunnable,300);
                }
            }
        };

        unlockMinutePicker.setOnScrollListener(onScrollListener);

        unlockHourPicker.setMinValue(0);
        unlockHourPicker.setMaxValue(99);
        setDividerColor(unlockHourPicker, getResources().getColor(R.color.linka_blue));
        unlockHourPicker.setOnValueChangedListener(onValueChangeListener);

        unlockMinutePicker.setMinValue(0);
        unlockMinutePicker.setMaxValue(11);
        unlockMinutePicker.setDisplayedValues(minuteValues);
        setDividerColor(unlockMinutePicker, getResources().getColor(R.color.linka_blue));
        unlockMinutePicker.setOnValueChangedListener(onValueChangeListener);

        lockHourPicker.setMinValue(0);
        lockHourPicker.setMaxValue(99);
        setDividerColor(lockHourPicker, getResources().getColor(R.color.linka_blue));
        lockHourPicker.setOnValueChangedListener(onValueChangeListener);

        lockMinutePicker.setMinValue(0);
        lockMinutePicker.setMaxValue(11);
        lockMinutePicker.setDisplayedValues(minuteValues);
        setDividerColor(lockMinutePicker, getResources().getColor(R.color.linka_blue));
        lockMinutePicker.setOnValueChangedListener(onValueChangeListener);
    }

    private int getHourFromSeconds(int seconds) {
        return seconds / 3600;
    }

    private int getMinutesFromSeconds(int seconds) {
        return (seconds - getHourFromSeconds(seconds) * 3600) / 60;
    }

//    private void updateTimeTexts() {
//        String lockHour;
//        if (lockHourPicker.getValue() != 0) {
//            lockHour = String.valueOf(getHourFromSeconds(lockTime) + " hours ");
//        } else {
//            lockHour = "";
//        }
//        String lockMinute;
//        if (lockMinutePicker.getValue() != 0) {
//            lockMinute = String.valueOf(getMinutesFromSeconds(lockTime)) + " mins";
//        } else {
//            lockMinute = "";
//        }
////        lockText.setText(lockHour + lockMinute);
//
//        String unlockHour;
//        if (unlockHourPicker.getValue() != 0) {
//            unlockHour = String.valueOf(getHourFromSeconds(unlockTime) + " hours ");
//        } else {
//            unlockHour = "";
//        }
//        String unlockMinute;
//        if (unlockMinutePicker.getValue() != 0) {
//            unlockMinute = String.valueOf(getMinutesFromSeconds(unlockTime) + " mins");
//        } else {
//            unlockMinute = "";
//        }
////        unlockText.setText(unlockHour + unlockMinute);
//    }


    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

//    private void setEstimatedBatteryRemaining(int unlock_time, int lock_time) {
//        battery_estimated_days_remaining.setText(linka.getEstimatedBatteryRemaining(unlock_time, lock_time));
//    }

//    @OnClick(R.id.save)
//    void onSave() {
//        save(unlockTime, lockTime);
//    }
//
//    @OnClick(R.id.reset_to_default)
//    void onDefault() {
//        save(AppDelegate.default_unlock_sleep_time, AppDelegate.default_lock_sleep_time);
//        unlockHourPicker.setValue(AppDelegate.default_unlock_sleep_time / 3600);
//        unlockMinutePicker.setValue(AppDelegate.default_unlock_sleep_time / 60);
//        lockHourPicker.setValue(AppDelegate.default_lock_sleep_time / 3600);
//        lockMinutePicker.setValue(AppDelegate.default_lock_sleep_time / 60);
////        setEstimatedBatteryRemaining(unlockTime, lockTime);
////        updateTimeTexts();
//    }

    private void save(int unlock_time, int lock_time) {
        boolean isAllCompleted = true;

        //Set Locked time first in case LINKA goes to sleep
        // set lock settings
        LockController lockController = LocksController.getInstance().getLockController();
        lockController.doAction_SetLockSleep(lock_time);
        linka.settings_locked_sleep = lock_time;

        // set unlock settings
        lockController.doAction_SetUnlockSleep(unlock_time);
        linka.settings_unlocked_sleep = unlock_time;

        //save to local db
        linka.saveSettings();

        // reset the timer
        SleepNotificationService.getInstance().restartTimer();

//        getAppMainActivity().popFragment();
//        new AlertDialog.Builder(getAppMainActivity())
//                .setTitle(_.i(R.string.success))
//                .setMessage(_.i(R.string.sleeping_set))
//                .setNegativeButton(_.i(R.string.ok), null)
//                .show();
//
//        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private void displayErrorMessage() {
        new AlertDialog.Builder(getAppMainActivity())
                .setTitle(_.i(R.string.fail_to_communicate))
                .setMessage(_.i(R.string.check_connection))
                .setNegativeButton(_.i(R.string.ok), null)
                .show();
    }
}

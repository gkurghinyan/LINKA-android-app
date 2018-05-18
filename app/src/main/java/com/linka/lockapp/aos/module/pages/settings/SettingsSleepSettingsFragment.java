package com.linka.lockapp.aos.module.pages.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.SleepNotificationService;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LinkaButton;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class SettingsSleepSettingsFragment extends CoreFragment {

    @BindView(R.id.save)
    LinkaButton save;

    @BindView(R.id.lock_sleep_timer)
    SeekBar lock_sleep_timer;

    @BindView(R.id.unlock_sleep_timer)
    SeekBar unlock_sleep_timer;

    @BindView(R.id.battery_estimated_days_remaining)
    TextView battery_estimated_days_remaining;

    @BindView(R.id.sleep_battery_percent)
    TextView battery_percent;

    @BindView(R.id.sleep_battery_icon)
    ImageView battery_icon;

    private Unbinder unbinder;

    private static ArrayList<SleepOption> mOptions = new ArrayList<SleepOption>() {{
        add(SleepOption.SLEEPING_OPTION_1);
//        add(SleepOption.SLEEPING_OPTION_2);
//        add(SleepOption.SLEEPING_OPTION_3);
//        add(SleepOption.SLEEPING_OPTION_4);
        add(SleepOption.SLEEPING_OPTION_5);
//        add(SleepOption.SLEEPING_OPTION_6);
//        add(SleepOption.SLEEPING_OPTION_7);
//        add(SleepOption.SLEEPING_OPTION_8);
//        add(SleepOption.SLEEPING_OPTION_9);
        add(SleepOption.SLEEPING_OPTION_10);
//        add(SleepOption.SLEEPING_OPTION_11);
        add(SleepOption.SLEEPING_OPTION_12);
//        add(SleepOption.SLEEPING_OPTION_13);
        add(SleepOption.SLEEPING_OPTION_14);
//        add(SleepOption.SLEEPING_OPTION_15);
        add(SleepOption.SLEEPING_OPTION_16);
//        add(SleepOption.SLEEPING_OPTION_17);
        add(SleepOption.SLEEPING_OPTION_18);
//        add(SleepOption.SLEEPING_OPTION_19);
        add(SleepOption.SLEEPING_OPTION_20);
        add(SleepOption.SLEEPING_OPTION_21);
        add(SleepOption.SLEEPING_OPTION_22);
        add(SleepOption.SLEEPING_OPTION_23);
        add(SleepOption.SLEEPING_OPTION_24);
        add(SleepOption.SLEEPING_OPTION_25);
        add(SleepOption.SLEEPING_OPTION_26);
        add(SleepOption.SLEEPING_OPTION_27);
        add(SleepOption.SLEEPING_OPTION_28);
        add(SleepOption.SLEEPING_OPTION_29);
    }};


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
        ButterKnife.bind(this, rootView);

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
            init(view);
        }
    }

    void init(final View view) {

        int batteryPercent = linka.batteryPercent;

        setEstimatedBatteryRemaining(linka.settings_unlocked_sleep, linka.settings_locked_sleep);
        battery_percent.setText("("+batteryPercent+"%)");

        if (batteryPercent < AppDelegate.battery_mid && batteryPercent >= AppDelegate.battery_low_below){
            battery_icon.setImageResource(R.drawable.icon_activity_battery_mid_x);
        } else if (batteryPercent < AppDelegate.battery_critically_low_below) {
            battery_icon.setImageResource(R.drawable.icon_activity_battery_low_critical_x);
        } else if (batteryPercent < AppDelegate.battery_low_below && batteryPercent >= AppDelegate.battery_critically_low_below) {
            battery_icon.setImageResource(R.drawable.icon_activity_battery_low_x);
        } else {
            battery_icon.setImageResource(R.drawable.icon_activity_battery_high_x);
        }

        SleepOption lockedSleepOption = SleepOption.lookUpLockedValue(linka.settings_locked_sleep);
        SleepOption unlockedSleepOption = SleepOption.lookUpUnlockedValue(linka.settings_unlocked_sleep);

        SeekBar.OnSeekBarChangeListener sleeptimer = new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int timerTextId = R.id.unlock_sleep_timer_text;
                if (seekBar.getId() == R.id.lock_sleep_timer){
                    timerTextId = R.id.lock_sleep_timer_text;
                }
                if (seekBar.getId() == R.id.unlock_sleep_timer){
                    timerTextId = R.id.unlock_sleep_timer_text;
                }
                TextView timerText = (TextView) view.findViewById(timerTextId);
                timerText.setText(mOptions.get(i).sleepStringResource);
                setEstimatedBatteryRemaining(mOptions.get(unlock_sleep_timer.getProgress()).sleepTime, mOptions.get(lock_sleep_timer.getProgress()).sleepTime);
                Log.i("timer progress", String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        //lock
        TextView timerText = (TextView) view.findViewById(R.id.lock_sleep_timer_text);
        if(lockedSleepOption == SleepOption.SLEEPING_OPTION_LOCK_DEFAULT){
            timerText.setText(SleepOption.SLEEPING_OPTION_LOCK_DEFAULT.sleepStringResource);
        }else{
            timerText.setText(lockedSleepOption.sleepStringResource);
            lock_sleep_timer.setProgress(mOptions.indexOf(lockedSleepOption));
        }
        //unlock
        timerText = (TextView) view.findViewById(R.id.unlock_sleep_timer_text);
        if(unlockedSleepOption == SleepOption.SLEEPING_OPTION_UNLOCK_DEFAULT){
            timerText.setText(SleepOption.SLEEPING_OPTION_UNLOCK_DEFAULT.sleepStringResource);
        }else{
            timerText.setText(unlockedSleepOption.sleepStringResource);
            unlock_sleep_timer.setProgress(mOptions.indexOf(unlockedSleepOption));
        }

        unlock_sleep_timer.setMax(mOptions.size() - 1);
        lock_sleep_timer.setMax(mOptions.size() - 1);
        unlock_sleep_timer.setOnSeekBarChangeListener(sleeptimer);
        lock_sleep_timer.setOnSeekBarChangeListener(sleeptimer);

    }

    private enum SleepOption {



        SLEEPING_OPTION_1(1*60,"1 min", "1 "+_.i(R.string.min)),
//        SLEEPING_OPTION_2(2*60,"2 mins", "2 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_3(3*60,"3 mins", "3 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_4(4*60,"4 mins", "4 "+_.i(R.string.mins)),
        SLEEPING_OPTION_5(5*60,"5 mins", "5 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_6(6*60,"6 mins", "6 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_7(7*60,"7 mins", "7 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_8(8*60,"8 mins", "8 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_9(9*60,"9 mins", "9 "+_.i(R.string.mins)),
        SLEEPING_OPTION_10(10*60,"10 mins", "10 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_11(15*60,"15 mins", "15 "+_.i(R.string.mins)),
        SLEEPING_OPTION_12(20*60,"20 mins", "20 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_13(25*60,"25 mins", "25 "+_.i(R.string.mins)),
        SLEEPING_OPTION_14(30*60,"30 mins", "30 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_15(35*60,"35 mins", "35 "+_.i(R.string.mins)),
        SLEEPING_OPTION_16(40*60,"40 mins", "40 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_17(45*60,"45 mins", "45 "+_.i(R.string.mins)),
        SLEEPING_OPTION_18(50*60,"50 mins", "50 "+_.i(R.string.mins)),
//        SLEEPING_OPTION_19(55*60,"55 mins", "55 "+_.i(R.string.mins)),
        SLEEPING_OPTION_20(1*60*60,"1 hour", "1 "+_.i(R.string.hr)),
        SLEEPING_OPTION_21(2*60*60,"2 hours", "2 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_22(3*60*60,"3 hours", "3 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_23(4*60*60,"4 hours", "4 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_24(5*60*60,"5 hours", "5 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_25(6*60*60,"6 hours", "6 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_26(7*60*60,"7 hours", "7 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_27(8*60*60,"8 hours", "8 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_28(9*60*60,"9 hours", "9 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_29(10*60*60,"10 hours", "10 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_LOCK_DEFAULT(AppDelegate.default_lock_sleep_time, "3 hrs", "3 "+_.i(R.string.hrs)),
        SLEEPING_OPTION_UNLOCK_DEFAULT(AppDelegate.default_unlock_sleep_time, "30 mins", "30 "+_.i(R.string.mins));


        private int sleepTime;
        private String optionName;
        private String sleepStringResource;


        SleepOption(int sleepTime, String optionName, String sleepStringResource) {
            this.sleepTime = sleepTime;
            this.optionName = optionName;
            this.sleepStringResource = sleepStringResource;
        }

        public static SleepOption lookUpLockedValue(int sec){
            for(SleepOption sleepOption : mOptions){
                if(sleepOption.sleepTime == sec){
                    return sleepOption;
                }
            }
            return SleepOption.SLEEPING_OPTION_LOCK_DEFAULT;
        }

        public static SleepOption lookUpUnlockedValue(int sec){
            for(SleepOption sleepOption : mOptions){
                if(sleepOption.sleepTime == sec){
                    return sleepOption;
                }
            }
            return SleepOption.SLEEPING_OPTION_UNLOCK_DEFAULT;
        }
    }

    private void setEstimatedBatteryRemaining(int unlock_time, int lock_time){
        battery_estimated_days_remaining.setText(linka.getEstimatedBatteryRemaining(unlock_time, lock_time));
    }

    @OnClick(R.id.save)
    void onSave() {
        int unlock_time = mOptions.get(unlock_sleep_timer.getProgress()).sleepTime;
        int lock_time = mOptions.get(lock_sleep_timer.getProgress()).sleepTime;

        save(unlock_time, lock_time);
    }

    @OnClick(R.id.reset_to_default)
    void onDefault() {
        //save(AppDelegate.default_unlock_sleep_time, AppDelegate.default_lock_sleep_time);

        //TextView timerText = (TextView) view.findViewById(R.id.lock_sleep_timer_text);
        //timerText.setText(SleepOption.SLEEPING_OPTION_LOCK_DEFAULT.sleepStringResource);
        lock_sleep_timer.setProgress(9);
        unlock_sleep_timer.setProgress(4);

    }

    private void save(int unlock_time, int lock_time){
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

        getAppMainActivity().popFragment();
        new AlertDialog.Builder(getAppMainActivity())
                .setTitle(_.i(R.string.success))
                .setMessage(_.i(R.string.sleeping_set))
                .setNegativeButton(_.i(R.string.ok), null)
                .show();

        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private void displayErrorMessage(){
        new AlertDialog.Builder(getAppMainActivity())
                .setTitle(_.i(R.string.fail_to_communicate))
                .setMessage(_.i(R.string.check_connection))
                .setNegativeButton(_.i(R.string.ok), null)
                .show();
    }
}

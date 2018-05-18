package com.linka.lockapp.aos.module.pages.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SettingsTamperSensitivityFragment extends CoreFragment {

    @BindView(R.id.tamper_slider)
    SeekBar tamper_slider;

    private TamperSensitivity mTamperSensitivity;


    public static SettingsTamperSensitivityFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SettingsTamperSensitivityFragment fragment = new SettingsTamperSensitivityFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public SettingsTamperSensitivityFragment() {
    }

    Linka linka;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_tamper_sensitivity, container, false);
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
            init();
        }
    }

    void init() {

        TamperSensitivity linkSavedMode = TamperSensitivity.lookUpValue(linka.settings_alarm_delay,
                linka.settings_alarm_time, linka.settings_bump_threshold, linka.settings_jostle_ms,
                linka.settings_roll_alrm_deg, linka.settings_pitch_alrm_deg);

        if(linkSavedMode.equals(TamperSensitivity.QUIET_GARAGE)){
            tamper_slider.setProgress(2);
            return;
        }

        if(linkSavedMode.equals(TamperSensitivity.SUBURBAN_NEIGHBORHOOD) || linkSavedMode.equals(TamperSensitivity.DEFAULT)){
            tamper_slider.setProgress(1);
            return;
        }

        if(linkSavedMode.equals(TamperSensitivity.METROPOLIAN_HIGH_TRAFFIC)){
            tamper_slider.setProgress(0);
            return;
        }

    }

    @OnClick(R.id.save)
    void onSave() {
        save(tamper_slider.getProgress());
    }

    @OnClick(R.id.reset_to_default)
    void onDefault() {
        tamper_slider.setProgress(1);
    }

    private void save(int slider_position) {
        if( slider_position == 2){
            mTamperSensitivity = TamperSensitivity.QUIET_GARAGE;
        }else if( slider_position == 1){
            mTamperSensitivity = TamperSensitivity.SUBURBAN_NEIGHBORHOOD;
        }else if( slider_position == 0){
            mTamperSensitivity = TamperSensitivity.METROPOLIAN_HIGH_TRAFFIC;
        }
        // set lock settings
        LockController lockController = LocksController.getInstance().getLockController();
        lockController.doAction_SetAlarmDelay(mTamperSensitivity.alarmDelay);
        //set alarm time
        lockController.doAction_SetAlarmTime(mTamperSensitivity.alarmTime);
        //set Bump Threshold
        lockController.doAction_SetBumpThreshold(mTamperSensitivity.bumpThreshold);
        //set Pulse Tap
        //lockController.doAction_SetPulseTap(mTamperSensitivity.pulseTap);
        //set Jostle
        lockController.doAction_SetJostle(mTamperSensitivity.jostleMs);
        //set roll
        lockController.doAction_SetRoll(mTamperSensitivity.rollAlrmDeg);
        //set tilt
        lockController.doAction_SetTilt(mTamperSensitivity.pitchAlrmDeg);
        //set accelDatarate
        //lockController.doAction_SetAccelDataRate(mTamperSensitivity.accelDatarate);
        //save to local db
        linka.settings_alarm_delay = mTamperSensitivity.alarmDelay;
        linka.settings_alarm_time = mTamperSensitivity.alarmTime;
        linka.settings_bump_threshold = mTamperSensitivity.bumpThreshold;
        //linka.settings_pulse_tap = mTamperSensitivity.pulseTap;
        linka.settings_jostle_ms = mTamperSensitivity.jostleMs;
        linka.settings_roll_alrm_deg = mTamperSensitivity.rollAlrmDeg;
        linka.settings_pitch_alrm_deg = mTamperSensitivity.pitchAlrmDeg;
        //linka.settings_accel_datarate = mTamperSensitivity.accelDatarate;
        linka.saveSettings();

        getAppMainActivity().popFragment();
        new AlertDialog.Builder(getAppMainActivity())
                .setTitle(_.i(R.string.success))
                .setMessage(_.i(R.string.tamper_sensitivity_set))
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



    private enum TamperSensitivity {
        QUIET_GARAGE(5,4,4,5,33,7,7,2),
        SUBURBAN_NEIGHBORHOOD(6,5,5,5,34,9,9,2),
        METROPOLIAN_HIGH_TRAFFIC(6,5,6,5,35,11,11,2),
        DEFAULT(6,5,5,5,35,9,9,2);

        private int alarmDelay;
        private int alarmTime;
        private int bumpThreshold;
        //private int pulseTap;
        private int jostleMs;
        private int rollAlrmDeg;
        private int pitchAlrmDeg;
        //private int accelDatarate;

        private TamperSensitivity(int alarmDelay,int alarmTime,int bumpThreshold,int pulseTap,
                                    int jostleMs,int rollAlrmDeg,int pitchAlrmDeg,int accelDatarate) {
            this.alarmDelay = alarmDelay;
            this.alarmTime = alarmTime;
            this.bumpThreshold = bumpThreshold;
            //this.pulseTap = pulseTap;
            this.jostleMs = jostleMs;
            this.rollAlrmDeg = rollAlrmDeg;
            this.pitchAlrmDeg = pitchAlrmDeg;
            //this.accelDatarate = accelDatarate;
        }

        public static TamperSensitivity lookUpValue(int alarmDelay,int alarmTime,int bumpThreshold,
                                                    int jostleMs,int rollAlrmDeg,int pitchAlrmDeg){
            if(compareValue(TamperSensitivity.METROPOLIAN_HIGH_TRAFFIC, alarmDelay,alarmTime,bumpThreshold,
                    jostleMs,rollAlrmDeg, pitchAlrmDeg)){
                return    TamperSensitivity.METROPOLIAN_HIGH_TRAFFIC;
            }
            if(compareValue(TamperSensitivity.QUIET_GARAGE, alarmDelay,alarmTime,bumpThreshold,
                    jostleMs,rollAlrmDeg, pitchAlrmDeg)){
                return    TamperSensitivity.QUIET_GARAGE;
            }
            if(compareValue(TamperSensitivity.SUBURBAN_NEIGHBORHOOD, alarmDelay,alarmTime,bumpThreshold,
                    jostleMs,rollAlrmDeg, pitchAlrmDeg)){
                return    TamperSensitivity.SUBURBAN_NEIGHBORHOOD;
            }
            return TamperSensitivity.DEFAULT;
        }


        private static boolean compareValue(TamperSensitivity tamperSensitivity, int alarmDelay,int alarmTime,int bumpThreshold,
                                            int jostleMs,int rollAlrmDeg,int pitchAlrmDeg) {

            if (//alarmDelay == tamperSensitivity.alarmDelay &&
                    //alarmTime == tamperSensitivity.alarmTime &&
                    bumpThreshold == tamperSensitivity.bumpThreshold){
                    //pulseTap == tamperSensitivity.pulseTap &&
                    //jostleMs == tamperSensitivity.jostleMs &&
                    //rollAlrmDeg == tamperSensitivity.rollAlrmDeg &&
                    //pitchAlrmDeg == tamperSensitivity.pitchAlrmDeg){
                    //accelDatarate == tamperSensitivity.accelDatarate{
                return true;
            }
            return false;
        }
    }


}

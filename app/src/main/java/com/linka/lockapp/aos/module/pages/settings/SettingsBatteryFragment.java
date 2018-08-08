package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class SettingsBatteryFragment extends CoreFragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.high_performance_image)
    ImageView highImage;

    @BindView(R.id.normal_performance_image)
    ImageView normalImage;

    @BindView(R.id.low_performance_image)
    ImageView lowImage;

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
        setPerformanceImage();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setBackIconVisible(false);
        unbinder.unbind();
    }

    @OnClick(R.id.low_performance)
    void onClickLowPerformance(){
        linka.settingsSleepPerformance = Linka.LOW_PERFORMANCE;
        linka.save();
        LockController lockController = LocksController.getInstance().getLockController();
        lockController.doAction_SetLockSleep(Linka.LOW_PERFORMANCE);
        lockController.doAction_SetUnlockSleep(Linka.LOW_PERFORMANCE);
        setPerformanceImage();
    }

    @OnClick(R.id.normal_performance)
    void onClickNormalPerformance(){
        linka.settingsSleepPerformance = Linka.NORMAL_PERFORMANCE;
        linka.save();
        LockController lockController = LocksController.getInstance().getLockController();
        lockController.doAction_SetLockSleep(Linka.NORMAL_PERFORMANCE);
        lockController.doAction_SetUnlockSleep(Linka.NORMAL_PERFORMANCE);
        setPerformanceImage();
    }

    @OnClick(R.id.high_performance)
    void onClickHighPerformance(){
        linka.settingsSleepPerformance = Linka.HIGH_PERFORMANCE;
        linka.save();
        LockController lockController = LocksController.getInstance().getLockController();
        lockController.doAction_SetLockSleep(Linka.HIGH_PERFORMANCE);
        lockController.doAction_SetUnlockSleep(Linka.HIGH_PERFORMANCE);
        setPerformanceImage();
    }

    private void setPerformanceImage(){
        if(linka.settingsSleepPerformance == 1800){
            linka.settingsSleepPerformance = Linka.NORMAL_PERFORMANCE;
            linka.save();
        }

        lowImage.setVisibility(View.GONE);
        normalImage.setVisibility(View.GONE);
        highImage.setVisibility(View.GONE);

        switch (linka.settingsSleepPerformance) {
            case Linka.LOW_PERFORMANCE:
                lowImage.setVisibility(View.VISIBLE);
                break;
            case Linka.NORMAL_PERFORMANCE:
                normalImage.setVisibility(View.VISIBLE);
                break;
            case Linka.HIGH_PERFORMANCE:
                highImage.setVisibility(View.VISIBLE);
                break;
        }
    }
}

package com.linka.lockapp.aos.module.pages.update;


import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.other.Utils;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SecondUpdateFragment extends Fragment {

    @BindView(R.id.root)
    ConstraintLayout root;

    private Unbinder unbinder;

    private FirmwareUpdateActivityCallback activityCallback;

    public static SecondUpdateFragment newInstance() {

        Bundle args = new Bundle();
        SecondUpdateFragment fragment = new SecondUpdateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second_update, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        activityCallback = null;
    }

    private void init() {
        if (getActivity() != null && getActivity() instanceof FirmwareUpdateActivityCallback) {
            activityCallback = ((FirmwareUpdateActivityCallback) getActivity());
        }
        activityCallback.setBackButtonVisibility(View.VISIBLE);
        activityCallback.changeTitle("Preparing Your Device");

        if(((FirmwareUpdateActivity) getActivity()).blod_firmware_mode){
            ((FirmwareUpdateActivity) getActivity()).blod_firmware_mode = false;
            //If firmware recovery mode, immediately skip to next step
            activityCallback.changeCurrentPage(3);
        } else if (!isLockLocked()) {
            setDfuMode();
        }
    }

    @OnClick(R.id.lets_go_button)
    void onClickLetsGo() {
        refreshBluetooth();
    }

    @OnClick(R.id.not_see_purple_button)
    void onClickNotSeeDevice() {
        getFragmentManager().popBackStack();
    }

    private void refreshBluetooth(){
        Utils.showLoading(getActivity(),root);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
                if (!isAdded()) return;
                Utils.cancelLoading();
                if (!activityCallback.getDfuManager().isUploading) {
                    activityCallback.changeCurrentPage(3);
                }
            }
        },3000);
    }

    private boolean isLockLocked() {
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
        if(targetLinka != null && targetLinka.isLocked){
            new android.app.AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(R.string.unlock_to_continue)
                    .setNegativeButton(R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }

    private void setDfuMode(){
        final LockController lockController = LocksController.getInstance().getLockController();
        activityCallback.getDfuManager().lockController = lockController;
        if (lockController.doFwUpg()) {
            AppBluetoothService.getInstance().dfu = true; // Disable connections during DFU
        } else {
            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage("Invalid Error 712. Please try again. If error persists, terminate and restart the app.")
                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
        }
    }

}

package com.linka.lockapp.aos.module.pages.update;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.i18n._;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.nordicsemi.android.dfu.DfuProgressListener;

public class ThirdUpdateFragment extends Fragment {

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.text_uploading)
    TextView textUploading;

    @BindView(R.id.text_percentage)
    TextView progressPercent;

    private Unbinder unbinder;

    private FirmwareUpdateActivityCallback activityCallback;

    public static ThirdUpdateFragment newInstance() {

        Bundle args = new Bundle();
        ThirdUpdateFragment fragment = new ThirdUpdateFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third_update, container, false);
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
        activityCallback.setBackButtonVisibility(View.GONE);
        activityCallback.changeTitle("Update in Progress...");

        progressBar.getProgressDrawable().setColorFilter(
                Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

        startDfu();
    }

    @OnClick(R.id.cancel_button)
    void onClickCancel() {

        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage("Cancel the Firmware Update process?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changePage();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resumeController();
                    }
                })
                .create();

        alert.show();
        pauseController();
    }

    private void changePage() {
        AppBluetoothService.getInstance().dfu = false;
        abortController();
        activityCallback.getDfuManager().destroyDfu(getActivity(), dfuProgressListener);
        activityCallback.changeCurrentPage(4);

    }

    void pauseController() {
        if (activityCallback.getDfuManager() != null && getActivity() != null) {
            activityCallback.getDfuManager().tryPause(getActivity());
        }
    }

    void resumeController() {
        if (activityCallback.getDfuManager() != null && getActivity() != null) {
            activityCallback.getDfuManager().tryResume(getActivity());
        }
    }

    void abortController() {
        if (activityCallback.getDfuManager() != null && getActivity() != null) {
            activityCallback.getDfuManager().tryAbort(getActivity());
        }
    }


    void onCompleteDfu(boolean isCancelled, boolean isError, String errorMessage) {
        if (isCancelled) {
            textUploading.setText(_.i(R.string.cancelled));
            return;
        }
        if (isError) {
            textUploading.setText(_.i(R.string.dfu_status_error));
            progressPercent.setText(errorMessage);
            startDfu();
            return;
        }

        // DFU Was a success!
        ((FirmwareUpdateActivity) getActivity()).wasDFUSuccessful = true;
        activityCallback.getDfuManager().isUploading = false;
        AppBluetoothService.getInstance().dfu = false;
        activityCallback.getDfuManager().destroyDfu(getActivity(), dfuProgressListener);

        //For the BLOD Popup:
        //Set a timestamp for the current moment that DFU was completed
        Date curDate = new Date();
        AppBluetoothService.getInstance().dfuCompleteTimestamp = curDate.getTime();

        changePage();

        //Reset counter of how many times we've failed to 0
        //Only do this if the field already exists
        if (Prefs.contains("times-failed-fw")) {
            SharedPreferences.Editor edit = Prefs.edit();
            edit.putInt("times-failed-fw", 0);
            edit.commit();
        }

    }


    void startDfu() {

        AppBluetoothService.getInstance().enableFixedTimeScanning(false);

        //Don't disable back button
        //tryDisableAbortAlertButton();
        activityCallback.getDfuManager().startDfu(getActivity(), dfuProgressListener);
    }

    DfuProgressListener dfuProgressListener = new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            textUploading.setText(R.string.dfu_status_connecting);
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            textUploading.setText(R.string.dfu_status_starting);
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {

        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            textUploading.setText(R.string.dfu_status_switching_to_dfu);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            textUploading.setText(R.string.dfu_status_validating);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            textUploading.setText(R.string.dfu_status_disconnecting);
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {

        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            textUploading.setText(R.string.dfu_status_completed);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onCompleteDfu(false, false, null);
                }
            }, 200);
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {

            if (isAdded()
                    && getActivity() != null) {
                AlertDialog alert = new AlertDialog.Builder(getActivity())
                        .setTitle("")
                        .setMessage(R.string.update_firmware_aborted)
                        .setNegativeButton(R.string.ok, null)
                        .create();

                alert.show();

                if (activityCallback.getDfuManager() != null
                        && dfuProgressListener != null
                        && isAdded()
                        && getActivity() != null) {
                    activityCallback.getDfuManager().destroyDfu(getActivity(), dfuProgressListener);
                }
            }
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            progressBar.setIndeterminate(false);
            progressBar.setProgress(percent);
            progressPercent.setText("" + percent + "%");
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            onCompleteDfu(false, true, message);
            activityCallback.changeCurrentPage(4);
        }
    };


}

package com.linka.lockapp.aos.module.pages.dfu;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.setup.SetupLinka1;
import com.linka.lockapp.aos.module.widget.LinkaButton;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.nordicsemi.android.dfu.DfuProgressListener;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Vanson on 17/2/16.
 */
public class DfuManagerPageFragment extends CoreFragment {

    Linka linka;
    @BindView(R.id.step_0)
    LinearLayout step0;
    @BindView(R.id.btn_step_0)
    LinkaButton btnStep0;
    @BindView(R.id.btn_step_1)
    LinkaButton btnStep1;
    @BindView(R.id.step_1)
    LinearLayout step1;
    @BindView(R.id.btn_step_2)
    LinkaButton btnStep2;
    @BindView(R.id.step_2)
    LinearLayout step2;
    @BindView(R.id.btn_step_3)
    LinkaButton btnStep3;
    @BindView(R.id.step_3)
    LinearLayout step3;
    @BindView(R.id.file_status_view)
    TextView fileStatusView;
    @BindView(R.id.text_percentage)
    TextView textPercentage;
    @BindView(R.id.text_uploading)
    TextView textUploading;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.step_4)
    LinearLayout step4;
    @BindView(R.id.btn_step_5)
    LinkaButton btnStep5;
    @BindView(R.id.step_5)
    LinearLayout step5;
    @BindView(R.id.abort_btn)
    LinkaButton abortBtn;

    private Unbinder unbinder;


    int state = 0;

    //Is Bluetooth powered on
    boolean isPoweredOn = true;

    //Boolean for oreo users if they have turned spotify off
    boolean hasConfirmedSpotifyOff = false;

    //Trigger to track if we're in BLOD Recovery Mode
    public boolean blod_firmware_mode = false;
    public boolean blod_firmware_try_again = false; //Are they doing a firmware update after pressing "Yes" to the blod popup?

    public boolean isBusy() {
        return state == 3 || !isPoweredOn;
    }

    DfuManager dfuManager;
    boolean wasDFUSuccessful = false;


    public static DfuManagerPageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        DfuManagerPageFragment fragment = new DfuManagerPageFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public DfuManagerPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dfu_manager_page, container, false);
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
            init(savedInstanceState);

            progressBar.getProgressDrawable().setColorFilter(
                    Color.WHITE, PorterDuff.Mode.SRC_IN);
        }


        // HTC Models have a BTLE customization that causes a crash withing android BTLE stack
        // if you turn off the bluetooth during a scan.  This is unfortuantely required for DFU
        // So let's catch the exception and do nothing
        if (Build.MANUFACTURER.equalsIgnoreCase("htc")) {
            LogHelper.e("DfuManagerPageFrag", "Current device is a HTC device, setting uncaught exception handler");

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    if (ex instanceof IllegalStateException && ex.getMessage()
                            .contains("BT Adapter is not turned ON")) {

                        LogHelper.e("DfuManagerPageFrag", "Got a BT Adapter exception.");
                    }
                }
            });
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    void init(Bundle savedInstanceState) {

        dfuManager = new DfuManager();
        refreshState();

    }


    void refreshState() {

        step0.setVisibility(View.GONE);
        step1.setVisibility(View.GONE);
        step2.setVisibility(View.GONE);
        step3.setVisibility(View.GONE);
        step4.setVisibility(View.GONE);
        step5.setVisibility(View.GONE);

        switch (state) {
            case 0:
                if(blod_firmware_try_again) {
                    showPopupForTryingBlodFix();
                    blod_firmware_try_again = false;

                    step0.setVisibility(View.VISIBLE);
                }else if (turnOnBluetoothToProceed() || displaySpotifyForceStopWarning()) {

                    step0.setVisibility(View.VISIBLE);
                } else {
                    step1.setVisibility(View.VISIBLE);
                }
                break;
            case 1:
                step2.setVisibility(View.VISIBLE);
                break;
            case 2:
                step3.setVisibility(View.VISIBLE);
                btnStep3.setEnabled(false);
                btnStep3.setAlpha(0.2f);
                isPoweredOn = true;
                break;
            case 3:
                step4.setVisibility(View.VISIBLE);
                break;
            case 4:
                step5.setVisibility(View.VISIBLE);
                btnStep5.setEnabled(false);
                btnStep5.setAlpha(0.2f);
                isPoweredOn = false;
                break;
            default:
                step1.setVisibility(View.VISIBLE);
                break;
        }
    }

    // In Nougat 7.0 there is a problem during firmware update
    // If spotify is running it will have a high likelihood of BLOD
    // Check if we're running Nougat, and if Spotify is running, display Screen 0
    boolean displaySpotifyForceStopWarning() {

        // Turns out we DO need to display this, had a customer get a BLOD on 6.0.1
        // So for now disabling this check... Spotify == BAD in all cases, as far as we're concerned

        if(hasConfirmedSpotifyOff) {
            return false;
        }

        //If we're on Oreo (SDK 26) we need to display this every time
        if (Build.VERSION.SDK_INT >= 26) {
            LogHelper.e("SPOTIFY", "Display Spotify Warning due to Oreo");
            return true;
        }

        ActivityManager am = (ActivityManager)this.getAppMainActivity().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(Integer.MAX_VALUE);

        for (int i=0; i<rs.size(); i++) {
            ActivityManager.RunningServiceInfo
                    rsi = rs.get(i);
            LogHelper.d("Running Services", "Process " + rsi.process + " with component " + rsi.service.getClassName());
            if (rsi.service.getClassName().contains("SdlRouterService")) {
                LogHelper.e("Running Services", "Found Spotify Service Running...");
                return true;
            }
        }
        return false;
    }

    void showPopupForTryingBlodFix(){

        new android.app.AlertDialog.Builder(getAppMainActivity())
                .setTitle(R.string.alright)
                .setMessage(R.string.blod_try_again_popup)
                .setNegativeButton(R.string.ok, null)
                .show();

    }

    //Make sure that bluetooth is on before proceeding
    //Popup for user to turn on bluetooth
    //The moment bluetooth is detected on, proceed to next step
    boolean turnOnBluetoothToProceed(){

        BluetoothAdapter bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
        if(bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {

                //Popup to Request user turn on bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                getAppMainActivity().startActivityForResult(enableBtIntent, BLEHelpers.REQUEST_ENABLE_BT);

                //Detect when bluetooth is enabled
                dfuManager.startDetectingBLEPowerOn(getActivity(), new DfuManager.DfuManagerBLEPowerStatusCallback() {
                    @Override
                    public void onPowerOn() {
                        refreshState();
                    }

                    @Override
                    public void onPowerOff() {
                    }
                });

                return true;
            }
        }
        return false;

    }

    void doState() {
        switch (state) {
            case 0:

                break;
            case 1:

                break;
            case 2:

                btnStep3.setEnabled(true);
                btnStep3.setAlpha(1);

                break;
            case 3:
                if (!isAdded()) return;
                if (!dfuManager.isUploading) {
                    startDfu();
                }
                break;
            case 4:

                btnStep5.setEnabled(true);
                btnStep5.setAlpha(1);


                break;
            default:
                break;
        }
    }

    boolean isLocationEnabled() {
        //First check that location services are enabled. If not, popup
        LocationManager lm = (LocationManager) getAppMainActivity().getSystemService(getAppMainActivity().LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            new android.app.AlertDialog.Builder(getAppMainActivity())
                    .setTitle(R.string.location_unavailable)
                    .setMessage(R.string.enable_location)
                    .setNegativeButton(R.string.ok, null)
                    .show();

            return false;
        }else{
                return true;

        }
    }

    boolean isLockLocked() {
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
        if(targetLinka.isLocked){
            new android.app.AlertDialog.Builder(getAppMainActivity())
                        .setTitle(R.string.error)
                        .setMessage(R.string.unlock_to_continue)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            return true;
        }
        return false;
    }

        @OnClick({R.id.btn_step_0, R.id.btn_step_1, R.id.btn_step_2, R.id.btn_step_3, R.id.btn_step_5})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_step_0:
                //If it is oreo, then confirm that they have indeed forced stopped spotify
                if(Build.VERSION.SDK_INT >= 26) {

                    new android.app.AlertDialog.Builder(this.getContext())
                            .setTitle(R.string.screen0_0)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hasConfirmedSpotifyOff = true;
                                    refreshState();
                                }
                            })
                            .show();
                } else if(displaySpotifyForceStopWarning()){
                    new android.app.AlertDialog.Builder(this.getContext())
                            .setTitle(R.string.error)
                            .setMessage(R.string.screen0_1)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                }
                state = 0;
                break;
            case R.id.btn_step_1:
                if(!isLocationEnabled()) {
                    state = 0;
                }else if(blod_firmware_mode){
                    blod_firmware_mode = false;
                    //If firmware recovery mode, immediately skip to 2nd step
                    state = 2;
                }else if (isLockLocked()) {
                    state = 0;
                }else {
                    state = 1;
                }
                break;
            case R.id.btn_step_2:
                setDfuMode();
                break;
            case R.id.btn_step_3:
                state = 3;
                break;
            case R.id.btn_step_5:
                quitDfu();
                return;
            default:
                break;
        }
        refreshState();
        doState();
    }


    void setDfuMode() {

        final LockController lockController = LocksController.getInstance().getLockController();
        dfuManager.lockController = lockController;
        if (lockController.doFwUpg())
        {
            AppBluetoothService.getInstance().dfu = true; // Disable connections during DFU
            state = 2;

            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshState();
                    doState();
                }
            });
        }
        else
        {
            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage("Invalid Error 712. Please try again. If error persists, terminate and restart the app.")
                            .setNegativeButton(R.string.ok, null)
                            .create();
                    alertDialog.show();
                }
            });
        }
/*
        final LockController lockController = LocksController.getInstance().getLockControllerByLinka(linka);
        dfuManager.lockController = lockController;
        boolean res = lockController.doTryReadSettings(new LockController.LockControllerPacketCallback() {
            @Override
            public void onUpdateCounter() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (lockController.doFwUpg())
                        {
                            AppBluetoothService.getInstance().dfu = true; // Disable connections during DFU
                            state = 2;

                            if (!isAdded()) return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshState();
                                    doState();
                                }
                            });
                        }
                        else
                        {
                            if (!isAdded()) return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                            .setTitle(R.string.error)
                                            .setMessage("Invalid Error 712. Please try again. If error persists, terminate and restart the app.")
                                            .setNegativeButton(R.string.ok, null)
                                            .create();
                                    alertDialog.show();
                                }
                            });
                        }
                    }
                }, 500);
            }

            @Override
            public void onTimeout() {
                if (!isAdded()) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.error)
                                .setMessage("Invalid Error 711. Please try again. If error persists, terminate and restart the app.")
                                .setNegativeButton(R.string.ok, null)
                                .create();
                        alertDialog.show();
                    }
                });
            }
        });

        if (!res)
        {
            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage("Invalid Error 710. Please try again. If error persists, terminate and restart the app.")
                            .setNegativeButton(R.string.ok, null)
                            .create();
                    alertDialog.show();
                }
            });
        }*/
    }


    void startDfu() {

        AppBluetoothService.getInstance().enableFixedTimeScanning(false);

        //Don't disable back button
        //tryDisableAbortAlertButton();
        textUploading.setText(R.string.dfu_status_initializing);
        dfuManager.startDfu(getActivity(), dfuProgressListener);
    }


    void onCompleteDfu(boolean isCancelled, boolean isError, String errorMessage) {
        if (isCancelled) {
            state = 33;
            textUploading.setText(_.i(R.string.cancelled));
            return;
        }
        if (isError) {
            state = 33;
            textUploading.setText(_.i(R.string.dfu_status_error));
            textPercentage.setText(errorMessage);
            startDfu();
            return;
        }

        // DFU Was a success!
        wasDFUSuccessful = true;
        dfuManager.isUploading = false;
        AppBluetoothService.getInstance().dfu = false;
        dfuManager.destroyDfu(getActivity(), dfuProgressListener);

        //For the BLOD Popup:
        //Set a timestamp for the current moment that DFU was completed
        Date curDate = new Date();
        AppBluetoothService.getInstance().dfuCompleteTimestamp = curDate.getTime();

        state = 4;
        refreshState();
        doState();

        //Reset counter of how many times we've failed to 0
        //Only do this if the field already exists
        if (Prefs.contains("times-failed-fw")) {
            SharedPreferences.Editor edit = Prefs.edit();
            edit.putInt("times-failed-fw", 0);
            edit.commit();
        }

    }


    public void quitDfu() {
        AppBluetoothService.getInstance().dfu = false;
        abortController();
        dfuManager.destroyDfu(getActivity(), dfuProgressListener);
        AppBluetoothService.getInstance().enableFixedTimeScanning(true);


        // Reset the view controller
        if(Linka.getLinkas().size() == 0){
            SetupLinka1 fragment = SetupLinka1.newInstance();
            AppMainActivity.getInstance().pushFragment(fragment);
        }else {
            AppMainActivity.getInstance().setFragment(MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(),MainTabBarPageFragment.LOCK_SCREEN));
        }

        // If success, popup specific 1.4.3 text and notify the LockController
        // to update the Lock Settings Profile next Context Packet received
        if (wasDFUSuccessful) {

            if(linka != null) {
                linka.updateLockSettingsProfile = true;
                linka.saveSettings();
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.fw_1_4_3_post_update_title)
                            .setMessage(R.string.fw_1_4_3_post_update_desc)
                            .setNegativeButton(R.string.ok, null)
                            .create();
                    alertDialog.show();
                }
            });
        }
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
            tryEnableAbortAlertButton();
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
                    && getActivity() != null)
            {
//                AlertDialog alert = new AlertDialog.Builder(getActivity())
//                        .setTitle("")
//                        .setMessage(R.string.update_firmware_aborted)
//                        .setNegativeButton(R.string.ok, null)
//                        .create();
//
//                alert.show();

//                if (dfuManager != null
//                        && dfuProgressListener != null
//                        && isAdded()
//                        && getActivity() != null) {
//                    dfuManager.destroyDfu(getActivity(), dfuProgressListener);
//                }
            }
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            progressBar.setIndeterminate(false);
            progressBar.setProgress(percent);
            textPercentage.setText("" + percent + "%");
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            onCompleteDfu(false, true, message);
        }
    };


    AlertDialog abortAlertDialog;

    @OnClick(R.id.abort_btn)
    void tryAbortDFU() {
        if (abortAlertDialog != null)
        {
            abortAlertDialog.dismiss();
            abortAlertDialog = null;
        }

        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage("Cancel the Firmware Update process?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        quitDfu();

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
        abortAlertDialog = alert;
    }





    void pauseController()
    {
        if (dfuManager != null && getActivity() != null)
        {
            dfuManager.tryPause(getActivity());
        }
    }

    void resumeController()
    {
        if (dfuManager != null && getActivity() != null)
        {
            dfuManager.tryResume(getActivity());
        }
    }

    void abortController()
    {
        if (dfuManager != null && getActivity() != null)
        {
            dfuManager.tryAbort(getActivity());
        }
    }




    void tryEnableAbortAlertButton()
    {
        if (!abortBtn.isEnabled())
        {
            abortBtn.setEnabled(false);
            abortBtn.setBackgroundColor(getResources().getColor(R.color.linka_white));
            abortBtn.setTextColor(getResources().getColor(R.color.linka_blue));
        }
    }

    void tryDisableAbortAlertButton()
    {
        if (abortBtn.isEnabled())
        {
            abortBtn.setEnabled(false);
            abortBtn.setBackgroundColor(getResources().getColor(R.color.linka_gray));
            abortBtn.setTextColor(getResources().getColor(R.color.linka_white));
        }
    }

}
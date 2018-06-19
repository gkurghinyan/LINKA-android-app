package com.linka.lockapp.aos.module.pages.setup;


import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.dfu.DfuManager;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;

import static android.content.Context.ACTIVITY_SERVICE;

public class AutoUpdateFragment extends CoreFragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";
    private static final String CURRENT_FRAGMENT = "CurrentFragment";
    public static final int WALKTHROUGH = 0;
    public static final int SETTINGS = 1;
    private static final int INITIAL_STEP = 0;
    private static final int FIRST_STEP = 1;
    private static final int SECOND_STEP = 2;
    private static final int THIRD_STEP = 3;
    private static final int FOURTH_STEP = 4;
    private Linka linka;

    private int state = INITIAL_STEP;

    //Is Bluetooth powered on
    private boolean isPoweredOn = true;

    //Boolean for oreo users if they have turned spotify off
    private boolean hasConfirmedSpotifyOff = false;

    //Trigger to track if we're in BLOD Recovery Mode
    public boolean blod_firmware_mode = false;
    public boolean blod_firmware_try_again = false; //Are they doing a firmware update after pressing "Yes" to the blod popup?


    private DfuManager dfuManager;
    private boolean wasDFUSuccessful = false;

    public static AutoUpdateFragment newInstance(Linka linka,int currentFragment) {
        Bundle args = new Bundle();
        args.putSerializable(LINKA_ARGUMENT, linka);
        args.putInt(CURRENT_FRAGMENT,currentFragment);
        AutoUpdateFragment fragment = new AutoUpdateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auto_update, container, false);
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

    private void init() {
        dfuManager = new DfuManager();
        refreshState();
    }

    private void refreshState() {
        switch (state) {
            case INITIAL_STEP:
                firstStep();
                break;
            case FIRST_STEP:
                secondStep();
                break;
            case SECOND_STEP:
                thirdStep();
                break;
            case THIRD_STEP:
                fourthStep();
                break;
            case FOURTH_STEP:
                isPoweredOn = false;
                quitDfu();
                break;
            default:
                firstStep();
                break;
        }
    }

    private void firstStep() {
        if (blod_firmware_try_again) {
            showPopupForTryingBlodFix();
            blod_firmware_try_again = false;

        } else if (Build.VERSION.SDK_INT >= 26 && !hasConfirmedSpotifyOff) {

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
        } else if (displaySpotifyForceStopWarning()) {
            new android.app.AlertDialog.Builder(this.getContext())
                    .setTitle(R.string.error)
                    .setMessage(R.string.screen0_1)
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            state = INITIAL_STEP;
                            refreshState();
                        }
                    })
                    .show();
        } else if (!turnOnBluetoothToProceed()) {
            state = FIRST_STEP;
            refreshState();
        }
    }

    private void secondStep() {
        if (!isLocationEnabled()) {
            new android.app.AlertDialog.Builder(this.getContext())
                    .setTitle(R.string.location_disabled)
                    .setMessage(R.string.location_turn_on)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            state = FIRST_STEP;
                            refreshState();
                        }
                    })
                    .show();
        } else if (blod_firmware_mode) {
            blod_firmware_mode = false;
            //If firmware recovery mode, immediately skip to 2nd step
            state = THIRD_STEP;
            refreshState();
        } else if (isLockLocked()) {
            state = FIRST_STEP;
            refreshState();
        } else {
            state = SECOND_STEP;
            refreshState();
        }
    }

    private void thirdStep() {

        final LockController lockController = LocksController.getInstance().getLockController();
        dfuManager.lockController = lockController;
        if (lockController.doFwUpg()) {
            AppBluetoothService.getInstance().dfu = true; // Disable connections during DFU
            state = THIRD_STEP;

            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshState();
                }
            });
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
                                    getAppMainActivity().popFragment();
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
        }
    }

    private void fourthStep(){
//        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.disable();
//        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (!mBluetoothAdapter.isEnabled()) {
//                    mBluetoothAdapter.enable();
//                }
                isPoweredOn = true;
                if (!isAdded()) return;
                if (!dfuManager.isUploading) {
                    startDfu();
                }
            }
        },3000);
    }

    private void showPopupForTryingBlodFix() {
        new android.app.AlertDialog.Builder(getAppMainActivity())
                .setTitle(R.string.alright)
                .setMessage(R.string.blod_try_again_popup)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshState();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getAppMainActivity().popFragment();
                    }
                })
                .show();
    }

    //Make sure that bluetooth is on before proceeding
    //Popup for user to turn on bluetooth
    //The moment bluetooth is detected on, proceed to next step
    private boolean turnOnBluetoothToProceed() {

        BluetoothAdapter bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
        if (bluetoothAdapter != null) {
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
                        refreshState();
                    }
                });

                return true;
            }
        }
        return false;

    }

    // In Nougat 7.0 there is a problem during firmware update
    // If spotify is running it will have a high likelihood of BLOD
    // Check if we're running Nougat, and if Spotify is running, display Screen 0
    private boolean displaySpotifyForceStopWarning() {

        // Turns out we DO need to display this, had a customer get a BLOD on 6.0.1
        // So for now disabling this check... Spotify == BAD in all cases, as far as we're concerned

        if (hasConfirmedSpotifyOff) {
            return false;
        }

        //If we're on Oreo (SDK 26) we need to display this every time
        if (Build.VERSION.SDK_INT >= 26) {
            LogHelper.e("SPOTIFY", "Display Spotify Warning due to Oreo");
            return true;
        }

        ActivityManager am = (ActivityManager) this.getAppMainActivity().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(Integer.MAX_VALUE);

        for (int i = 0; i < rs.size(); i++) {
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

    private boolean isLocationEnabled() {
        //First check that location services are enabled. If not, popup
        LocationManager lm = (LocationManager) getAppMainActivity().getSystemService(Activity.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        if (lm != null) {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (lm != null) {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (!gps_enabled && !network_enabled) {
            new android.app.AlertDialog.Builder(getAppMainActivity())
                    .setTitle(R.string.location_unavailable)
                    .setMessage(R.string.enable_location)
                    .setNegativeButton(R.string.ok, null)
                    .show();

            return false;
        } else {
            return true;

        }
    }

    private boolean isLockLocked() {
        Linka targetLinka = LinkaNotificationSettings.get_latest_linka();
        if (targetLinka != null && targetLinka.isLocked) {
            new android.app.AlertDialog.Builder(getAppMainActivity())
                    .setTitle(R.string.error)
                    .setMessage(R.string.unlock_to_continue)
                    .setNegativeButton(R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }

    private void startDfu() {

        AppBluetoothService.getInstance().enableFixedTimeScanning(false);

        //Don't disable back button
        //tryDisableAbortAlertButton();
        getAppMainActivity().setBackAviable(false);
        dfuManager.startDfu(getActivity(), dfuProgressListener);
    }

    private DfuProgressListener dfuProgressListener = new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            Log.d("LinkaNot","Connecting");
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            Log.d("LinkaNot","Connected");
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            Log.d("LinkaNot","Starting");
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            Log.d("LinkaNot","Started");
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            Log.d("LinkaNot","EnableDFUMode");
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            Log.d("LinkaNot","Validating");
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            Log.d("LinkaNot","Disconecting");
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            Log.d("LinkaNot","Disconected");
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
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
            getAppMainActivity().setBackAviable(true);

            if (isAdded()
                    && getActivity() != null) {
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
            Log.d("LinkaNot",deviceAddress + String.valueOf(speed));
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//            if(getArguments().getInt(CURRENT_FRAGMENT) == WALKTHROUGH){
//                openWalkthrough();
//            }else {
                onCompleteDfu(false, true, message);
//            }
        }
    };

    private void onCompleteDfu(boolean isCancelled, boolean isError, String errorMessage) {
        getAppMainActivity().setBackAviable(true);
        if (isCancelled) {
            state = 33;
            return;
        }
        if (isError) {
            state = 33;
//            getAppMainActivity().popFragment();
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
        if(getArguments().getInt(CURRENT_FRAGMENT) == WALKTHROUGH) {
            if (wasDFUSuccessful) {
                if (linka != null) {
                    linka.updateLockSettingsProfile = true;
                    linka.saveSettings();
                }
            }
            openWalkthrough();
            return;
        }else if (Linka.getLinkas().size() == 0) {
            SetupLinka1 fragment = SetupLinka1.newInstance();
            AppMainActivity.getInstance().pushFragment(fragment);
        } else {
            AppMainActivity.getInstance().setFragment(MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka()));
        }

        // If success, popup specific 1.4.3 text and notify the LockController
        // to update the Lock Settings Profile next Context Packet received
        if (wasDFUSuccessful) {

            if (linka != null) {
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

    private void openWalkthrough(){
        if (Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).getName() != null &&
                !Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).getName().equals("") && !Prefs.getBoolean(Constants.SHOW_SETUP_NAME, false)) {
            SharedPreferences.Editor editor = Prefs.edit();
            if ((!Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).pacIsSet && Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).pac == 0) ||
                    Prefs.getBoolean(Constants.SHOW_SETUP_PAC, false)) {
                editor.putInt(Constants.SHOWING_FRAGMENT, Constants.SET_PAC_FRAGMENT);
                editor.apply();
                getActivity().finish();
                startActivity(new Intent(getActivity(), WalkthroughActivity.class));
            } else {
                if (Prefs.getBoolean("show-walkthrough", false) || Prefs.getBoolean(Constants.SHOW_TUTORIAL_WALKTHROUGH, false)) {
                    editor.putInt(Constants.SHOWING_FRAGMENT, Constants.TUTORIAL_FRAGMENT);
                } else {
                    editor.putInt(Constants.SHOWING_FRAGMENT, Constants.DONE_FRAGMENT);
                }
                editor.apply();
                getActivity().finish();
                startActivity(new Intent(getActivity(), WalkthroughActivity.class));
            }
        } else {
            getAppMainActivity().pushFragment(SetupLinka3.newInstance(SetupLinka3.WALKTHROUGH));
        }
    }

    void abortController() {
        if (dfuManager != null && getActivity() != null) {
            dfuManager.tryAbort(getActivity());
        }
    }
}

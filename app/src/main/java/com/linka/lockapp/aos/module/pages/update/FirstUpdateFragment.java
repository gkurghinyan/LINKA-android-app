package com.linka.lockapp.aos.module.pages.update;


import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.Context.ACTIVITY_SERVICE;

public class FirstUpdateFragment extends Fragment {

    private Unbinder unbinder;

    private FirmwareUpdateActivityCallback activityCallback;

    //Boolean for oreo users if they have turned spotify off
    boolean hasConfirmedSpotifyOff = false;

    public boolean disableSpotify = false;

    public static FirstUpdateFragment newInstance() {

        Bundle args = new Bundle();
        FirstUpdateFragment fragment = new FirstUpdateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_update, container, false);
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
        activityCallback.changeTitle("Enter Upgrade Mode");

        turnOnBluetoothToProceed();

        if (((FirmwareUpdateActivity) getActivity()).blod_firmware_try_again) {
            showPopupForTryingBlodFix();
            ((FirmwareUpdateActivity) getActivity()).blod_firmware_try_again = false;
        } else {
            disableSpotify = displaySpotifyForceStopWarning();
        }
    }

    @OnClick(R.id.lets_go_button)
    void onClickLetsGo() {
        if (!disableSpotify) {
            if(turnOnBluetoothToProceed()) {
                if (isLocationEnabled()) {
                    if(!isLockLocked()) {
                        activityCallback.changeCurrentPage(2);
                    }
                }
            }else {
                new AlertDialog.Builder(getActivity())
                        .setMessage("Bluetooth enabled now,try again")
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok,null)
                        .create().show();
            }
        } else {
            if (Build.VERSION.SDK_INT >= 26) {

                new android.app.AlertDialog.Builder(this.getContext())
                        .setTitle(R.string.screen0_0)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hasConfirmedSpotifyOff = true;
                                activityCallback.changeCurrentPage(1);
                            }
                        })
                        .show();
            } else if (displaySpotifyForceStopWarning()) {
                new android.app.AlertDialog.Builder(this.getContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.screen0_1)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        }
    }

    @OnClick(R.id.not_see_device_button)
    void onClickNotSeeDevice() {
        new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setMessage("Please go back to the LINKA home page and press the power button on LINKA to reconnect.")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                }).create().show();
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

        ActivityManager am = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
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

    private void showPopupForTryingBlodFix() {
        new android.app.AlertDialog.Builder(getActivity())
                .setTitle(R.string.alright)
                .setMessage(R.string.blod_try_again_popup)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activityCallback.changeCurrentPage(1);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
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
                bluetoothAdapter.enable();
                return false;
            }else {
                return true;
            }
        }
        return false;
    }

    private boolean isLocationEnabled() {
        //First check that location services are enabled. If not, popup
        LocationManager lm = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        if (lm != null) {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (lm != null) {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (!gps_enabled && !network_enabled) {
            new android.app.AlertDialog.Builder(getActivity())
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
}

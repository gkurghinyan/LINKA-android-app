package com.linka.lockapp.aos.module.pages.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.pages.pac.PacTutorialFragment;
import com.linka.lockapp.aos.module.pages.setup.AutoUpdateFragment;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.rey.material.widget.Switch;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS;

/**
 * Created by Vanson on 17/2/16.
 */
public class SettingsPageFragment extends CoreFragment {

    @BindView(R.id.row_phoneless_passcode)
    LinearLayout rowPhonelessPasscode;
    @BindView(R.id.text_phoneless_passcode)
    TextView textPhonelessPasscode;

    @BindView(R.id.row_quick_lock)
    RelativeLayout rowQuickLock;
    @BindView(R.id.text_quick_lock)
    TextView textQuickLock;
    @BindView(R.id.switch_quick_lock)
    Switch switchQuickLock;

    @BindView(R.id.row_edit_name)
    EditText editName;

    @BindView(R.id.row_auto_unlocking)
    RelativeLayout rowAutoUnlocking;
    @BindView(R.id.text_auto_unlock)
    TextView textAutoUnlock;
    @BindView(R.id.settings_auto_unlocking)
    Switch switchAutoUnlocking;

    @BindView(R.id.row_radius_settings)
    LinearLayout rowRadiusSettings;
    @BindView(R.id.text_radius_settings)
    TextView textRadiusSettings;

    @BindView(R.id.row_tamper_siren)
    RelativeLayout rowTamperSiren;
    @BindView(R.id.text_tamper_siren)
    TextView textTamperSiren;
    @BindView(R.id.settings_tamper_siren)
    Switch switchTamperSiren;

    @BindView(R.id.row_tamper_sensitivity)
    LinearLayout rowTamperSensitivity;
    @BindView(R.id.text_tamper_sensitivity)
    TextView textTamperSensitivity;

    @BindView(R.id.row_audible_locking_unlocking)
    RelativeLayout rowAudibleLockingUnlocking;
    @BindView(R.id.text_audible_locking_unlocking)
    TextView textAudibleLockingUnlocking;
    @BindView(R.id.settings_audible_locking_unlocking)
    Switch switchAudibleLockingUnlocking;

    @BindView(R.id.row_sleep_settings)
    LinearLayout rowSleepSettings;
    @BindView(R.id.text_sleep_settings)
    TextView textSleepSettings;

    @BindView(R.id.row_firmware_version)
    LinearLayout rowFirmwareVersion;
    @BindView(R.id.firmware_text)
    TextView firmwareText;
    @BindView(R.id.firmware_version)
    TextView firmwareVersion;

    @BindView(R.id.row_reset_to_factory_settings)
    LinearLayout rowResetToFactorySettings;
    @BindView(R.id.text_reset_to_factory_settings)
    TextView textResetToFactorySettings;

    @BindView(R.id.row_remove_lock)
    LinearLayout rowRemoveLock;
    @BindView(R.id.text_remove_lock)
    TextView textRemoveLock;

    Linka linka;

    RevocationControllerV2 revocationController = new RevocationControllerV2();

    boolean isAdmin = false;
//    @BindView(R.id.fw_update_button)
//    LinkaButton fwUpdateButton;

    private Unbinder unbinder;

    public static SettingsPageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SettingsPageFragment fragment = new SettingsPageFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public SettingsPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        revocationController.onResume();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            if (bundle.get("linka") != null) {
                linka = (Linka) bundle.getSerializable("linka");

                LockController lockController = LocksController.getInstance().getLockController();
                revocationController.implement(getAppMainActivity(), linka, lockController);
            }
            init();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        revocationController.onPause();
    }


    void init() {
        switchAudibleLockingUnlocking.setOnCheckedChangeListener(settings_audible_locking_unlocking);

        switchTamperSiren.setOnCheckedChangeListener(settings_tamper_siren);

        switchAutoUnlocking.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                setRadiusLinearVisibility(checked);
                linka.settings_auto_unlocking = checked;
                linka.saveSettings();
            }
        });
        refreshDisplay();
    }


    Switch.OnCheckedChangeListener settings_audible_locking_unlocking = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(Switch view, boolean checked) {
            if (!doNotSendWrite) {
                linka.settings_audible_locking_unlocking = checked;
                LockController lockController = LocksController.getInstance().getLockController();
                if (lockController.doSetAudibility(checked)) {
                    linka.saveSettings();
                }
            } else {
                linka.settings_audible_locking_unlocking = checked;
                linka.saveSettings();
            }
        }
    };

    Switch.OnCheckedChangeListener settings_tamper_siren = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(Switch view, boolean checked) {
            if (!doNotSendWrite) {
                linka.settings_tamper_siren = checked;
                LockController lockController = LocksController.getInstance().getLockController();
                if (lockController.doSetTamperAlert(checked)) {
                    linka.saveSettings();
                }
            } else {
                linka.settings_tamper_siren = checked;
                linka.saveSettings();
            }
            setTamperSensitivityVisibility(checked);
        }
    };


    boolean doNotSendWrite = false;

    void refreshDisplay() {
        doNotSendWrite = true;
        switchAudibleLockingUnlocking.setChecked(linka.settings_audible_locking_unlocking);
        switchTamperSiren.setChecked(linka.settings_tamper_siren);
        switchAutoUnlocking.setChecked(linka.settings_auto_unlocking);
        setRadiusLinearVisibility(linka.settings_auto_unlocking);
        setTamperSensitivityVisibility(linka.settings_tamper_siren);
        doNotSendWrite = false;

//        switchStallOverride.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(Switch view, boolean checked) {
//                linka.settings_stall_override = checked;
//
//                int stall;
//
//                String fwVersion = linka.fw_version;
//                //If 1.5.9 then use new way of detecting stall, so reduce stall value
//
//                if (checked) {
//                    stall = 250;  // Crank it all the way up
//                    if (fwVersion.equals("1.5.9")){
//                        stall = 0;
//                    }
//                } else {
//                    stall = 135;  // Normal setting
//
//                    if (fwVersion.equals("1.5.9")){
//                        stall = 60;
//                    }
//                }
//
//                // set lock settings
//                LockController lockController = LocksController.getInstance().getLockController();
//                if (lockController.doSetStall(stall)) {
//                    linka.saveSettings();
//
//                    if (checked){
//                        getAppMainActivity().popFragment();
//                        new AlertDialog.Builder(getAppMainActivity())
//                                .setTitle(_.i(R.string.warning))
//                                .setMessage(_.i(R.string.stall_override_warning))
//                                .setNegativeButton(_.i(R.string.ok), null)
//                                .show();
//                    }
//                }
//            }
//        });


        LinkaAccessKey key = LinkaAccessKey.getKeyFromLinka(linka);
        if (key != null && !key.access_key_admin.equals("")) {
            isAdmin = true;
        } else {
            isAdmin = false;
        }

        LockController lockController = LocksController.getInstance().getLockController();
        String ver = lockController.lockControllerBundle.getFwVersionNumber();
        if (!ver.equals("")) {
            if (ver.equals("0.76")) {
                firmwareVersion.setText("1.0");
            } else if (ver.equals("0.77")) {
                firmwareVersion.setText("1.1");
            } else if (ver.equals("0.83")) {
                firmwareVersion.setText("1.2");
            } else {
                firmwareVersion.setText(ver);
            }
        } else {
            firmwareVersion.setText("");
        }

//        macId.setText(linka.lock_mac_address);

        if (isAdmin && linka.isLockSettled) {
            //Set PAC values into settings page
            if (linka.pac == 0 || linka.pac == 1234) {
                if (!lockController.hasReadPac) {
                    lockController.doReadPAC();
                }
            }
        }

        if (linka != null && linka.isConnected && linka.isLockSettled && isAdmin) {
            int color = getResources().getColor(R.color.linka_blue);

            rowPhonelessPasscode.setClickable(true);
            textPhonelessPasscode.setTextColor(color);

            switchQuickLock.setClickable(false);
            switchQuickLock.setAlpha(0.4f);

            editName.setAlpha(1.0f);
            editName.setEnabled(true);
            editName.setText(linka.getName());

            textAutoUnlock.setTextColor(color);
            switchAutoUnlocking.setClickable(true);
            switchAutoUnlocking.setAlpha(1.0f);

            textTamperSiren.setTextColor(color);
            switchTamperSiren.setClickable(true);
            switchTamperSiren.setAlpha(1.0f);

            textAudibleLockingUnlocking.setTextColor(color);
            switchAudibleLockingUnlocking.setClickable(true);
            switchAudibleLockingUnlocking.setAlpha(1.0f);

            textSleepSettings.setTextColor(color);
            rowSleepSettings.setClickable(true);

            textResetToFactorySettings.setTextColor(color);
            rowResetToFactorySettings.setClickable(true);

            textRemoveLock.setTextColor(getResources().getColor(R.color.red));
            rowRemoveLock.setClickable(true);

            firmwareText.setText(getString(R.string.firmware_version));
            firmwareText.setTextColor(getResources().getColor(R.color.search_text));
            rowFirmwareVersion.setClickable(false);

            String no = lockController.lockControllerBundle.getFwVersionNumber();
            if (true) {
                firmwareText.setText(getString(R.string.firmware_update_available));
                firmwareText.setTextColor(getResources().getColor(R.color.red));
                rowFirmwareVersion.setClickable(true);
            }

            if (AppDelegate.shouldAlwaysEnableFwUpgradeButton) {
                firmwareText.setText(getString(R.string.firmware_update_available));
                firmwareText.setTextColor(getResources().getColor(R.color.red));
                rowFirmwareVersion.setClickable(true);
            }

        } else {
            int color = getResources().getColor(R.color.search_text);

            rowPhonelessPasscode.setClickable(false);
            textPhonelessPasscode.setTextColor(color);

            switchQuickLock.setClickable(false);
            switchQuickLock.setAlpha(0.4f);

            editName.setAlpha(1.0f);
            editName.setEnabled(false);
            editName.setText(linka.getName());

            textAutoUnlock.setTextColor(color);
            switchAutoUnlocking.setClickable(false);
            switchAutoUnlocking.setAlpha(0.4f);

            textTamperSiren.setTextColor(color);
            switchTamperSiren.setClickable(false);
            switchTamperSiren.setAlpha(0.4f);

            textAudibleLockingUnlocking.setTextColor(color);
            switchAudibleLockingUnlocking.setClickable(false);
            switchAudibleLockingUnlocking.setAlpha(0.4f);

            textSleepSettings.setTextColor(color);
            rowSleepSettings.setClickable(false);

            textResetToFactorySettings.setTextColor(color);
            rowResetToFactorySettings.setClickable(false);

            textRemoveLock.setTextColor(color);
            rowRemoveLock.setClickable(false);

            firmwareText.setText(getString(R.string.firmware_version));
            firmwareText.setTextColor(getResources().getColor(R.color.search_text));
            rowFirmwareVersion.setEnabled(false);
        }

//        switchAudibleLockingUnlocking.setChecked(linka.settings_audible_locking_unlocking);
//        switchTamperSiren.setChecked(linka.settings_tamper_siren);
//        switchAutoUnlocking.setChecked(linka.settings_auto_unlocking);
//        setRadiusLinearVisibility(linka.settings_auto_unlocking);


        if (linka != null && !linka.isUnlocked()) {
            rowRemoveLock.setClickable(false);
            rowResetToFactorySettings.setClickable(false);
            rowFirmwareVersion.setClickable(false);
        }
    }


    @OnClick(R.id.row_audible_locking_unlocking)
    void onClick_row_audible_locking_unlocking() {
        switchAudibleLockingUnlocking.toggle();
    }

    @OnClick(R.id.row_auto_unlocking)
    void onClick_row_auto_unlocking() {
//        switchAutoUnlocking.toggle();
    }

    @OnClick(R.id.row_phoneless_passcode)
    void onClick_row_phoneless_passcode() {
        getAppMainActivity().pushFragment(PacTutorialFragment.newInstance());
    }

//    @OnClick(R.id.row_edit_name)
//    void onClick_row_edit_name() {
//        getAppMainActivity().pushFragment(SetupLinka3.newInstance(SetupLinka3.SETTINGS));
//    }
//    777777777777777

    @OnClick(R.id.row_tamper_sensitivity)
    void OnClick_row_tamper_sensitivity() {
        getAppMainActivity().pushFragment(SettingsTamperSensitivityFragment.newInstance(linka));
    }

    @OnClick(R.id.row_sleep_settings)
    void OnClick_row_sleep_settings() {
        getAppMainActivity().pushFragment(SettingsBatteryFragment.newInstance(linka));
    }

    @OnClick(R.id.row_tamper_siren)
    void onClick_row_tamper_siren() {
        switchTamperSiren.toggle();
    }

    @OnClick(R.id.row_reset_to_factory_settings)
    void onClick_row_reset_to_factory_settings() {

        new AlertDialog.Builder(getAppMainActivity())
                .setTitle("Are you sure you want to factory reset?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        revocationController.confirmConnected();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    @OnClick(R.id.row_radius_settings)
    void onClick_row_radius_settings() {
        getAppMainActivity().curFragmentCount++;
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.settings_page_root, CheckRadiusFragment.newInstance(linka))
                .commit();
    }

    @OnClick(R.id.row_firmware_version)
    void onClick_fw_update_button() {


//            DfuManagerPageFragment fragment = DfuManagerPageFragment.newInstance(linka);
        AutoUpdateFragment fragment = AutoUpdateFragment.newInstance(linka, AutoUpdateFragment.SETTINGS);
        getAppMainActivity().pushFragment(fragment);

    }


    @Override
    public void onResume() {
        super.onResume();
        getAppMainActivity().setTitle(getString(R.string.big_settings));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;
        if (object instanceof String && ((String) object).equals(LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS)) {

            linka = Linka.getLinkaFromLockController(linka);

            refreshDisplay();

        } else if (object != null && object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {

            linka = Linka.getLinkaFromLockController(linka);

            refreshDisplay();
        }
    }

    private void setRadiusLinearVisibility(boolean visibility) {
        if (visibility) {
            textRadiusSettings.setTextColor(getResources().getColor(R.color.linka_blue));
            rowRadiusSettings.setClickable(true);
        } else {
            textRadiusSettings.setTextColor(getResources().getColor(R.color.search_text));
            rowRadiusSettings.setClickable(false);
        }
    }

    private void setTamperSensitivityVisibility(boolean visibility){
        if(visibility){
            textTamperSensitivity.setTextColor(getResources().getColor(R.color.linka_blue));
            rowTamperSensitivity.setClickable(true);
        }else {
            textTamperSensitivity.setTextColor(getResources().getColor(R.color.search_text));
            rowTamperSensitivity.setClickable(false);
        }
    }

}

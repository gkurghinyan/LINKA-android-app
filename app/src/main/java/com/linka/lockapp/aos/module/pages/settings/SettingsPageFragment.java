package com.linka.lockapp.aos.module.pages.settings;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.pac.SetPac3;
import com.linka.lockapp.aos.module.pages.update.FirmwareUpdateActivity;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.rey.material.widget.Switch;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_READ_SETTINGS;
import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS;

/**
 * Created by Vanson on 17/2/16.
 */
public class SettingsPageFragment extends CoreFragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";
    private static final String SCROLL_VIEW_POSITION = "ScrollViewPosition";
    public static final String FRAGMENT_ADDED = "FragmentAdded";
    public static final int NO_FRAGMENT = 0;
    public static final int TAMPER_SENSITIVITY_FRAGMENT = 1;
    public static final int REMOVE_INFO_FRAGMENT = 2;

    @BindView(R.id.root)
    FrameLayout root;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    @BindView(R.id.settings_page_root)
    FrameLayout rootFrame;

    @BindView(R.id.row_phoneless_passcode)
    RelativeLayout rowPhonelessPasscode;
    @BindView(R.id.text_phoneless_passcode)
    TextView textPhonelessPasscode;
    @BindView(R.id.passcode_text)
    TextView passcode;

    @BindView(R.id.row_quick_lock)
    RelativeLayout rowQuickLock;
    @BindView(R.id.text_quick_lock)
    TextView textQuickLock;
    @BindView(R.id.switch_quick_lock)
    Switch switchQuickLock;
    @BindView(R.id.quick_switch_view)
    View quickSwitchView;

    @BindView(R.id.row_edit_name)
    EditText editName;

    @BindView(R.id.row_auto_unlocking)
    RelativeLayout rowAutoUnlocking;
    @BindView(R.id.text_auto_unlock)
    TextView textAutoUnlock;
    @BindView(R.id.settings_auto_unlocking)
    Switch switchAutoUnlocking;
    @BindView(R.id.auto_switch_view)
    View autoSwitchView;

    @BindView(R.id.row_tamper_siren)
    RelativeLayout rowTamperSiren;
    @BindView(R.id.text_tamper_siren)
    TextView textTamperSiren;
    @BindView(R.id.settings_tamper_siren)
    Switch switchTamperSiren;
    @BindView(R.id.tamper_switch_view)
    View tamperSwitchView;

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
    @BindView(R.id.tone_switch_view)
    View toneSwitchView;

    @BindView(R.id.row_battery_settings)
    RelativeLayout rowBatterySettings;
    @BindView(R.id.text_battery_settings)
    TextView textBatterySettings;
    @BindView(R.id.battery_performance)
    TextView batteryPerformance;

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
    RelativeLayout rowRemoveLock;
    @BindView(R.id.text_remove_lock)
    TextView removeLock;
    @BindView(R.id.remove_info)
    ImageView removeInfo;
    @BindView(R.id.remove_space)
    Space removeSpace;
    @BindView(R.id.remove_top_divider)
    View removeTopDivider;
    @BindView(R.id.remove_bottom_divider)
    View removeBottomDivider;

//      @BindView(R.id.row_radius_settings)
//    LinearLayout rowRadiusSettings;
//    @BindView(R.id.text_radius_settings)
//    TextView textRadiusSettings;

    private Linka linka;
    public static int currentFragment = NO_FRAGMENT;
    private ThreeDotsDialogFragment threeDotsDialogFragment = null;
    private RevocationControllerV2 revocationController = new RevocationControllerV2();
    private boolean isAdmin = false;
    private Unbinder unbinder;
    private int[] array = null;
    //    private boolean doNotSendWrite = false;
    private LockController lockController;
    private Disposable disposable;

    public static SettingsPageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SettingsPageFragment fragment = new SettingsPageFragment();
        bundle.putSerializable(LINKA_ARGUMENT, linka);
        fragment.setArguments(bundle);
        return fragment;
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

        lockController = LocksController.getInstance().getLockController();

        if (getArguments().get(LINKA_ARGUMENT) != null) {
            linka = (Linka) getArguments().getSerializable(LINKA_ARGUMENT);
            revocationController.implement(getAppMainActivity(), linka, lockController);

            if (savedInstanceState != null && savedInstanceState.getIntArray(SCROLL_VIEW_POSITION) != null) {
                array = savedInstanceState.getIntArray(SCROLL_VIEW_POSITION);
            }
        }
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (currentFragment) {
            case TAMPER_SENSITIVITY_FRAGMENT:
                getFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.settings_page_root, SettingsTamperSensitivityFragment.newInstance(linka))
                        .commit();
                break;
            case REMOVE_INFO_FRAGMENT:
                getFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.settings_page_root, RemovingInfoFragment.newInstance())
                        .commit();
                break;
            case NO_FRAGMENT:
                rootFrame.setBackgroundColor(getResources().getColor(R.color.linka_transparent));
                break;

        }
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (editName != null && linka.isLockSettled) {
            if (!linka.getName().equals(editName.getText().toString())) {
                linka.saveName(editName.getText().toString());
                linka.save();
            }
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootFrame.setBackgroundColor(getResources().getColor(R.color.linka_transparent));
        unbinder.unbind();
        revocationController.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (scrollView != null) {
            outState.putIntArray(SCROLL_VIEW_POSITION, new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
        }
    }

    private void init() {
        if (array != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(array[0], array[1]);
                }
            });
        }

        setSwitchesCheckedChangeListeners();

        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (editName != null && linka.isLockSettled) {
                        if (!linka.getName().equals(editName.getText().toString())) {
                            linka.saveName(editName.getText().toString());
                            linka.save();
                        }
                    }
                }
            }
        });

        editName.setText(linka.getName());
        disposable = RxTextView.textChanges(editName)
                .debounce(3, TimeUnit.SECONDS)
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(CharSequence charSequence) {
                        if (editName!=null && editName.getText()!=null) {
                            linka.saveName(editName.getText().toString());
                            linka.save();
                        }
                    }
                });
        editName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) && linka.isLockSettled) {
                    linka.saveName(editName.getText().toString());
                    linka.save();
                }
                return false;
            }
        });
        switchQuickLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value;
                if (switchQuickLock.isChecked()){
                    switchQuickLock.setChecked(false);
                    value = 1;
                }else {
                    switchQuickLock.setChecked(true);
                    value = 0;
                }
                setQuickLockChecked(value);
            }
        });

        refreshDisplay();
    }

    private void setSwitchesCheckedChangeListeners() {
        Switch.OnCheckedChangeListener onCheckedChangeListener = new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                switch (view.getId()) {
                    case R.id.settings_audible_locking_unlocking:
                        if (lockController.doSetAudibility(checked)) {
                            linka.settings_audible_locking_unlocking = checked;
                            linka.saveSettings();
                        }
                        break;

                    case R.id.settings_tamper_siren:
                        if (lockController.doSetTamperAlert(checked)) {
                            linka.settings_tamper_siren = checked;
                            linka.saveSettings();
                        }
                        setTamperSensitivityVisibility(checked);
                        break;

                    case R.id.settings_auto_unlocking:
                        linka.settings_auto_unlocking = checked;
                        linka.save();
                        if (checked && switchQuickLock.isChecked()) {
                            setQuickLockChecked(0);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Quick Lock has been disabled")
                                    .setMessage("Auto-unlock requires Quick Lock to be off.")
                                    .setPositiveButton(R.string.ok, null)
                                    .create().show();
                        }
                        break;

                    case R.id.switch_quick_lock:
                        if (checked && switchAutoUnlocking.isChecked()) {
                            switchAutoUnlocking.setChecked(false);
                            linka.settings_auto_unlocking = false;
                            linka.save();
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Auto-unlock has been disabled")
                                    .setMessage("Quick Lock requires auto-unlock to be off.")
                                    .setPositiveButton(R.string.ok, null)
                                    .create().show();
                        }
                        break;
                }
            }
        };

        switchAudibleLockingUnlocking.setOnCheckedChangeListener(onCheckedChangeListener);
        switchTamperSiren.setOnCheckedChangeListener(onCheckedChangeListener);
        switchAutoUnlocking.setOnCheckedChangeListener(onCheckedChangeListener);
        switchQuickLock.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    void refreshDisplay() {
        switchAudibleLockingUnlocking.setChecked(linka.settings_audible_locking_unlocking);
        switchTamperSiren.setChecked(linka.settings_tamper_siren);
        switchAutoUnlocking.setChecked(linka.settings_auto_unlocking);
//        setRadiusLinearVisibility(linka.settings_auto_unlocking);

        LinkaAccessKey key = LinkaAccessKey.getKeyFromLinka(linka);
        if (key != null && !key.access_key_admin.equals("")) {
            isAdmin = true;
        } else {
            isAdmin = false;
        }

        setFirmwareVersion();
        setBatteryPerformance();

        if (linka.pacIsSet) {
            passcode.setText(String.valueOf(linka.pac));
        } else {
            passcode.setText("");
        }

        if (linka.isLockSettled) {
            //Set PAC values into settings page
            if (linka.pac == 0 || linka.pac == 1234) {
                if (!lockController.hasReadPac) {
                    lockController.doReadPAC();
                }
            }
        }

        if (linka != null && linka.isConnected && linka.isLockSettled) {
            setLockConnectedState();
        } else {
            setLockNotConnectedState();
        }
    }

    private void setLockConnectedState() {
        int color = getResources().getColor(R.color.linka_blue);

        setTamperSensitivityVisibility(linka.settings_tamper_siren);

        rowPhonelessPasscode.setClickable(true);
        textPhonelessPasscode.setTextColor(color);

        textQuickLock.setTextColor(color);
        quickSwitchView.setVisibility(View.GONE);
        switchQuickLock.setAlpha(1.0f);

        editName.setAlpha(1.0f);
        editName.setEnabled(true);

        textAutoUnlock.setTextColor(color);
        autoSwitchView.setVisibility(View.GONE);
        switchAutoUnlocking.setAlpha(1.0f);

        textTamperSiren.setTextColor(color);
        tamperSwitchView.setVisibility(View.GONE);
        switchTamperSiren.setAlpha(1.0f);

        textAudibleLockingUnlocking.setTextColor(color);
        toneSwitchView.setVisibility(View.GONE);
        switchAudibleLockingUnlocking.setAlpha(1.0f);

        textBatterySettings.setTextColor(color);
        rowBatterySettings.setClickable(true);

        textResetToFactorySettings.setTextColor(color);
        rowResetToFactorySettings.setClickable(true);

        removeLock.setTextColor(getResources().getColor(R.color.red));
        rowRemoveLock.setClickable(true);
        removeInfo.setClickable(true);

        checkUpdates();

//            if (AppDelegate.shouldAlwaysEnableFwUpgradeButton) {
//                firmwareText.setText(getString(R.string.firmware_update_available));
//                firmwareText.setTextColor(getResources().getColor(R.color.red));
//                rowFirmwareVersion.setClickable(true);
//            }

        if (linka != null && !linka.isUnlocked()) {
            removeLock.setClickable(false);
            rowResetToFactorySettings.setClickable(false);
            rowFirmwareVersion.setClickable(false);
        }
    }

    private void setLockNotConnectedState() {
        int color = getResources().getColor(R.color.search_text);

        setTamperSensitivityVisibility(false);

        rowPhonelessPasscode.setClickable(false);
        textPhonelessPasscode.setTextColor(color);

        textQuickLock.setTextColor(color);
        switchQuickLock.setVisibility(View.VISIBLE);
        switchQuickLock.setAlpha(0.4f);

        editName.setAlpha(1.0f);
        editName.setEnabled(false);

        textAutoUnlock.setTextColor(color);
        autoSwitchView.setVisibility(View.VISIBLE);
        switchAutoUnlocking.setAlpha(0.4f);

        textTamperSiren.setTextColor(color);
        tamperSwitchView.setVisibility(View.VISIBLE);
        switchTamperSiren.setAlpha(0.4f);

        textAudibleLockingUnlocking.setTextColor(color);
        toneSwitchView.setVisibility(View.VISIBLE);
        switchAudibleLockingUnlocking.setAlpha(0.4f);

        textBatterySettings.setTextColor(color);
        rowBatterySettings.setClickable(false);

        textResetToFactorySettings.setTextColor(color);
        rowResetToFactorySettings.setClickable(false);

        removeLock.setTextColor(color);
        rowRemoveLock.setClickable(false);
        removeInfo.setClickable(false);

        firmwareText.setText(getString(R.string.firmware_version));
        firmwareText.setTextColor(getResources().getColor(R.color.search_text));
        firmwareVersion.setTextColor(getResources().getColor(R.color.search_text));
        rowFirmwareVersion.setClickable(false);
    }

    private void checkUpdates() {
        if (linka != null && lockController != null &&
                linka.isLockSettled && linka.pacIsSet) {
            if (lockController.lockControllerBundle != null) {
                String ver = lockController.lockControllerBundle.getFwVersionNumber();
                if (!ver.equals("")) {
//                    if (!ver.equals(AppDelegate.linkaMinRequiredFirmwareVersion) && !ver.equals("1.5.9") && AppDelegate.linkaMinRequiredFirmwareVersionIsCriticalUpdate) {
                    if (!ver.equals("2.0")) {
                        LogHelper.e("MainTabBarPageFrag", "FW version of " + ver + " does not equal " + AppDelegate.linkaMinRequiredFirmwareVersion);
                        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(linka);
                        if (accessKey != null && accessKey.isAdmin()) {
                            firmwareText.setText(getString(R.string.firmware_update_available));
                            firmwareText.setTextColor(getResources().getColor(R.color.red));
                            firmwareVersion.setTextColor(getResources().getColor(R.color.red));
                            rowFirmwareVersion.setClickable(true);
                            return;
                        }
                    }
                }
            }
        }
        firmwareText.setText(getString(R.string.firmware_version));
        firmwareText.setTextColor(getResources().getColor(R.color.search_text));
        firmwareVersion.setTextColor(getResources().getColor(R.color.search_text));
        rowFirmwareVersion.setClickable(false);
    }

    private void setFirmwareVersion() {
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
    }

    private void setBatteryPerformance() {
        if (linka.settingsSleepPerformance == 1800) {
            linka.settingsSleepPerformance = Linka.NORMAL_PERFORMANCE;
            linka.save();
        }

        switch (linka.settingsSleepPerformance) {
            case Linka.LOW_PERFORMANCE:
                batteryPerformance.setText("Low");
                break;
            case Linka.NORMAL_PERFORMANCE:
                batteryPerformance.setText("Normal");
                break;
            case Linka.HIGH_PERFORMANCE:
                batteryPerformance.setText("High");
                break;
        }
    }

    private void setQuickLockChecked(int isChecked) {
        lockController.doAction_SetQuickLock(isChecked);
        lockController.read_setting();
    }

    private void setTamperSensitivityVisibility(boolean visibility) {
        if (visibility) {
            textTamperSensitivity.setTextColor(getResources().getColor(R.color.linka_blue));
            rowTamperSensitivity.setClickable(true);
        } else {
            textTamperSensitivity.setTextColor(getResources().getColor(R.color.search_text));
            rowTamperSensitivity.setClickable(false);
        }
    }

    private void cancelThreeDotsDialog() {
        if (threeDotsDialogFragment != null) {
            threeDotsDialogFragment.dismiss();
            threeDotsDialogFragment = null;
        }
    }

    private void removeLock() {
        if (!isAdmin) {
            LinkaAPIServiceImpl.revoke_access(getActivity(), linka, LinkaAPIServiceImpl.getUserID(), new Callback<LinkaAPIServiceResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                    cancelThreeDotsDialog();
                    if (LinkaAPIServiceImpl.check(response, false, null)) {
                        getAppMainActivity().logout();
//                        Linka.removeLinka(linka);
                        getAppMainActivity().resetActivity();
                    }
                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                    cancelThreeDotsDialog();
                }
            });
        } else {
            LinkaAPIServiceImpl.hide_lock(getActivity(), linka.lock_mac_address, new Callback<LinkaAPIServiceResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                    cancelThreeDotsDialog();
                    if (LinkaAPIServiceImpl.check(response, false, null)) {
                        getAppMainActivity().logout();
                    }
                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                    cancelThreeDotsDialog();
                }
            });
        }
    }

    //    private void setRadiusLinearVisibility(boolean visibility) {
//        if (visibility) {
//            textRadiusSettings.setTextColor(getResources().getColor(R.color.linka_blue));
//            rowRadiusSettings.setClickable(true);
//        } else {
//            textRadiusSettings.setTextColor(getResources().getColor(R.color.search_text));
//            rowRadiusSettings.setClickable(false);
//        }
//    }

//    private void setSwitchStallOverrideCheckedChangeListener(){
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
//    }

    @OnClick(R.id.row_phoneless_passcode)
    void onClick_row_phoneless_passcode() {
        getAppMainActivity().pushFragmentWithoutAnimation(SetPac3.newInstance(LinkaNotificationSettings.get_latest_linka(), SetPac3.SETTINGS));
    }

    @OnClick(R.id.row_tamper_sensitivity)
    void OnClick_row_tamper_sensitivity() {
        currentFragment = TAMPER_SENSITIVITY_FRAGMENT;
        getFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.settings_page_root, SettingsTamperSensitivityFragment.newInstance(linka))
                .commit();
    }

    @OnClick(R.id.row_battery_settings)
    void OnClick_row_battery_settings() {
        getAppMainActivity().pushFragmentWithoutAnimation(SettingsBatteryFragment.newInstance(linka));
    }

    @OnClick(R.id.row_reset_to_factory_settings)
    void onClick_row_reset_to_factory_settings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure you want to reset?")
                .setMessage("This will delete all data and revoke all access to this lock")
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        revocationController.confirmConnected();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

//    @OnClick(R.id.row_radius_settings)
//    void onClick_row_radius_settings() {
//        getAppMainActivity().curFragmentCount++;
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .addToBackStack(null)
//                .replace(R.id.settings_page_root, CheckRadiusFragment.newInstance(linka))
//                .commit();
//    }

    @OnClick(R.id.row_firmware_version)
    void onClick_fw_update_button() {

        Intent intent = new Intent(getActivity(), FirmwareUpdateActivity.class);
        intent.putExtra(FirmwareUpdateActivity.LINKA_EXTRA, linka);
        startActivity(intent);
//            DfuManagerPageFragment fragment = DfuManagerPageFragment.newInstance(linka);
//        AutoUpdateFragment fragment = AutoUpdateFragment.newInstance(linka, AutoUpdateFragment.SETTINGS);
//        getAppMainActivity().pushFragment(fragment);

    }

    @OnClick(R.id.row_remove_lock)
    void onRemoveLockClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Remove this lock?").
                setMessage("The lock will no longer appear in the app until you add it back.").
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeLock();
                        threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance().setConnectingText(false, null);
                        threeDotsDialogFragment.show(getFragmentManager(), null);
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.remove_info)
    void onRemoveInfoClicked() {
        currentFragment = REMOVE_INFO_FRAGMENT;
        getFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.settings_page_root, RemovingInfoFragment.newInstance())
                .commit();
    }

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;
        if (object != null && object instanceof String) {
            if (object.equals(LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS) || object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {
                linka = Linka.getLinkaFromLockController();
                refreshDisplay();
            }else if (object.equals(LOCKSCONTROLLER_NOTIFY_READ_SETTINGS)){
                setQuickSwitchViewStatus();
            }else if (object.equals(FRAGMENT_ADDED)) {
                rootFrame.setBackgroundColor(getResources().getColor(R.color.linka_transparent));
            } else if (((String) object).substring(0, 9).equals(MainTabBarPageFragment.SELECTED_SCREEN)) {
                if (object.equals(MainTabBarPageFragment.SELECTED_SCREEN + String.valueOf(MainTabBarPageFragment.SETTING_SCREEN))) {
                    editName.setText(linka.getName());
                }
            }
        }
    }

    private void setQuickSwitchViewStatus(){
         if (linka.settings_quick_lock == 1) {
            switchQuickLock.setChecked(true);
        } else if (linka.settings_quick_lock == 0) {
            switchQuickLock.setChecked(false);
        }
    }
}

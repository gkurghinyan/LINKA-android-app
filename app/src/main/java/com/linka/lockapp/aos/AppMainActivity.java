package com.linka.lockapp.aos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.linka.lockapp.aos.module.adapters.LockListAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreActivity;
import com.linka.lockapp.aos.module.gcm.MyFirebaseInstanceIdService;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.AppLocationService;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.helpers.FontHelpers;
import com.linka.lockapp.aos.module.helpers.GeofencingService;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.helpers.LocksHelper;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.helpers.NotificationsHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.other.Utils;
import com.linka.lockapp.aos.module.pages.AvailableDevicesFragment;
import com.linka.lockapp.aos.module.pages.CircleView;
import com.linka.lockapp.aos.module.pages.TestingFragment;
import com.linka.lockapp.aos.module.pages.dfu.DfuManagerPageFragment;
import com.linka.lockapp.aos.module.pages.help.HelpFragment;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.location.LocationPageFragment;
import com.linka.lockapp.aos.module.pages.mylinkas.MyLinkasPageFragmentPage;
import com.linka.lockapp.aos.module.pages.notifications.NotificationSettingsPageFragment;
import com.linka.lockapp.aos.module.pages.notifications.NotificationsPageFragment;
import com.linka.lockapp.aos.module.pages.others.WebPageFragment;
import com.linka.lockapp.aos.module.pages.pac.PacTutorialFragment;
import com.linka.lockapp.aos.module.pages.pac.SetPac3;
import com.linka.lockapp.aos.module.pages.prelogin.ForgotPasswordPage1;
import com.linka.lockapp.aos.module.pages.prelogin.ForgotPasswordPage2;
import com.linka.lockapp.aos.module.pages.prelogin.SignInPage;
import com.linka.lockapp.aos.module.pages.prelogin.SignUpPage;
import com.linka.lockapp.aos.module.pages.prelogin.WelcomePage;
import com.linka.lockapp.aos.module.pages.settings.AppSettingsFragment;
import com.linka.lockapp.aos.module.pages.settings.SettingsEditNamePageFragment;
import com.linka.lockapp.aos.module.pages.settings.SettingsPageFragment;
import com.linka.lockapp.aos.module.pages.settings.SettingsTamperSensitivityFragment;
import com.linka.lockapp.aos.module.pages.setup.AutoUpdateFragment;
import com.linka.lockapp.aos.module.pages.setup.SetupLinka1;
import com.linka.lockapp.aos.module.pages.setup.SetupLinka2;
import com.linka.lockapp.aos.module.pages.setup.SetupLinka3;
import com.linka.lockapp.aos.module.pages.walkthrough.AccessLockFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughGetToKnow;
import com.linka.lockapp.aos.module.widget.BadgeIconView;
import com.linka.lockapp.aos.module.widget.LinkaTouchableLinearLayout;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 7/11/15.
 */
public class AppMainActivity extends CoreActivity {


    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.menu)
    BadgeIconView menu;

    @BindView(R.id.back)
    BadgeIconView back;

    @BindView(R.id.nav_bar_left_icons)
    FrameLayout navBarLeftIcons;

    @BindView(R.id.nav_bar_right_icons)
    LinearLayout navBarRightIcons;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;

    @BindView(R.id.drawer)
    RelativeLayout drawer;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.toolbar_space)
    View toolbarSpace;


    /* MENU */
    @BindView(R.id.r1)
    TextView r1;
    /*
    @InjectView(R.id.item_1_text)
    LinkaTextView item1Text;
    @InjectView(R.id.item_1)
    LinkaTouchableLinearLayout item1;
    @InjectView(R.id.item_2_text)
    LinkaTextView item2Text;
    @InjectView(R.id.item_2)
    LinkaTouchableLinearLayout item2;
    @InjectView(R.id.item_3_text)
    LinkaTextView item3Text;
    @InjectView(R.id.item_3)
    LinkaTouchableLinearLayout item3;
    @InjectView(R.id.item_4_text)
    LinkaTextView item4Text;
    @InjectView(R.id.item_4)
    LinkaTouchableLinearLayout item4;*/
    @BindView(R.id.item_add)
    LinkaTouchableLinearLayout itemAdd;

    @BindView(R.id.sidebar_icon_help)
    LinkaTouchableLinearLayout itemHelp;

    @BindView(R.id.sidebar_icon_settings)
    LinkaTouchableLinearLayout itemSettings;

    @BindView(R.id.sidebar_icon_logout)
    LinkaTouchableLinearLayout sidebarIconLogout;

    @BindView(R.id.user_avatar)
    ImageView userAvatar;

    @BindView(R.id.user_name)
    TextView userName;


    @BindView(R.id.log)
    TextView log;

    /*
        @InjectView(R.id.item_1_delete)
        ImageView item1Delete;
        @InjectView(R.id.item_2_delete)
        ImageView item2Delete;
        @InjectView(R.id.item_3_delete)
        ImageView item3Delete;
        @InjectView(R.id.item_4_delete)
        ImageView item4Delete;*/
    @BindView(R.id.main_root)
    RelativeLayout root;

    private boolean isBackAvailable = true;

    public enum WalkthroughOrder {
        SETUP,
        PAC,
        TAMPER,
        SHARING,
        AUTOUNLOCK,
        GET_TO_KNOW,
        NONE
    }

    public WalkthroughOrder currentWalkthrough;

    LockListAdapter adapter;
    RecyclerView recyclerView;
    private Fragment drawerFragment = null;

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            View view = AppMainActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            if (drawerFragment != null) {
                setFragment(drawerFragment);
                drawerFragment = null;
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.recyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        initDrawer();
        initNavBar();

        Linka linka = new Linka();
        Linka linka1 = new Linka();
        Linka linka2 = linka;
        Log.d("linka_code", String.valueOf(linka.hashCode()));
        Log.d("linka_code", String.valueOf(linka1.hashCode()));
        Log.d("linka_code", String.valueOf(linka2.hashCode()));
        linka2 = linka1;
        Log.d("linka_code", String.valueOf(linka2.hashCode()));



        MyFirebaseInstanceIdService.getFcmToken();

//        if (!AppDelegate.shouldShowSelectLanguage) {
//            AppLanguagePickerActivity.forceSelectLanguageEnglish(getBaseContext());
//
//            sidebarTextSelectLanguage.setVisibility(View.GONE);
//        }

        /*if (AppLanguagePickerActivity.shouldStartLanguageSelect()) {
            AppLanguagePickerActivity.createNewInstance(this, true);
        } else {
            LinkaAPIServiceImpl.get_app_version(this, false, null);
        }*/

        if (getIntent()!=null && getIntent().getAction()!=null && getIntent().getAction().equals(NotificationsHelper.LINKA_NOTIFICATION_ACTION)){
            setFragment(MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(), MainTabBarPageFragment.NOTIFICATION_SCREEN));
        }else {
            setFragment(decide(getIntent()));
        }
        LinkaAPIServiceImpl.get_app_version(this, false, null);
        // Listen to bluetooth to detect state changes

        startService(new Intent(this, AppBluetoothService.class));

        //Start Location Service
        if (!Utils.isMyServiceRunning(AppLocationService.class,this)) {
            startService(new Intent(this, AppLocationService.class));
        }
        if (!Utils.isMyServiceRunning(GeofencingService.class,this)) {
            //Start Location Service
            LogHelper.e("GEOFENCE", "Intent");
            startService(new Intent(this, GeofencingService.class));
            GeofencingService.init(AppMainActivity.this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setFragment(decide(intent));
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppDelegate.activityResumed();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppDelegate.activityPaused();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setFragment(WalkthroughOrder step) {
        switch (step) {
            case SETUP:
                break;

            case SHARING:
                popFragment();
                pushFragment(WalkthroughGetToKnow.class);
                break;

            case GET_TO_KNOW:
                popFragment();
                break;


            case NONE:
                popFragment();
        }
    }

    public void didSignIn() {

        LocksHelper.get_locks(AppMainActivity.this, new LocksHelper.LocksCallback() {
            @Override
            public void onNext() {
                resetActivity();
            }
        });

//        setFragment(decide());
    }


    public void resetActivity() {
//        Intent intent = getIntent();
        Intent intent = new Intent(this, AppMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
    }


    public Fragment decide(Intent intent) {
        LinkaNotificationSettings.refresh_for_latest_linka();
        LocksController.getInstance().refresh();

        if (LinkaAPIServiceImpl.getUserID() != null) {

            if (Linka.getLinkas().size() > 0) {
                EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);
                refreshDevices();

                //Only start bluetooth scanning if not dfu mode
                if (!AppBluetoothService.getInstance().dfu) {
                    AppBluetoothService.getInstance().enableFixedTimeScanning(true);
                }
                Fragment fragment = null;

                if (intent.getBooleanExtra(Constants.IS_IT_OPEN_FROM_NOTIFICATION, false)) {
                    fragment = MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(), MainTabBarPageFragment.LOCK_SCREEN);
                } else if (intent.getBooleanExtra(Constants.OPEN_SETTINGS, false)) {
                    fragment = MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(), MainTabBarPageFragment.SETTING_SCREEN);
                } else {
                    switch (Prefs.getInt(Constants.SHOWING_FRAGMENT, Constants.LAUNCHER_FRAGMENT)) {
                        case Constants.LAUNCHER_FRAGMENT:
                            fragment = MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(), MainTabBarPageFragment.LOCK_SCREEN);
                            break;
                        case Constants.SET_NAME_FRAGMENT:
                            fragment = SetupLinka3.newInstance(SetupLinka3.WALKTHROUGH);
                            break;
                        case Constants.SET_PAC_FRAGMENT:
                            this.finish();
                            startActivity(new Intent(AppMainActivity.this, WalkthroughActivity.class));
                            break;
                        case Constants.TUTORIAL_FRAGMENT:
                            this.finish();
                            startActivity(new Intent(AppMainActivity.this, WalkthroughActivity.class));
                            break;
                        case Constants.DONE_FRAGMENT:
                            this.finish();
                            startActivity(new Intent(AppMainActivity.this, WalkthroughActivity.class));
                            break;
                    }
                }
                return fragment;

            } else {
                LinkaNotificationSettings.disconnect_all_linka();
                AppBluetoothService.getInstance().enableFixedTimeScanning(false);
                Fragment fragment = SetupLinka1.newInstance();
                return fragment;
            }
        } else {
            LinkaNotificationSettings.disconnect_all_linka();
            AppBluetoothService.getInstance().enableFixedTimeScanning(false);

            Fragment fragment = WelcomePage.newInstance();
            return fragment;
        }
    }

    /*Function to detect changes to bluetooth state */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        LogHelper.e("BLUETOOTH", "Bluetooth off");


                        //Turn off bluetooth scanning
                        AppBluetoothService.getInstance().enableFixedTimeScanning(false);

                        //Update Settings and my linkas Page
                        EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        LogHelper.e("BLUETOOTH", "Bluetooth on");


                        //Only enable bluetooth scanning if dfu mode is off
                        if (!AppBluetoothService.getInstance().dfu) {
                            //Turn on Bluetooth scanning, and immediatly try to pair up
                            AppBluetoothService.getInstance().enableFixedTimeScanning(true);
                        }

                        //Update Settings and my linkas Page
                        EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    /* LINKA */

    public void addLinka(Linka linka) {

    }

    public void saveLatestLinka(Linka linka) {
        //only connect user selected device
//        AppBluetoothService.instance.is_user_selected_device_to_connect = true;
        LinkaNotificationSettings.save_as_latest_linka(linka);
        refreshDevices();
    }

    public void gotoLinka(Linka linka) {
        saveLatestLinka(linka);
        drawerFragment = MainTabBarPageFragment.newInstance(linka, MainTabBarPageFragment.LOCK_SCREEN);
        drawerLayout.closeDrawers();
//        setFragment(fragment);
    }

    public void removeLinka(Linka linka) {
        // Remove from memory
        LinkaAccessKey.deleteAllKeysFromLinka(linka);
        LinkaActivity.removeAllActivitiesForLinka(linka);


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice _device : pairedDevices) {
                if (_device.getAddress().equals(linka.lock_address)) {
                    if (_device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        try {
                            Method m = null;
                            m = _device.getClass().getMethod("removeBond", (Class[]) null);
                            m.invoke(_device, (Object[]) null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        linka.delete();

        // Refresh
        Linka existingLinka = LinkaNotificationSettings.refresh_for_latest_linka();

        // Notify
        resetActivity();
    }




    /* NAV BAR */


    private void initDrawer() {
        drawerLayout.addDrawerListener(drawerListener);
        setDrawer();
        back.setOnClickListener(defaultBackListener);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(drawer)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(drawer);
                }
            }
        });
        back.setVisibility(View.GONE);
    }

    View.OnClickListener defaultBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private boolean isOtherBackListener = false;

    public void setOnBackListener(View.OnClickListener clickListener) {
        if (clickListener != null) {
            menu.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
            isOtherBackListener = true;
            back.setOnClickListener(clickListener);
        }
    }

    public void removeBackListener() {
        back.setVisibility(View.GONE);
        menu.setVisibility(View.VISIBLE);
        isOtherBackListener = false;
        back.setOnClickListener(defaultBackListener);
    }

    public void setDrawer() {
        if (LinkaAPIServiceImpl.isLoggedIn()) {
            userName.setText(Prefs.getString("user-name", ""));

        }
    }


    public void enableDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void disableDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }


    public void setTitle(String name) {
        title.setText(name.toUpperCase());
    }

    public void setTitleNoUpperCase(String name) {
        title.setText(name);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void initNavBar() {
        setSupportActionBar(toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        FontHelpers.setFontFaceLight(this, title);
    }




    /* ON CHANGE FRAGMENT */


    @Override
    public void onBackPressed() {
        if (isOtherBackListener) {
            back.callOnClick();
            return;
        }
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
            return;
        }
        if (isBackAvailable) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof DfuManagerPageFragment) {
                final DfuManagerPageFragment f = (DfuManagerPageFragment) fragment;
                //Never disable the back button
                //Until we can ensure that the firmware update will go through
                //Sometimes they will be stuck in the firmware update process with no way of exiting.
            /*if (f.isBusy()) {
                return;
            } else {*/
                new AlertDialog.Builder(this)
                        .setTitle(_.i(R.string.sure_quit_fw_upgrade))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                f.quitDfu();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();

                return;
                //}
            }else if (!(fragment instanceof MainTabBarPageFragment)){
                if (fragment instanceof HelpFragment || fragment instanceof SetupLinka1 || fragment instanceof AppSettingsFragment) {
                    if (LinkaNotificationSettings.get_latest_linka() != null) {
                        setFragment(MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(), MainTabBarPageFragment.LOCK_SCREEN));
                        return;
                    } else {
                        super.onBackPressed();
                    }
                }else {
                    super.onBackPressed();
                }
            }else {
                super.onBackPressed();
            }
         /*   if (curFragmentCount <= 0) {
                super.onBackPressed();
//            if (fragment instanceof StartupFragment) {
//            } else {
//                setFragment(StartupFragment.newInstance(true));
//            }
            } else {
                super.onBackPressed();
            }*/
            setBackIconVisible(false);
        }
    }

    public boolean isBackAvailable() {
        return isBackAvailable;
    }

    public void setBackAvailable(boolean backAvailable) {
        isBackAvailable = backAvailable;
    }

    @Override
    public void onChangeFragment(Fragment fragment) {
        super.onChangeFragment(fragment);

        if (fragment == null) {
            fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }

        if (curFragmentCount <= 0) {
            menu.setVisibility(View.VISIBLE);

            back.setVisibility(View.GONE);
        } else {
            menu.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
        }

        refreshDevices();

        r1.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);

        if (fragment instanceof WelcomePage) {
            toolbar.setVisibility(View.GONE);
            toolbarSpace.setVisibility(View.GONE);
            disableDrawer();
        } else if (fragment instanceof SignUpPage
                || fragment instanceof SignInPage
                || fragment instanceof SetupLinka3
                || fragment instanceof AutoUpdateFragment
                || fragment instanceof SetupLinka2
                || fragment instanceof AccessLockFragment) {
            toolbar.setVisibility(View.GONE);
            toolbarSpace.setVisibility(View.GONE);
            title.setTextColor(getResources().getColor(R.color.linka_white));
            disableDrawer();
        } else if (fragment instanceof ForgotPasswordPage1
                || fragment instanceof ForgotPasswordPage2) {
            if (!LinkaAPIServiceImpl.isLoggedIn()) {
                toolbar.setVisibility(View.GONE);
                toolbarSpace.setVisibility(View.GONE);
                title.setTextColor(getResources().getColor(R.color.linka_white));
                disableDrawer();
            } else {
                toolbar.setVisibility(View.VISIBLE);
                toolbarSpace.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(getResources().getColor(R.color.linka_blue_tabbar));
                title.setTextColor(getResources().getColor(R.color.linka_white));
                enableDrawer();
            }
        } else if (fragment instanceof SettingsPageFragment
                || fragment instanceof SettingsEditNamePageFragment
                || fragment instanceof NotificationsPageFragment
                || fragment instanceof LocationPageFragment
                || fragment instanceof MyLinkasPageFragmentPage
                || fragment instanceof WebPageFragment
                || fragment instanceof MainTabBarPageFragment
                || fragment instanceof TestingFragment
                || fragment instanceof AvailableDevicesFragment
                || fragment instanceof PacTutorialFragment
                || fragment instanceof SetPac3
                || fragment instanceof HelpFragment
                || fragment instanceof SetupLinka1) {
            toolbar.setVisibility(View.VISIBLE);
            toolbarSpace.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(getResources().getColor(R.color.linka_blue_tabbar));
            title.setTextColor(getResources().getColor(R.color.linka_white));
            enableDrawer();
        } else if (fragment instanceof DfuManagerPageFragment) {
            toolbar.setVisibility(View.VISIBLE);
            toolbarSpace.setVisibility(View.GONE);
            toolbar.setBackgroundColor(getResources().getColor(R.color.linka_blue_tabbar_transparent));
            title.setTextColor(getResources().getColor(R.color.linka_white));
            disableDrawer();
        }

        if (fragment instanceof WelcomePage) {
            setTitle(_.i(R.string.sign_in));
        } else if (fragment instanceof SignUpPage) {
            setTitle(_.i(R.string.sign_up));
        } else if (fragment instanceof SetupLinka1) {
            setTitle(_.i(R.string.setup_linka));
        } else if (fragment instanceof SetupLinka2) {
            setTitle(_.i(R.string.setup_linka));
        } else if (fragment instanceof CircleView) {
            setTitle(LinkaNotificationSettings.get_latest_linka().getName());
        } else if (fragment instanceof SettingsPageFragment) {
            setTitle(_.i(R.string.settings));
        } else if (fragment instanceof SetPac3) {
            setTitle(_.i(R.string.set_pac));
        } else if (fragment instanceof SettingsEditNamePageFragment) {
            setTitle(_.i(R.string.edit_name));
        } else if (fragment instanceof SettingsTamperSensitivityFragment) {
            setTitle(_.i(R.string.tamper_sensitivity));
        } else if (fragment instanceof NotificationsPageFragment) {
            setTitle(_.i(R.string.activity));
        } else if (fragment instanceof NotificationSettingsPageFragment) {
            setTitle(R.string.activities);
        } else if (fragment instanceof LocationPageFragment) {
            setTitle(_.i(R.string.location));
        } else if (fragment instanceof WebPageFragment) {
            setTitle(_.i(""));
        } else if (fragment instanceof MyLinkasPageFragmentPage) {
            ((MyLinkasPageFragmentPage) fragment).implementTitle();
            r1.setTextColor(getResources().getColor(R.color.linka_white));
        } else if (fragment instanceof DfuManagerPageFragment) {
            setTitle(_.i(R.string.firmware_update));
        }


        LogHelper.e("App main ACtivity", "Fragment changed");
        if (fragment instanceof SetupLinka2
                || fragment instanceof SetupLinka1) {
            AppBluetoothService.getInstance().enableFixedTimeScanning(false);
        } else {
            AppBluetoothService.getInstance().enableFixedTimeScanning(true);
        }
    }

    public void setBackIconVisible(boolean visible) {
        if (visible) {
            menu.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
        } else {
            back.setVisibility(View.GONE);
            menu.setVisibility(View.VISIBLE);
        }
    }


    /* MENU */

    //    @OnClick(R.id.sidebar_lock)
    void onClick_sidebar_lock() {

        if (Linka.getLinkas().size() == 0) {
            drawerFragment = SetupLinka1.newInstance();
//            pushFragment(fragment);
        } else {
            drawerFragment = MainTabBarPageFragment.newInstance(LinkaNotificationSettings.get_latest_linka(), MainTabBarPageFragment.LOCK_SCREEN);
        }
        drawerLayout.closeDrawers();
    }

    //    @OnClick(R.id.sidebar_available_devices)
    void onClick_sidebar_available_devices() {
        drawerFragment = AvailableDevicesFragment.newInstance();

        drawerLayout.closeDrawers();
    }

    //    @OnClick(R.id.sidebar_notifications)
    void onClick_sidebar_notifications() {
        drawerFragment = NotificationSettingsPageFragment.newInstance();

        drawerLayout.closeDrawers();
    }

    private int firmwareMode = 0;

    //    @OnClick(R.id.sidebar_icon_user_email)
    void onClick_sidebar_email() {

        //Enable firmware recovery mode if it is pressed 10 times
        firmwareMode += 1;

        if (firmwareMode >= 10) {
            firmwareMode = 0;
            LogHelper.e("FIRMWARE", "Firmware recovery mode engaged");

            new android.app.AlertDialog.Builder(this)
                    .setTitle(_.i(R.string.firmware_recovery))
                    .setCancelable(false)
                    .setMessage(_.i(R.string.firmware_recovery_question))
                    .setPositiveButton(_.i(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            drawerLayout.closeDrawers();

                            //Start DFU fragment, make LINKA parameter null because we don't initialize it
//                            DfuManagerPageFragment fragment = DfuManagerPageFragment.newInstance(null);
                            AutoUpdateFragment fragment = AutoUpdateFragment.newInstance(null, AutoUpdateFragment.SETTINGS);
                            fragment.blod_firmware_mode = true;
                            pushFragment(fragment);

                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        }
    }

    //    @OnClick(R.id.sidebar_icon_reportbug)
    void onClick_sidebar_bug() {
        drawerLayout.closeDrawers();
        Helpers.sendEmail(this, _.i(R.string.report_a_bug_url), _.i(R.string.report_a_bug));
    }

    //    @OnClick(R.id.sidebar_icon_tc)
    void onClick_sidebar_terms_conditions() {
        drawerFragment = WebPageFragment.newInstance(_.i(R.string.terms_and_conditions), _.i(R.string.terms_url));
        drawerLayout.closeDrawers();
    }

    //    @OnClick(R.id.sidebar_icon_privacy)
    void onClick_sidebar_privacy() {
        drawerFragment = WebPageFragment.newInstance(_.i(R.string.privacy_policy), _.i(R.string.privacy_url));
        drawerLayout.closeDrawers();
    }

    //    @OnClick(R.id.sidebar_icon_faq)
    void onClick_sidebar_faq() {
        drawerFragment = WebPageFragment.newInstance(_.i(R.string.faqs), _.i(R.string.faq_url));
        drawerLayout.closeDrawers();
    }

    //    @OnClick(R.id.sidebar_icon_check_app_updates)
    void onClick_sidebar_check_app_updates() {

        double latitude = AppLocationService.getInstance().latitude;
        double longitude = AppLocationService.getInstance().longitude;
        GeofencingService.getInstance().startGeofence1(latitude, longitude, 400);
        GeofencingService.getInstance().startGeofence2(latitude, longitude);
        /*new android.app.AlertDialog.Builder(this)
                .setTitle(_.i(R.string.firmware_recovery))
                .setCancelable(false)
                .setMessage(_.i(R.string.firmware_recovery_question))
                .setPositiveButton(_.i(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        drawerLayout.closeDrawers();

                        //Start DFU fragment, make LINKA parameter null because we don't initialize it
                        DfuManagerPageFragment fragment = DfuManagerPageFragment.newInstance(null);
                        fragment.blod_firmware_mode = true;
                        pushFragment(fragment);

                    }
                })
                .setNegativeButton("No", null)
                .show();


        */

        Intent intent = new Intent(AppMainActivity.this, WalkthroughActivity.class);
        startActivity(intent);

    }


    //    @OnClick(R.id.sidebar_icon_select_language)
    void onClick_sidebar_icon_select_language() {
        AppLanguagePickerActivity.createNewInstance(this, false);
    }

    //    @OnClick(R.id.sidebar_icon_testing)
    void onClick_sidebar_icon_testing() {
        drawerFragment = TestingFragment.newInstance();
        drawerLayout.closeDrawers();
    }

    @OnClick(R.id.sidebar_icon_help)
    void onHelpItemClicked() {
        drawerFragment = HelpFragment.newInstance();
        drawerLayout.closeDrawers();
    }

    @OnClick(R.id.sidebar_icon_settings)
    void onSettingsItemClicked() {
        drawerFragment = AppSettingsFragment.newInstance();
        drawerLayout.closeDrawers();
    }


    @OnClick(R.id.sidebar_icon_logout)
    void onClick_sidebar_logout() {
        tryLogout();
    }


    public void tryLogout() {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(R.string.logout_question)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.showLoading(AppMainActivity.this, root);
                        logout();
                    }
                })
                .show();
    }

    public void logout() {
        LinkaNotificationSettings.disconnect_all_linka();
        LinkaAPIServiceImpl.logout(AppMainActivity.this, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                Utils.cancelLoading();
                onLogout();
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                Utils.cancelLoading();
                onLogout();
            }
        });
    }


    void onLogout() {
        LinkaNotificationSettings.disconnect_all_linka();
        drawerLayout.closeDrawers();
        resetActivity();
    }


    @OnClick(R.id.item_add)
    void onClick_item_add() {
        if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof SetupLinka1)) {
            drawerFragment = SetupLinka1.newInstance();
//            setFragment(fragment);
        }
        drawerLayout.closeDrawers();
    }


    void refreshDevices() {

        /*
        List<LockController> lockControllerList = LocksController.getInstance().getLockControllers();
        int i = 0;
        for (final LockController lockController : lockControllerList) {
            TextView label = null;
            View item = null;
            View delete = null;
            switch (i) {
                case 0:
                    label = item1Text;
                    item = item1;
                    delete = item1Delete;
                    break;
                case 1:
                    label = item2Text;
                    item = item2;
                    delete = item2Delete;
                    break;
                case 2:
                    label = item3Text;
                    item = item3;
                    delete = item3Delete;
                    break;
                case 3:
                    label = item4Text;
                    item = item4;
                    delete = item4Delete;
                    break;
                default:
                    break;
            }
            if (item != null && label != null && delete != null) {
                label.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Linka linka = lockController.getLinka();
                        gotoLinka(linka);
                        drawerLayout.closeDrawers();
                    }
                });
                label.setText(lockController.getLinka().getName());

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Linka linka = lockController.getLinka();
                        new AlertDialog.Builder(AppMainActivity.this)
                                .setMessage(R.string.remove_device_instructions)
                                .setNegativeButton(R.string.dont_allow, null)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeLinka(linka);
                                        drawerLayout.closeDrawers();
                                    }
                                }).show();
                    }
                });
            }
            i += 1;
        }

        item1.setVisibility(View.VISIBLE);
        item2.setVisibility(View.VISIBLE);
        item3.setVisibility(View.VISIBLE);
        item4.setVisibility(View.VISIBLE);
        itemAdd.setVisibility(View.VISIBLE);


        if (i >= 4) {
            itemAdd.setVisibility(View.GONE);
        }
        if (i < 4) {
            item4.setVisibility(View.GONE);
        }
        if (i < 3) {
            item3.setVisibility(View.GONE);
        }
        if (i < 2) {
            item2.setVisibility(View.GONE);
        }
        if (i < 1) {
            item1.setVisibility(View.GONE);
        }
*/


        List<Linka> linkas = Linka.getLinkas();

        LocksController.getInstance().refresh();

        adapter = new LockListAdapter(AppMainActivity.this);
        adapter.setOnClickDeviceItemListener(new LockListAdapter.OnClickDeviceItemListener() {
            @Override
            public void onClickDeviceItem(final Linka linka, int position) {
                LogHelper.e("Linka List", "LINKA clicked " + linka.lock_mac_address + " " + linka.getName());

                gotoLinka(linka);

                return;
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(AppMainActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        if (adapter == null) return;
        adapter.setList(linkas);
        adapter.notifyDataSetChanged();

    }


    public void getLocks() {
        LogHelper.e("App Main Activity", "Getting Locks");
        LocksHelper.get_locks(AppMainActivity.this, new LocksHelper.LocksCallback() {
            @Override
            public void onNext() {
                resetActivity();
            }
        });
    }


    /* ON EVENT */

    @Subscribe
    public void onEvent(Object object) {
        if (object != null && object instanceof LogHelper) {
            LogHelper logHelper = (LogHelper) object;
            logHelpers.add(logHelper);
            if (logHelpers.size() > 24) {
                logHelpers.remove(0);
            }

            String text = "";
            for (LogHelper logHelper1 : logHelpers) {
                text += "\n" + logHelper1.prefix + " - " + logHelper1.suffix;
            }

            final String t = text;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    log.setText(t);
                }
            });
        }
    }

    List<LogHelper> logHelpers = new ArrayList<>();


    public CallbackManager callbackManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SetupLinka1.REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                EventBus.getDefault().post("GPSConnected");
            } else {
                EventBus.getDefault().post("GPSNotConnected");
            }
        }

        if (FacebookSdk.isFacebookRequestCode(requestCode)) {
            callbackManager.onActivityResult(requestCode, resultCode, data);

        }
        if (requestCode == AppLanguagePickerActivity.SET_LANGUAGE) {
            if (resultCode == AppLanguagePickerActivity.LANGUAGE_IS_SET) {
                initDrawer();
                initNavBar();

                resetActivity();
            }
        }

        // We only care about the lower 16 bits of this integer for the requestCode
        // So convert this int to a short int
        requestCode = (short) requestCode;

        if (data != null && callbackManager != null
                &&
                (
                        requestCode != 7458 && requestCode != 7459
                )
                ) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            callbackManager = null;
            return;
        }

    }


    /* GCM GOOGLE PLAY */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, _.i(R.string.not_support_google_play), Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

}

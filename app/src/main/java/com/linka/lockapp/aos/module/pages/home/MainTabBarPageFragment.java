package com.linka.lockapp.aos.module.pages.home;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.NotificationListAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.Notification;
import com.linka.lockapp.aos.module.pages.CircleView;
import com.linka.lockapp.aos.module.pages.notifications.NotificationsPageFragment;
import com.linka.lockapp.aos.module.pages.pac.SetPac3;
import com.linka.lockapp.aos.module.pages.settings.SettingsPageFragment;
import com.linka.lockapp.aos.module.pages.setup.AutoUpdateFragment;
import com.linka.lockapp.aos.module.pages.users.SharingPageFragment;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.linka.lockapp.aos.module.widget.ToggleSwipeableViewPager;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.goncalves.pugnotification.notification.PugNotification;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 30/3/16.
 */
public class MainTabBarPageFragment extends CoreFragment {
    private static final String SCREEN_ARGUMENT = "ScreenArgument";
    private static final String LINKA_ARGUMENT = "LinkaArgument";
    private static final String VIEW_PAGER_STATE = "ViewPagerState";
    private static final String CURRENT_POSITION = "CurrentPosition";
    private static final String LINKA_ID = "LinkaId";
    public static final String CLOSE_PAGES_IN_USERS_SCREEN = "ClosePagesInUsersScreen";
    public static final String UPDATE_NOTIFICATIONS = "UpdateNotifications";
    public static final String SELECTED_SCREEN = "Selected-";

    //Numbers of ViewPager screens
    public static final int LOCK_SCREEN = 0;
    public static final int USER_SCREEN = 1;
    public static final int NOTIFICATION_SCREEN = 2;
    public static final int SETTING_SCREEN = 3;

    @BindView(R.id.viewPager)
    ToggleSwipeableViewPager viewPager;

    @BindView(R.id.t1)
    LinearLayout t1;

    @BindView(R.id.t2)
    LinearLayout t2;

    @BindView(R.id.t3)
    ConstraintLayout t3;

    @BindView(R.id.t4)
    ConstraintLayout t4;

    @BindView(R.id.tab_bar)
    LinearLayout tabBar;

    @BindView(R.id.t1_img)
    ImageView t1Img;

    @BindView(R.id.t2_img)
    ImageView t2Img;

    @BindView(R.id.t3_img)
    ImageView t3Img;

    @BindView(R.id.t4_img)
    ImageView t4Img;

    @BindView(R.id.settings_update)
    TextView settingsUpdate;

    @BindView(R.id.notifications_update)
    TextView notificationsUpdate;

    public static int currentPosition = 0;
    private int newNotificationsCount = 0;
    private Linka linka;
    private Unbinder unbinder;
    private MainTabBarPageFragmentAdapter adapter;
    private LockController lockController;

    public boolean awaitsForLinkaSetPasscode = true;

    public static MainTabBarPageFragment newInstance(Linka linka, int screen) {
        Bundle bundle = new Bundle();
        MainTabBarPageFragment fragment = new MainTabBarPageFragment();
        bundle.putSerializable(LINKA_ARGUMENT, linka);
        bundle.putInt(SCREEN_ARGUMENT, screen);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_tabbar_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (getArguments() != null) {
            if (getArguments().get(LINKA_ARGUMENT) != null) {
                linka = (Linka) getArguments().getSerializable(LINKA_ARGUMENT);
                if (getArguments().getInt(SCREEN_ARGUMENT) != -1) {
                    currentPosition = getArguments().getInt(SCREEN_ARGUMENT);
                    getArguments().putInt(SCREEN_ARGUMENT, -1);
                }
                if (linka.getId() != null) {
                    linka = Linka.getLinkaById(linka.getId());
                }
                init(savedInstanceState);
            } else if (savedInstanceState != null && savedInstanceState.getLong(LINKA_ID, 0) != 0) {
                linka = Linka.getLinkaById(savedInstanceState.getLong(LINKA_ID, 0));
                init(savedInstanceState);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (linka != null) {
            LinkaAPIServiceImpl.get_lock_single(
                    getAppMainActivity(),
                    linka,
                    new Callback<LinkaAPIServiceResponse.LocksResponse>() {
                        @Override
                        public void onResponse(Call<LinkaAPIServiceResponse.LocksResponse> call, Response<LinkaAPIServiceResponse.LocksResponse> response) {
                            if (linka == null || !isAdded()) {
                                return;
                            }
                            EventBus.getDefault().post(LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED);
                        }

                        @Override
                        public void onFailure(Call<LinkaAPIServiceResponse.LocksResponse> call, Throwable t) {

                        }
                    });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        newNotificationsCount = 0;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null) {
            outState.putParcelable(VIEW_PAGER_STATE, viewPager.onSaveInstanceState());
        }
        if (linka != null) {
            outState.putLong(LINKA_ID, linka.getId());
        }
        outState.putInt(CURRENT_POSITION, currentPosition + 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager.clearOnPageChangeListeners();
        if (adapter != null) {
            adapter.f1 = null;
            adapter.f2 = null;
            adapter.f3 = null;
            adapter.f4 = null;
        }
        adapter = null;
        unbinder.unbind();
    }

    private void init(Bundle savedInstanceState) {
        checkNewNotifications();
        lockController = LocksController.getInstance().getLockController();

        if (savedInstanceState != null) {
            if (viewPager != null && savedInstanceState.getParcelable(VIEW_PAGER_STATE) != null) {
                viewPager.onRestoreInstanceState(savedInstanceState.getParcelable(VIEW_PAGER_STATE));
            }
            if (savedInstanceState.getInt(CURRENT_POSITION) != 0) {
                currentPosition = savedInstanceState.getInt(CURRENT_POSITION) - 1;
            }
        }

        viewPager.isSwipable = false;
        viewPager.setOffscreenPageLimit(3);

        // INIT VIEWPAGER
        if (linka != null) {
            if (adapter == null) {
                adapter = new MainTabBarPageFragmentAdapter(getChildFragmentManager(), linka);
            }
        }

        // INIT LINKA CONNECTION
        if (linka != null) {
            if (!lockController.getIsDeviceConnecting() && lockController.getIsDeviceDisconnected()) {
                Log.e("MainTabBarPage", "DoConnectDevice");
                lockController.doConnectDevice();
            }
        }

        if (adapter != null) {
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(currentPosition, false); //Circle view is default
            changeButtonsState(currentPosition);

            getAppMainActivity().onChangeFragment(getSelectedPageFragment(viewPager, adapter));

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    EventBus.getDefault().post(CLOSE_PAGES_IN_USERS_SCREEN);
                }

                @Override
                public void onPageSelected(int position) {
                    getAppMainActivity().onChangeFragment(getSelectedPageFragment(viewPager, adapter));

                    if (position != USER_SCREEN && getActivity().getSupportFragmentManager().findFragmentById(R.id.users_page_root) != null) {
                        getAppMainActivity().popFragment();
                    }

                    if (position == NOTIFICATION_SCREEN) {
                        if (SettingsPageFragment.currentFragment != SettingsPageFragment.NO_FRAGMENT) {
                            getAppMainActivity().setBackIconVisible(true);
                        }
                    } else {
                        getAppMainActivity().setBackIconVisible(false);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        EventBus.getDefault().post(SELECTED_SCREEN + String.valueOf(currentPosition));
                    }
                }
            });
        }
    }

    private void checkNewNotifications() {
        if (!isAdded()) return;

        if (linka == null) {
            return;
        }
        if (getAppMainActivity().isNetworkAvailable()) {
            LinkaAPIServiceImpl.fetch_activities(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.ActivitiesResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse.ActivitiesResponse> call, Response<LinkaAPIServiceResponse.ActivitiesResponse> response) {
                    if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity()) && notificationsUpdate != null) {
                        LinkaAPIServiceResponse.ActivitiesResponse body = response.body();
                        List<LinkaActivity> activities = new ArrayList<>();

                        if (body == null || body.data == null) {
                            return;
                        }

                        for (LinkaAPIServiceResponse.ActivitiesResponse.Data data : body.data) {
                            LinkaActivity activity = data.makeLinkaActivity(linka);
                            activities.add(activity);
                        }
                        LinkaActivity.saveAndOverwriteActivities(activities, linka);

                        List<LinkaActivity> act = LinkaActivity.getLinkaActivitiesByLinka(linka);
                        List<Notification> notifications = Notification.fromLinkaActivities(act);

                        for (Notification notification : notifications) {
                            if (!notification.isRead) {
                                newNotificationsCount++;
                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (newNotificationsCount != 0) {
                                        notificationsUpdate.setVisibility(View.VISIBLE);
                                        notificationsUpdate.setText(String.valueOf(newNotificationsCount));
                                    } else {
                                        notificationsUpdate.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse.ActivitiesResponse> call, Throwable t) {

                }
            });
        }
    }

    private Fragment getSelectedPageFragment(ViewPager viewPager, MainTabBarPageFragmentAdapter adapter) {
        if (viewPager == null || adapter == null) return null;
        int page = viewPager.getCurrentItem();
        return adapter.getItem(page);
    }

    private void changeButtonsState(int position) {
        t1Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_linka));
        t2Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_user));
        t3Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_notif));
        t4Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_setting));
        switch (position) {
            case LOCK_SCREEN:
                t1Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_linka_select));
                getAppMainActivity().setTitleNoUpperCase(linka.getName());
                break;
            case USER_SCREEN:
                t2Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_user_select));
                getAppMainActivity().setTitle(getString(R.string.users));
                break;
            case NOTIFICATION_SCREEN:
                t3Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_notif_select));
                getAppMainActivity().setTitle(getString(R.string.activities));
                break;
            case SETTING_SCREEN:
                t4Img.setImageDrawable(getResources().getDrawable(R.drawable.tab_setting_select));
                getAppMainActivity().setTitle(getString(R.string.big_settings));
                break;
        }
    }

    @OnClick(R.id.t1)
    void on_t1() {
        currentPosition = 0;
        viewPager.setCurrentItem(0, true);
        changeButtonsState(currentPosition);
    }

    @OnClick(R.id.t2)
    void on_t2() {
        currentPosition = 1;
        viewPager.setCurrentItem(1, true);
        changeButtonsState(currentPosition);
    }

    @OnClick(R.id.t3)
    void on_t3() {
        currentPosition = 2;
        viewPager.setCurrentItem(2, true);
        changeButtonsState(currentPosition);
    }

    @OnClick(R.id.t4)
    void on_t4() {
        //Refresh the settings page
        currentPosition = 3;
        viewPager.setCurrentItem(3, true);
        EventBus.getDefault().post(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE);
        changeButtonsState(currentPosition);
    }

    public void hideTabBar() {
        tabBar.setVisibility(View.INVISIBLE);
    }

    public void showTabBar() {
        tabBar.setVisibility(View.VISIBLE);
    }

    private void checkPacIsExisting() {
        final Linka _linka = Linka.getLinkaFromLockController();
        if (_linka == null) return;
        linka = _linka;

        if (_linka.isConnected && !linka.pacIsSet && lockController.hasReadPac) {
            if (awaitsForLinkaSetPasscode) {
                awaitsForLinkaSetPasscode = false;
                LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(linka);
                if (accessKey != null && accessKey.isAdmin()) {
                    new AlertDialog.Builder(getAppMainActivity())
                            .setMessage(R.string.setup_your_passcode_recommended)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getAppMainActivity().pushFragment(SetPac3.newInstance(_linka, SetPac3.SETTINGS));
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String pin_value;
                                    int digit_value;
                                    Random rand = new Random();

                                    //Find 4 digit PAC values that are valid
                                    do {
                                        pin_value = "";

                                        for (int i = 0; i < 4; i++) {
                                            digit_value = rand.nextInt(9) + 1;
                                            pin_value = pin_value + Integer.toString(digit_value);
                                        }

                                    } while (pin_value.equals("1234")
                                            || pin_value.equals("1111")
                                            || pin_value.equals("2222")
                                            || pin_value.equals("3333")
                                            || pin_value.equals("4444")
                                            || pin_value.equals("5555")
                                            || pin_value.equals("6666")
                                            || pin_value.equals("7777")
                                            || pin_value.equals("8888")
                                            || pin_value.equals("9999"));

                                    //Now set as the PAC in the Lock

                                    // set PAC in lock settings
                                    if (lockController.doSetPasscode(pin_value)) {

                                        linka.pac = Integer.parseInt(pin_value);
                                        LogHelper.e("Random Generated PAC", pin_value);
                                        linka.saveSettings();

                                        //Notify user of their new PAC
                                        new AlertDialog.Builder(getAppMainActivity())
                                                .setTitle(pin_value)
                                                .setMessage(_.i(R.string.random_code_set))
                                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        linka.pacIsSet = true;
                                                        linka.saveSettings();
                                                    }
                                                }).show();
                                    }

                                }
                            }).show();
                }
            }
        }
    }

    private void checkFirmwareUpdates() {
        // Make sure PAC is set before any attempts to update firmware
        // TODO: This is becoming spaghetti, need to be completely reworked in the future
        if (linka != null && lockController != null &&
                linka.isLockSettled && linka.pacIsSet) {
            if (lockController.lockControllerBundle != null) {
                String ver = lockController.lockControllerBundle.getFwVersionNumber();
                if (!ver.equals("")) {
//                            if (!ver.equals(AppDelegate.linkaMinRequiredFirmwareVersion) && !ver.equals("1.5.9") && AppDelegate.linkaMinRequiredFirmwareVersionIsCriticalUpdate) {
                    if (!ver.equals("2.0")) {
                        LogHelper.e("MainTabBarPageFrag", "FW version of " + ver + " does not equal " + AppDelegate.linkaMinRequiredFirmwareVersion);
                        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromLinka(linka);
                        if (accessKey != null && accessKey.isAdmin()) {
                            settingsUpdate.setVisibility(View.VISIBLE);
                            if (linka.canAlertCriticalFirmwareUpdate) {
                                linka.canAlertCriticalFirmwareUpdate = false;
                                Bundle args = new Bundle();
                                args.putBoolean(Constants.OPEN_SETTINGS, true);
                                PugNotification.with(AppDelegate.getInstance())
                                        .load()
                                        .autoCancel(true)
                                        .identifier(Constants.UPDATE_AVAILABLE_NOTIFICATION)
                                        .title("Update Available")
                                        .message(R.string.critical_firmware_update)
                                        .smallIcon(R.drawable.ic_action_name)
                                        .largeIcon(R.mipmap.ic_launcher)
                                        .flags(PendingIntent.FLAG_UPDATE_CURRENT)
                                        .click(AppMainActivity.class, args)
                                        .simple()
                                        .build();
                            }
                        } else {
                            settingsUpdate.setVisibility(View.GONE);
                        }
                    } else {
                        settingsUpdate.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void displayBlodPopup() {
        //To determine the number of times we can show the BLOD popup, we need a counter in prefs
        SharedPreferences.Editor edit = Prefs.edit();
        switch (Prefs.getInt("numberTimesDetectLinkaFuBlod", 0)) {
            case 0:

                new AlertDialog.Builder(getAppMainActivity())
                        .setTitle(R.string.oops)
                        .setMessage(R.string.blod_popup)
                        .setNegativeButton(R.string.blod_popup_no, null)
                        .setPositiveButton(R.string.blod_popup_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Start DFU fragment, make LINKA parameter null because we don't initialize it
//                                        DfuManagerPageFragment fragment = DfuManagerPageFragment.newInstance(null);
                                AutoUpdateFragment fragment = AutoUpdateFragment.newInstance(null, AutoUpdateFragment.SETTINGS);
                                fragment.blod_firmware_mode = true;
                                fragment.blod_firmware_try_again = true;
                                AppMainActivity.getInstance().pushFragment(fragment);
                            }
                        })
                        .show();

                edit.putInt("numberTimesDetectLinkaFuBlod", 1).commit();


                break;

            //The second time we have the popup, we tell them to contact LINKA Support
            case 1:
                new AlertDialog.Builder(getAppMainActivity())
                        .setTitle(R.string.oops)
                        .setMessage(R.string.blod_popup_contact_linka)
                        .setPositiveButton(R.string.ok, null)
                        .show();

                //We only show the contact linka support popup once. Subsequent times we don't show anything, so that it doesn't get annoying it they don't have a BLOD
                edit.putInt("numberTimesDetectLinkaFuBlod", 2).commit();
                break;
        }

        lockController.shouldDisplayBLODPopup = false;
    }

    @Subscribe
    public void onEvent(Object object) {
        if (object != null && object instanceof String) {
            if (object.equals(UPDATE_NOTIFICATIONS)) {
                if (currentPosition != NOTIFICATION_SCREEN) {
                    newNotificationsCount++;
                    notificationsUpdate.setVisibility(View.VISIBLE);
                    notificationsUpdate.setText(String.valueOf(newNotificationsCount));
                }
            }
            if (object.equals(NotificationListAdapter.UPDATE_NOTIFICATIONS_COUNT)) {
                newNotificationsCount = 0;
                notificationsUpdate.setText("");
                notificationsUpdate.setVisibility(View.GONE);
            }
            if (object.equals(LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED)) {
                if (linka.isLockSettled) {
                    checkPacIsExisting();
                    checkFirmwareUpdates();
                }
                //Check if there was a possible BLOD, and if so, display a popup
                //We decided that we can only show the solid blue popup once.
                //After it is shown once, then we show the message to contact LINKA Support
                if (lockController.shouldDisplayBLODPopup) {
                    displayBlodPopup();
                }
            }
        }
    }

    // DELETE FUNCTION BELOW ??
    // If the user declines to set the PAC
    // Set it to 1212
    private void setDefaultPasscode(final Linka linka) {
        LockController lockController = LocksController.getInstance().getLockController();
        if (lockController.doSetPasscode("1212")) {

            getAppMainActivity().popFragment();
            new AlertDialog.Builder(getAppMainActivity())
                    .setTitle(R.string.default_passcode_title)
                    .setMessage(R.string.default_passcode_desc)
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            linka.saveSettings();
                        }
                    })
                    .show();
        } else {
            new AlertDialog.Builder(getAppMainActivity())
                    .setTitle(_.i(R.string.fail_to_communicate))
                    .setMessage(_.i(R.string.check_connection))
                    .setNegativeButton(_.i(R.string.ok), null)
                    .show();
        }
    }

    private class MainTabBarPageFragmentAdapter extends FragmentPagerAdapter {
        CircleView f1;
        SharingPageFragment f2;
        NotificationsPageFragment f3;
        SettingsPageFragment f4;

        MainTabBarPageFragmentAdapter(FragmentManager fragmentManager, Linka linka) {
            super(fragmentManager);

            f1 = CircleView.newInstance(linka);
            f2 = SharingPageFragment.newInstance(linka);
            f3 = NotificationsPageFragment.newInstance(linka);// TODO
            f4 = SettingsPageFragment.newInstance(linka);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return f1;
                case 1:
                    return f2;
                case 2:
                    return f3;
                case 3:
                    return f4;
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
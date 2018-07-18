package com.linka.lockapp.aos.module.pages;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.Lock.BLE.BluetoothLEDevice;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.helpers.NotificationsHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.mylinkas.Circle;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LockGattUpdateReceiver;
import com.linka.lockapp.aos.module.widget.LocksController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifImageView;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED;

/**
 * Created by kyle on 2/19/18.
 */


public class CircleView extends CoreFragment {
    //This fragment position in viewpager of MainTabBarPageFragment
    private final int thisPage = 0;

    @BindView(R.id.circle)
    Circle circleView;

    @BindView(R.id.slide_to_lock)
    SwipeButton swipeButton;

    @BindView(R.id.battery_percent)
    TextView batteryPercent;

    @BindView(R.id.panic_button)
    ImageView panicButton;

    @BindView(R.id.root)
    FrameLayout root;

    @BindView(R.id.all_root)
    ConstraintLayout allRoot;

    @BindView(R.id.no_connection_img)
    GifImageView gifImageView;

    @BindView(R.id.warning_title)
    TextView warningTitle;

    @BindView(R.id.warning_text)
    TextView warningText;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback scanCallback;
    private Unbinder unbinder;

    private Linka linka;
    private LockController lockController;
    private View internetPage;

    private boolean isWarningShow = false;
    private boolean isTurningOnShow = false;

    private AlertDialog turningLinkaDialog = null;

    private Handler notSuccessLockHandler;
    private Runnable notSuccessLockRunnable = new Runnable() {
        @Override
        public void run() {
            notSuccessLockHandler = null;
            isWarningShow = false;
            gifImageView.setBackgroundResource(R.drawable.panic_button);
            gifImageView.setVisibility(View.GONE);
            panicButton.setVisibility(View.VISIBLE);
            warningTitle.setVisibility(View.GONE);
            warningText.setVisibility(View.GONE);
            refreshDisplay();
        }
    };

    private Handler turningOnLinkaHandler;
    private Runnable turningOnLinkaRunnable = new Runnable() {
        @Override
        public void run() {
            turningOnLinkaHandler = null;
            isTurningOnShow = false;
            showTurningOnLinkaDialog();
        }
    };

    private final BroadcastReceiver blueToothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        gifImageView.setVisibility(View.VISIBLE);
                        gifImageView.setImageResource(R.drawable.close_white_linka);
                        swipeButton.setCircleClickable(false);
                        panicButton.setBackground(getResources().getDrawable(R.drawable.panic_button));
                        turnOnBluetooth();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        refreshDisplay();
                        break;
                }

            }
        }
    };

    public static CircleView newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        CircleView fragment = new CircleView();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_circle_view, container, false);
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

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private boolean getInternetConnectivity() {
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        return connected;
    }

    private boolean getBluetoothConnectivity() {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.isEnabled();
    }


    void init() {
        internetPage = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_no_internet_connectivity, null);
        ((TextView) internetPage.findViewById(R.id.title)).setText(R.string.network_required_to_connect);

        lockController = LocksController.getInstance().getLockController();

        final MainTabBarPageFragment tabBarPageFragment = (MainTabBarPageFragment) getParentFragment();

        swipeButton.setSwipeCompleteListener(new SwipeButton.OnSwipeCompleteListener() {
            @Override
            public void clickStarted() {
                panicButton.setVisibility(View.GONE);

            }

            @Override
            public void clickCancelled() {
                panicButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void clickComplete() {
                if (tabBarPageFragment != null) {

                    tabBarPageFragment.hideTabBar();
                }
            }

            @Override
            public void swipeStarted() {
                if (tabBarPageFragment != null) {

                    tabBarPageFragment.hideTabBar();
                }
            }

            @Override
            public void swipeCancelled() {
                if (tabBarPageFragment != null) {
                    tabBarPageFragment.showTabBar();
                }

            }

            @Override
            public void onSwipeComplete(boolean swiped) {
                if (tabBarPageFragment != null) {
                    tabBarPageFragment.showTabBar();
                }

                if (lockController.getLinka().isUnlocked()) {
                    lockController.doLock();
                } else {
                    lockController.doUnlock();
                }
                panicButton.setVisibility(View.VISIBLE);
            }
        });

    }

    private void turnOnBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }


    @OnClick(R.id.panic_button)
    void onPanic() {
        lockController.doActionSiren();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(blueToothReceiver, filter1);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDisplay();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(blueToothReceiver);
        if (notSuccessLockHandler != null) {
            isWarningShow = false;
            notSuccessLockHandler.removeCallbacks(notSuccessLockRunnable);
            notSuccessLockHandler = null;
            gifImageView.setBackgroundResource(R.drawable.panic_button);
            gifImageView.setVisibility(View.GONE);
            panicButton.setVisibility(View.VISIBLE);
            warningTitle.setVisibility(View.GONE);
            warningText.setVisibility(View.GONE);
            refreshDisplay();
        }
        if (turningOnLinkaHandler != null) {
            turningOnLinkaHandler.removeCallbacks(turningOnLinkaRunnable);
            turningOnLinkaHandler = null;
            isTurningOnShow = false;
        }
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(scanCallback);
        }
        if (turningLinkaDialog != null && turningLinkaDialog.isShowing()) {
            turningLinkaDialog.dismiss();
        }
    }

    public void refreshDisplay() {
        if (!isAdded()) return;

        if (getAppMainActivity() != null) {
            getAppMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!getInternetConnectivity()) {
                        root.removeView(internetPage);
                        if (internetPage.getParent() == null) {
                            root.addView(internetPage);
                        }
                        root.setBackground(getResources().getDrawable(R.drawable.blue_gradient));
                        batteryPercent.setText("");
                    } else if (!getBluetoothConnectivity()) {
                        setLockNotConnectedState();
                        turnOnBluetooth();
                    } else {
                        root.setBackgroundColor(getResources().getColor(R.color.linka_transparent));
                        if (!isWarningShow) {
                            if (!linka.isConnected) {
                                if (!isTurningOnShow) {
                                    root.removeView(internetPage);
                                    setLockNotConnectedState();
                                    showTurningOnLinkaDialog();
                                }
                            } else {
                                isTurningOnShow = false;
                                if (turningOnLinkaHandler != null) {
                                    turningOnLinkaHandler.removeCallbacks(turningOnLinkaRunnable);
                                }
                                if (turningLinkaDialog != null && turningLinkaDialog.isShowing()) {
                                    turningLinkaDialog.dismiss();
                                }
                                if (linka.isLockSettled) {
                                    setLockSettledState();
                                } else {
                                    root.removeView(internetPage);
                                    if (gifImageView.getVisibility() != View.VISIBLE ||
                                            gifImageView.getDrawable().equals(getResources().getDrawable(R.drawable.wi_fi_connection))) {
                                        gifImageView.setVisibility(View.VISIBLE);
                                        gifImageView.setImageResource(R.drawable.wi_fi_connection);
                                    }
                                    swipeButton.setCircleClickable(false);
                                    panicButton.setBackground(getResources().getDrawable(R.drawable.panic_button));
                                }
                            }
                        }
                    }
                    LocksController.getInstance().refresh();
                }
            });
        }
    }

    private void setLockNotConnectedState() {
        batteryPercent.setText("");
        gifImageView.setVisibility(View.VISIBLE);
        gifImageView.setImageResource(R.drawable.close_white_linka);
        swipeButton.setCircleClickable(false);
        panicButton.setBackground(getResources().getDrawable(R.drawable.panic_button));
    }

    private void setLockSettledState() {
        batteryPercent.setText(linka.batteryPercent + "%");
        root.removeView(internetPage);
        gifImageView.setVisibility(View.GONE);
        panicButton.setBackground(getResources().getDrawable(R.drawable.panic_red_button));
        if (linka.isLocked) {
            swipeButton.setCurrentState(Circle.LOCKED_STATE);
            swipeButton.setCircleClickable(true);
        } else if (linka.isUnlocked) {
            swipeButton.setCurrentState(Circle.UNLOCKED_STATE);
            swipeButton.setCircleClickable(true);
        } else if (linka.isLocking) {
            swipeButton.setCircleClickable(false);
        } else if (linka.isUnlocking) {
            swipeButton.setCircleClickable(false);
        }
    }

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;
        if (object instanceof String && object.equals(LOCKSCONTROLLER_NOTIFY_REFRESHED)) {

            linka = Linka.getLinkaFromLockController(linka);

            if (MainTabBarPageFragment.currentPosition == MainTabBarPageFragment.LOCK_SCREEN) {
                refreshDisplay();
            }

        } else if (object != null && object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {

            linka = Linka.getLinkaFromLockController(linka);

            if (MainTabBarPageFragment.currentPosition == MainTabBarPageFragment.LOCK_SCREEN) {
                refreshDisplay();
            }
        } else if (object instanceof String && ((String) object).substring(0, 8).equals("Selected")) {
            if (object.equals("Selected-" + String.valueOf(MainTabBarPageFragment.LOCK_SCREEN))) {
                init();
                refreshDisplay();
            }
        } else if (object != null && object.equals(LockGattUpdateReceiver.GATT_UPDATE_RECEIVER_NOTIFY_DISCONNECTED)) {
            LogHelper.e("MyLinkasPageFrag", "[EVENTBUS] GATT DISCONNECT Notified");

            linka = Linka.getLinkaFromLockController(linka);
            if (MainTabBarPageFragment.currentPosition == MainTabBarPageFragment.LOCK_SCREEN) {
                refreshDisplay();
            }
        } else if (object instanceof String && ((String) object).substring(0, 8).equals("Selected")) {
            if (object.equals("Selected-" + String.valueOf(MainTabBarPageFragment.LOCK_SCREEN))) {
                refreshDisplay();
            }
        } else if (object != null && object.equals(NotificationsHelper.LINKA_NOT_LOCKED)) {
            isWarningShow = true;
            swipeButton.setCircleClickable(false);
            panicButton.setVisibility(View.GONE);
            gifImageView.setVisibility(View.VISIBLE);
            gifImageView.setBackgroundResource(R.drawable.danger_red_back);
            gifImageView.setImageResource(R.drawable.close_white_linka);
            warningTitle.setVisibility(View.VISIBLE);
            warningText.setVisibility(View.VISIBLE);
            notSuccessLockHandler = new Handler();
            notSuccessLockHandler.postDelayed(notSuccessLockRunnable, 5000);
        }
    }

    private void showTurningOnLinkaDialog() {
        if (MainTabBarPageFragment.currentPosition == thisPage) {
            scanCallback = null;
            devices.clear();
            linkaList.clear();
            LinkaNotificationSettings.refresh_for_latest_linka();
            LocksController.getInstance().refresh();
            if (!AppBluetoothService.getInstance().dfu) {
                AppBluetoothService.getInstance().enableFixedTimeScanning(true);
            }
            if (!isTurningOnShow) {
                gifImageView.setImageResource(R.drawable.close_white_linka);
                isTurningOnShow = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Turn on lock").
                        setMessage("Press the lock's POWER button to connect.").
                        setCancelable(false).
                        setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gifImageView.setVisibility(View.VISIBLE);
                                gifImageView.setImageResource(R.drawable.wi_fi_connection);
                                dialog.dismiss();
                                turningOnLinkaHandler = new Handler();
                                turningOnLinkaHandler.postDelayed(turningOnLinkaRunnable, 20000);
                                scanLeDevice();
                            }
                        });
                turningLinkaDialog = builder.create();
                turningLinkaDialog.show();
            }
        }
    }

    List<Linka> linkaList = new ArrayList<>();
    List<BluetoothLEDevice> devices = new ArrayList<>();

    private void initializeScanCallback() {
        scanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                if (device != null) {
                    LogHelper.e("SCAN", "Got Device " + device.getName() + device.getAddress());
                } else {
                    return;
                }
                int result = BLEHelpers.upsertBluetoothLEDeviceList(devices, linkaList, device, rssi, scanRecord);
                if (result == 0) {
                    if (!linkaList.isEmpty()) {
                        if (bluetoothAdapter == null) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                            bluetoothAdapter = bluetoothManager.getAdapter();
                        }
                        bluetoothAdapter.stopLeScan(this);
                        scanCallback = null;
                        bluetoothAdapter = null;
                    }
                } else if (result == 1) {
                    if (!linkaList.isEmpty()) {
                        if (bluetoothAdapter == null) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                            bluetoothAdapter = bluetoothManager.getAdapter();
                        }
                        bluetoothAdapter.stopLeScan(scanCallback);
                        scanCallback = null;
                        bluetoothAdapter = null;
                    }
                }
            }
        };
    }

    void scanLeDevice() {
        if (bluetoothAdapter == null) return;

        initializeScanCallback();
        if (bluetoothAdapter == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        bluetoothAdapter.startLeScan(scanCallback);
    }

}

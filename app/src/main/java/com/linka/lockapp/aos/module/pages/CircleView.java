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
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.Lock.BLE.BluetoothLEDevice;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.NotificationsHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
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
import butterknife.OnTouch;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifImageView;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED;

/**
 * Created by kyle on 2/19/18.
 */


public class CircleView extends CoreFragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.slide_to_lock)
    SwipeButton swipeButton;

    @BindView(R.id.battery_percent)
    TextView batteryPercent;

    @BindView(R.id.battery_image)
    ImageView batteryImage;

    @BindView(R.id.swipe_text)
    TextView swipeText;

    @BindView(R.id.panic_button)
    ImageView panicButton;

    @BindView(R.id.sleep_button)
    ImageView sleepButton;

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
    private boolean isPanicEnabled = false;
    private boolean isRefreshAvailable = true;
    private boolean isLockConnected = false;
    private boolean isPanicAndSleepEnabled = false;

    private AlphaAnimation animation = new AlphaAnimation(1.0f, 0.2f);

    private Handler notSuccessLockHandler;
    private Runnable notSuccessLockRunnable = new Runnable() {
        @Override
        public void run() {
            notSuccessLockHandler = null;
            isWarningShow = false;
            gifImageView.setVisibility(View.GONE);
            gifImageView.setImageResource(R.drawable.wi_fi_connection);
            gifImageView.setBackgroundResource(R.drawable.panic_button);
            setPanicAndSleepButtonsVisibility(View.VISIBLE);
            warningTitle.setVisibility(View.GONE);
            warningText.setVisibility(View.GONE);
            refreshDisplay();
        }
    };

    private Handler bluetoothHandler = null;
    private Runnable bluetoothRunnable = new Runnable() {
        @Override
        public void run() {
            isRefreshAvailable = true;
            turnOnBluetooth();
            setLockNotConnect();
            bluetoothHandler = null;
        }
    };

    private Handler scanHandler = null;
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            lockController.doConnectDevice();
            scanLeDevice();
            scanHandler.postDelayed(scanRunnable, 15 * 1000);
        }
    };

    private Handler refreshHandler;
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            isRefreshAvailable = true;
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
                        if (bluetoothHandler == null) {
                            isRefreshAvailable = false;
                            setLockNotConnectedState();
                            bluetoothHandler = new Handler();
                            bluetoothHandler.postDelayed(bluetoothRunnable, 1000);
                            lockController.doDisconnectDevice();
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        lockController.doConnectDevice();
                        refreshDisplay();
                        break;
                }

            }
        }
    };

    public static CircleView newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        CircleView fragment = new CircleView();
        bundle.putSerializable(LINKA_ARGUMENT, linka);
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
        if (getArguments().get(LINKA_ARGUMENT) != null) {
            linka = (Linka) getArguments().getSerializable(LINKA_ARGUMENT);
            init();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(blueToothReceiver, filter1);
        EventBus.getDefault().register(this);
        isRefreshAvailable = true;
        if (!linka.isConnected) {
            scanHandler = new Handler();
            scanHandler.postDelayed(scanRunnable, 3000);
        }
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
            gifImageView.setVisibility(View.GONE);
            setPanicAndSleepButtonsVisibility(View.VISIBLE);
            warningTitle.setVisibility(View.GONE);
            warningText.setVisibility(View.GONE);
            refreshDisplay();
        }
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(scanCallback);
        }
        removeCallbacks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init() {
        batteryImage.setColorFilter(getActivity().getResources().getColor(R.color.linka_gray), PorterDuff.Mode.SRC_IN);
        internetPage = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_no_internet_connectivity, null);
        setPanicAndSleepButtonsState(false);
        ((TextView) internetPage.findViewById(R.id.title)).setText(R.string.network_required_to_connect);

        lockController = LocksController.getInstance().getLockController();
        isPanicEnabled = linka.tamperStatus;

        final MainTabBarPageFragment tabBarPageFragment = (MainTabBarPageFragment) getParentFragment();

        swipeButton.setSwipeCompleteListener(new SwipeButton.OnSwipeCompleteListener() {
            @Override
            public void clickStarted() {

            }

            @Override
            public void clickCancelled() {
                setPanicAndSleepButtonsVisibility(View.VISIBLE);
            }

            @Override
            public void clickComplete() {
                if (tabBarPageFragment != null) {

                    tabBarPageFragment.hideTabBar();
                    setPanicAndSleepButtonsVisibility(View.GONE);
                }
            }

            @Override
            public void swipeStarted() {
                if (tabBarPageFragment != null) {

                    tabBarPageFragment.hideTabBar();
                    setPanicAndSleepButtonsVisibility(View.GONE);
                }
            }

            @Override
            public void swipeCancelled() {
                if (tabBarPageFragment != null) {
                    tabBarPageFragment.showTabBar();
                }
                if (!linka.isConnected) {
                    setLockNotConnect();
                    setLockNotConnectedState();
                }

            }

            @Override
            public void onSwipeComplete(boolean swiped) {
                if (tabBarPageFragment != null) {
                    tabBarPageFragment.showTabBar();
                }
                isRefreshAvailable = false;
                if (refreshHandler == null) {
                    refreshHandler = new Handler();
                }
                refreshHandler.postDelayed(refreshRunnable, 1000);

                if (lockController.getLinka().isUnlocked()) {
                    lockController.doLock();
                } else {
                    lockController.doUnlock();
                }
                setPanicAndSleepButtonsVisibility(View.VISIBLE);
            }
        });

    }

    public void refreshDisplay() {
        if (!isAdded()) return;

        if (isRefreshAvailable) {
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
                            if (bluetoothHandler == null) {
                                isRefreshAvailable = false;
                                setLockNotConnectedState();
                                bluetoothHandler = new Handler();
                                bluetoothHandler.postDelayed(bluetoothRunnable, 1000);
                                lockController.doDisconnectDevice();
                            }
                        } else {
                            root.setBackgroundColor(getResources().getColor(R.color.linka_transparent));
                            if (!isWarningShow) {
                                if (!linka.isConnected) {
                                    setLockNotConnect();
                                } else {
                                    setLockConnect();
                                }
                            }
                        }
                    }
                });
            }
            isRefreshAvailable = false;
            if (refreshHandler == null) {
                refreshHandler = new Handler();
            }
            refreshHandler.postDelayed(refreshRunnable, 700);
        }
    }

    private boolean getInternetConnectivity() {
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = connectivityManager != null &&
                (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }

    private boolean getBluetoothConnectivity() {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            return bluetoothAdapter.isEnabled();
        }else {
            return false;
        }
    }

    private void turnOnBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void removeCallbacks() {
        if (refreshHandler != null) {
            refreshHandler = null;
            isRefreshAvailable = true;
        }
        if (scanHandler != null) {
            scanHandler.removeCallbacks(scanRunnable);
            scanHandler = null;
        }
        if (bluetoothHandler != null) {
            bluetoothHandler.removeCallbacks(bluetoothRunnable);
            bluetoothHandler = null;
        }
    }

    private void setLockNotConnect() {
        root.removeView(internetPage);
        if (scanHandler == null) {
            scanHandler = new Handler();
            scanHandler.postDelayed(scanRunnable, 3000);
        }
        if (isLockConnected || swipeButton.getCurrentState() != Circle.NO_CONNECTION_STATE) {
            setLockNotConnectedState();
        }
    }

    private void setLockNotConnectedState() {
        if (batteryImage != null) {
            batteryImage.setColorFilter(getActivity().getResources().getColor(R.color.linka_gray), PorterDuff.Mode.SRC_IN);
            batteryPercent.setText("");
            swipeText.setText(getString(R.string.asleep_or_out));
            gifImageView.setVisibility(View.GONE);
            swipeButton.setCurrentState(Circle.NO_CONNECTION_STATE);
            swipeButton.setCircleClickable(false);
            if (isPanicAndSleepEnabled) {
                setPanicAndSleepButtonsState(false);
            }
            isLockConnected = false;
        }
    }

    private void setLockConnect() {
        if (scanCallback != null) {
            bluetoothAdapter.stopLeScan(scanCallback);
            scanCallback = null;
        }
        if (scanHandler != null) {
            scanHandler.removeCallbacks(scanRunnable);
            scanHandler = null;
        }
        if (linka.isLockSettled) {
            setLockSettledState();
        } else {
            isLockConnected = false;
            root.removeView(internetPage);
            if (gifImageView.getVisibility() != View.VISIBLE ||
                    gifImageView.getDrawable().equals(getResources().getDrawable(R.drawable.wi_fi_connection))) {
                gifImageView.setVisibility(View.VISIBLE);
            }
            swipeButton.setCircleClickable(false);
            if (isPanicAndSleepEnabled) {
                setPanicAndSleepButtonsState(false);
            }
        }
    }

    private void setLockSettledState() {
        if (!isPanicAndSleepEnabled) {
            setPanicAndSleepButtonsState(true);
            batteryImage.setColorFilter(null);
            batteryPercent.setText(linka.batteryPercent + "%");
        }
        if (!isLockConnected) {
            root.removeView(internetPage);
            gifImageView.setVisibility(View.GONE);
            isLockConnected = true;
        }
        if(linka.tamperStatus){
            if(!isPanicEnabled){
                panicStarted();
            }
        }else if(isPanicEnabled){
            panicEnded();
        }
        if (linka.isLocked) {
            swipeText.setText(getString(R.string.press_to_unlock));
            swipeButton.setCurrentState(Circle.LOCKED_STATE);
            swipeButton.setCircleClickable(true);
        } else if (linka.isUnlocked) {
            swipeText.setText(getString(R.string.press_to_lock));
            swipeButton.setCurrentState(Circle.UNLOCKED_STATE);
            swipeButton.setCircleClickable(true);
        } else if (linka.isLocking) {
            swipeText.setText(getString(R.string.locking));
            swipeButton.setCurrentState(Circle.LOCKING_STATE);
            swipeButton.setCircleClickable(false);
        } else if (linka.isUnlocking) {
            swipeText.setText(getString(R.string.unlocking));
            swipeButton.setCurrentState(Circle.UNLOCKING_STATE);
            swipeButton.setCircleClickable(false);
        }
    }

    private void setDeviceNotLockedSuccessState() {
        swipeText.setText(getString(R.string.press_to_lock));
        isWarningShow = true;
        swipeButton.setCircleClickable(false);
        setPanicAndSleepButtonsVisibility(View.GONE);
        gifImageView.setVisibility(View.VISIBLE);
        gifImageView.setBackgroundResource(R.drawable.danger_red_back);
        gifImageView.setImageResource(R.drawable.close_white_linka);
        warningTitle.setVisibility(View.VISIBLE);
        warningText.setVisibility(View.VISIBLE);
        notSuccessLockHandler = new Handler();
        notSuccessLockHandler.postDelayed(notSuccessLockRunnable, 5000);
    }

    private void setPanicAndSleepButtonsVisibility(int visibility) {
        if (panicButton.getAnimation() != null) {
            panicButton.clearAnimation();
            panicButton.setBackground(getResources().getDrawable(R.drawable.panic_blue_button));
        }
        swipeText.setVisibility(visibility);
        panicButton.setVisibility(visibility);
        sleepButton.setVisibility(visibility);
    }

    private void setPanicAndSleepButtonsState(boolean enable) {
        if (panicButton.getAnimation() != null) {
            panicButton.clearAnimation();
            panicButton.setBackground(getResources().getDrawable(R.drawable.panic_blue_button));
        }
        panicButton.setClickable(enable);
        sleepButton.setClickable(enable);
        isPanicAndSleepEnabled = enable;
        if (enable) {
            panicButton.setBackground(getResources().getDrawable(R.drawable.panic_blue_button));
            sleepButton.setBackground(getResources().getDrawable(R.drawable.panic_blue_button));
            panicButton.setColorFilter(getActivity().getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
            sleepButton.setColorFilter(getActivity().getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        } else {
            panicButton.setBackground(getResources().getDrawable(R.drawable.panic_button));
            sleepButton.setBackground(getResources().getDrawable(R.drawable.panic_button));
            panicButton.setColorFilter(getActivity().getResources().getColor(R.color.panic_gray_color), PorterDuff.Mode.SRC_IN);
            sleepButton.setColorFilter(getActivity().getResources().getColor(R.color.panic_gray_color), PorterDuff.Mode.SRC_IN);
        }
    }

    private List<Linka> linkaList = new ArrayList<>();
    private List<BluetoothLEDevice> devices = new ArrayList<>();

    private void initializeScanCallback() {
        scanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                if (device == null) {
                    return;
                }
                int result = BLEHelpers.upsertBluetoothLEDeviceList(devices, linkaList, device, rssi, scanRecord);
                if (result == 0) {
                    if (!linkaList.isEmpty()) {
                        if (bluetoothAdapter == null) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                            if(bluetoothManager != null) {
                                bluetoothAdapter = bluetoothManager.getAdapter();
                            }
                        }
                        bluetoothAdapter.stopLeScan(this);
                        scanCallback = null;
                        bluetoothAdapter = null;
                        if (scanHandler != null) {
                            scanHandler.removeCallbacks(scanRunnable);
                            scanHandler = null;
                        }
                    }
                } else if (result == 1) {
                    if (!linkaList.isEmpty()) {
                        if (bluetoothAdapter == null && getActivity() != null) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                            if (bluetoothManager != null) {
                                bluetoothAdapter = bluetoothManager.getAdapter();
                            }
                        }
                        if (bluetoothAdapter != null) {
                            bluetoothAdapter.stopLeScan(scanCallback);
                        }
                        scanCallback = null;
                        bluetoothAdapter = null;
                        if (scanHandler != null) {
                            scanHandler.removeCallbacks(scanRunnable);
                            scanHandler = null;
                        }
                    }
                }
            }
        };
    }

    private void scanLeDevice() {
        if (bluetoothAdapter == null) return;

        if (scanCallback != null) {
            bluetoothAdapter.stopLeScan(scanCallback);
            scanCallback = null;
        }
        initializeScanCallback();
        if (bluetoothAdapter == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager != null) {
                bluetoothManager.getAdapter().startLeScan(scanCallback);
            }
        }
    }

    private void panicStarted(){
        isPanicEnabled = true;
        panicButton.setBackground(getResources().getDrawable(R.drawable.panic_red_button));
        panicButton.setColorFilter(getActivity().getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        animation.setDuration(700);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        panicButton.startAnimation(animation);
    }

    private void panicEnded(){
        isPanicEnabled = false;
        panicButton.clearAnimation();
        panicButton.setBackground(getResources().getDrawable(R.drawable.panic_blue_button));
    }

    @OnTouch(R.id.panic_button)
    boolean onPanicTouch(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isPanicEnabled) {
                panicButton.setColorFilter(getActivity().getResources().getColor(R.color.panic_gray_color), PorterDuff.Mode.SRC_IN);
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (!isPanicEnabled) {
                lockController.doActionSiren();
                panicButton.setColorFilter(getActivity().getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
            } else {
                lockController.doStopActionSiren();
            }
        }
        return false;
    }

    @OnTouch(R.id.sleep_button)
    boolean onSleepTouch(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            sleepButton.setColorFilter(getActivity().getResources().getColor(R.color.panic_gray_color), PorterDuff.Mode.SRC_IN);
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            sleepButton.setColorFilter(getActivity().getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
            new AlertDialog.Builder(getActivity()).
                    setTitle("Put your lock to sleep?").
                    setMessage("Push the power button on your lock to turn it back on.").
                    setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Sleep", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            lockController.doSleep();
                            isRefreshAvailable = false;
                            setLockNotConnectedState();
                            refreshHandler.removeCallbacks(refreshRunnable);
                            refreshHandler.postDelayed(refreshRunnable, 5000);
                        }
                    })
                    .create().show();
        }
        return false;
    }

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;
        if(object != null && object instanceof String){
            if (object.equals(LOCKSCONTROLLER_NOTIFY_REFRESHED) ||
                    object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE) ||
                    object.equals(LockGattUpdateReceiver.GATT_UPDATE_RECEIVER_NOTIFY_DISCONNECTED)) {

                linka = Linka.getLinkaFromLockController();
                if (MainTabBarPageFragment.currentPosition == MainTabBarPageFragment.LOCK_SCREEN) {
                    refreshDisplay();
                }
            } else if (((String) object).substring(0, 9).equals(MainTabBarPageFragment.SELECTED_SCREEN)) {
                if (object.equals(MainTabBarPageFragment.SELECTED_SCREEN + String.valueOf(MainTabBarPageFragment.LOCK_SCREEN))) {
                    refreshDisplay();
                    if (!linka.isConnected) {
                        if (scanHandler != null) {
                            scanHandler.removeCallbacks(scanRunnable);
                            scanHandler = null;
                        }
                        scanHandler = new Handler();
                        scanHandler.postDelayed(scanRunnable, 1500);
                    }
                }
            } else  if (object.equals(NotificationsHelper.LINKA_NOT_LOCKED)) {
                setDeviceNotLockedSuccessState();
            }
        }
    }
}

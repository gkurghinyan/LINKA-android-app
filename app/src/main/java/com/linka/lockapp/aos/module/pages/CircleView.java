package com.linka.lockapp.aos.module.pages;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.mylinkas.Circle;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LockGattUpdateReceiver;
import com.linka.lockapp.aos.module.widget.LocksController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import jp.wasabeef.blurry.Blurry;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED;

/**
 * Created by kyle on 2/19/18.
 */


public class CircleView extends CoreFragment {


    @BindView(R.id.circle)
    Circle circleView;

    @BindView(R.id.circle_background)
    Circle circleViewBackground;

    @BindView(R.id.slide_to_lock)
    SwipeButton swipeButton;
    @BindView(R.id.status_text)
    TextView statusText;
    @BindView(R.id.battery_percent)
    TextView batteryPercent;
    @BindView(R.id.panic_button)
    ImageView panicButton;
    @BindView(R.id.root)
    FrameLayout root;
    @BindView(R.id.all_root)
    FrameLayout allRoot;

    private BluetoothAdapter bluetoothAdapter;
    private Unbinder unbinder;

    private Linka linka;
    private LockController lockController;
    private View internetPage;
//    View connectivityPage;

    private final BroadcastReceiver blueToothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        setBlur(true, getString(R.string.enabling_bluetooth));
                        break;
                    case BluetoothAdapter.STATE_ON:
                        setBlur(false,null);
                        break;
                }

            }
        }
    };

    // @InjectView(R.id.row_audible_locking_unlocking)
    //LinearLayout rowAudibleLockingUnlocking;

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
        internetPage = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_no_internet_connectivity, null);
        ((TextView) internetPage.findViewById(R.id.title)).setText(R.string.network_required_to_connect);
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

            init();
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

        if (!getInternetConnectivity()) {
            root.removeView(internetPage);
            if (internetPage.getParent() == null) {
                root.addView(internetPage);
            }
            root.setBackground(getResources().getDrawable(R.drawable.blue_gradient));
        } else if (!getBluetoothConnectivity()) {
            turnOnBluetooth();
        } else if (!linka.isConnected && !linka.isLockSettled) {
            statusText.setText("Turn On Linka");
        }

        lockController = LocksController.getInstance().getLockController();

        circleViewBackground.setColor(Color.parseColor("#0878ce"));
        circleViewBackground.setAngle(365);

        circleView.setVisibility(View.INVISIBLE);

        final MainTabBarPageFragment tabBarPageFragment = (MainTabBarPageFragment) getParentFragment();

        swipeButton.setSwipeCompleteListener(new SwipeButton.OnSwipeCompleteListener() {
            @Override
            public void clickStarted() {

                circleViewBackground.setColor(Color.parseColor("#32c967"));
                circleViewBackground.invalidate(); //This will cause the circle to update

                circleView.setVisibility(View.VISIBLE);
            }

            @Override
            public void clickCancelled() {

                circleViewBackground.setColor(Color.parseColor("#0878ce"));
                circleViewBackground.invalidate(); //This will cause the circle to update

                circleView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void clickComplete() {
                if (tabBarPageFragment != null) {

                    tabBarPageFragment.hideTabBar();
                    circleViewBackground.setColor(Color.parseColor("#0878ce"));
                    circleViewBackground.invalidate(); //This will cause the circle to update

                    circleView.setVisibility(View.INVISIBLE);
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
            }
        });

        batteryPercent.setText(linka.batteryPercent + "%");

    }

    private void turnOnBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    ThreeDotsDialogFragment threeDotsDialogFragment;

    public void setBlur(boolean isBlur, String text) {
        if (isBlur) {
            if(threeDotsDialogFragment == null) {
                root.setBackground(getResources().getDrawable(R.drawable.blue_gradient));
                Blurry.with(getContext()).radius(25).sampling(2).onto(allRoot);
                threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance().setConnectingText(true, text);
                threeDotsDialogFragment.show(getFragmentManager(), null);
            }
        } else {
            Blurry.delete(allRoot);
            if (threeDotsDialogFragment != null) {
                threeDotsDialogFragment.dismiss();
                threeDotsDialogFragment = null;
            }
        }
    }

    @OnTouch(R.id.circle)
    public boolean clickCircle(Circle circle, MotionEvent event) {

        return false;
    }

    Runnable showSlider = new Runnable() {
        @Override
        public void run() {

        }
    };


    @OnClick(R.id.panic_button)
    void onPanic() {
        lockController.doActionSiren();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(blueToothReceiver, filter1);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(blueToothReceiver);
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
                    } else if (!getBluetoothConnectivity()) {
                        setBlur(true, getString(R.string.enabling_bluetooth));
                        turnOnBluetooth();
                    } else {
                        root.setBackgroundColor(getResources().getColor(R.color.linka_transparent));
                        if (!linka.isConnected) {
                            root.removeView(internetPage);
                            statusText.setText("Turn On Linka");
                        } else if (linka.isLockSettled) {
                            root.removeView(internetPage);
                            if (linka.isLocked) {
                                statusText.setText("Hold to Unlock");
                            } else if (linka.isUnlocked) {
                                statusText.setText("Hold to Lock");
                            } else if (linka.isLocking) {
                                statusText.setText("Locking ...");
                            } else if (linka.isUnlocking) {
                                statusText.setText("Unlocking ...");
                            }
                        }
                    }
                }
            });
        }
    }

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;
        if (object instanceof String && ((String) object).equals(LOCKSCONTROLLER_NOTIFY_REFRESHED)) {

            linka = Linka.getLinkaFromLockController(linka);

            refreshDisplay();

        } else if (object != null && object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {

            linka = Linka.getLinkaFromLockController(linka);

            refreshDisplay();
        } else if (object != null && object.equals(LockGattUpdateReceiver.GATT_UPDATE_RECEIVER_NOTIFY_DISCONNECTED)) {
            LogHelper.e("MyLinkasPageFrag", "[EVENTBUS] GATT DISCONNECT Notified");

            linka = Linka.getLinkaFromLockController(linka);
            refreshDisplay();
        }
    }

}

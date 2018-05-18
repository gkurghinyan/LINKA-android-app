package com.linka.lockapp.aos.module.pages;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.mylinkas.Circle;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LockGattUpdateReceiver;
import com.linka.lockapp.aos.module.widget.LocksController;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

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

    BluetoothAdapter bluetoothAdapter;
    Unbinder unbinder;

    Linka linka;
    LockController lockController;

    CircleAngleAnimation animation;
    private Rect rect;

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


    void init() {

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
                if(tabBarPageFragment != null){

                    tabBarPageFragment.hideTabBar();
                    circleViewBackground.setColor(Color.parseColor("#0878ce"));
                    circleViewBackground.invalidate(); //This will cause the circle to update

                    circleView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void swipeStarted() {
                if(tabBarPageFragment != null){

                    tabBarPageFragment.hideTabBar();
                }
            }

            @Override
            public void swipeCancelled() {
                if(tabBarPageFragment != null){
                    tabBarPageFragment.showTabBar();
                }

            }

            @Override
            public void onSwipeComplete(boolean swiped) {
                if(tabBarPageFragment != null){
                    tabBarPageFragment.showTabBar();
                }

                if(lockController.getLinka().isUnlocked()) {
                    lockController.doLock();
                }else{
                    lockController.doUnlock();
                }
            }
        });

        batteryPercent.setText(linka.batteryPercent + "%");

    }

    @OnTouch(R.id.circle)
    public boolean clickCircle(Circle circle, MotionEvent event){

        return false;
    }

    Runnable showSlider = new Runnable() {
        @Override
        public void run() {

        }
    };


    @OnClick(R.id.panic_button)
    void onPanic(){
        lockController.doActionSiren();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    public void refreshDisplay() {
        if (!isAdded()) return;

        if (getAppMainActivity() != null) {
            getAppMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    if (linka.isConnected && linka.isLockSettled) {

                        batteryPercent.setText(linka.batteryPercent + "%");

                        if(linka.isLocked){
                            statusText.setText("Hold to Unlock");
                        }else if(linka.isUnlocked){
                            statusText.setText("Hold to Lock");
                        }else if(linka.isLocking){
                            statusText.setText("Locking ...");
                        }else if (linka.isUnlocking){
                            statusText.setText("Unlocking ...");
                        }


                    } else if (linka.isConnected) {

                        statusText.setText("Connecting to LINKA");

                    } else {


                        //Check if bluetooth is turned on
                        if (getContext() != null) {
                            if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { return; }
                            final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                            bluetoothAdapter = bluetoothManager.getAdapter();
                        }

                        if (bluetoothAdapter == null) { return; }
                        String searchingString = "";
                        if(bluetoothAdapter.isEnabled()){
                            searchingString = _.i(R.string.reconnect_to_linka);

                            statusText.setText("Wake up LINKA");
                        }else{
                            statusText.setText("Turn on Bluetooth");

                        }
                    }
                }
            });
        }
    }

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

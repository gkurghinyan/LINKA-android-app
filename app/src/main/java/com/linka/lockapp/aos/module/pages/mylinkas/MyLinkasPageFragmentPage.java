package com.linka.lockapp.aos.module.pages.mylinkas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.linka.Lock.FirmwareAPI.Types.LockState;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.widget.LinkaButton;
import com.linka.lockapp.aos.module.widget.LinkaTextView;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LockGattUpdateReceiver;
import com.linka.lockapp.aos.module.widget.LockWidget;
import com.linka.lockapp.aos.module.widget.LocksController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED;

/**
 * Created by Vanson on 18/2/16.
 */
public class MyLinkasPageFragmentPage extends CoreFragment {

    public static int active_page = 0;
    public static int rssiInterval = 10;
    private BluetoothAdapter bluetoothAdapter;

    @BindView(R.id.lock_connect_new_lock)
    LinkaButton lockConnectNewLock;
    @BindView(R.id.blank_profile_block)
    LinearLayout blankProfileBlock;
    @BindView(R.id.lock_connection_title_icon)
    ImageView lockConnectionTitleIcon;
    @BindView(R.id.lock_connection_title)
    LinkaTextView lockConnectionTitle;
    @BindView(R.id.lock_widget)
    LockWidget lockWidget;
    @BindView(R.id.display_profile_block)
    LinearLayout displayProfileBlock;
    @BindView(R.id.notice)
    LinkaTextView notice;
    @BindView(R.id.lock_reconnect_linka)
    LinkaButton lockReconnectLinka;
    Unbinder unbinder;
    //@InjectView(R.id.panic_button)
    //LinkaButton panicButton;

    public static MyLinkasPageFragmentPage newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        MyLinkasPageFragmentPage fragment = new MyLinkasPageFragmentPage();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    boolean isInitBLE = false;
    Linka linka;
    LockController lockController;

    public MyLinkasPageFragmentPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_linkas_page_page, container, false);
        ButterKnife.bind(this, rootView);

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

        // Bug 72, Make sure Bluetooth is enabled
        BluetoothAdapter bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                getAppMainActivity().startActivityForResult(enableBtIntent, BLEHelpers.REQUEST_ENABLE_BT);
            }
        }

        init();
        implementTitle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /*@OnClick(R.id.panic_button)
    public void onPanic(){
        lockController.doTune();
        SleepNotificationService.getInstance().restartTimer();
    }*/

    public void implementTitle() {
        if (linka != null) {
            getAppMainActivity().setTitleNoUpperCase(linka.getName());
        }
    }



    /* INIT -
    If linka is not null (has LINKA)
    Start BLE Proxy
     */

    int i = 0;


    public void init() {

        if (linka != null) {

            lockController = LocksController.getInstance().getLockController();
            linka = lockController.getLinka();

            refreshDisplay();

            lockWidget.lockInteractListener = new LockWidget.LockInteractListener() {
                @Override
                public void onLockRingClick() {

                    i++;
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            i = 0;
                        }
                    };

                    if (i == 1) {
                        //Single click
                        handler.postDelayed(r, 250);
                    } else if (i == 2) {
                        //Double click
                        i = 0;


                        if (linka.isLocking || linka.isUnlocking) {
                            return;
                        }
                        if (!linka.isConnected) return;
                        if (linka.isLocked) {
                            if (AppDelegate.shouldAllowTapOnLockWidgetToUnlock) {
                                lockController.doUnlock();
                            }
                        } else {
                            if (AppDelegate.shouldAllowTapOnLockWidgetToLock) {
                                lockController.doLock();
                            }
                        }


                    }

                }

            };

        }

    }


    void refreshDisplay() {
        if (!isAdded()) return;

        if (getAppMainActivity() != null) {
            getAppMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    blankProfileBlock.setVisibility(View.GONE);
                    displayProfileBlock.setVisibility(View.VISIBLE);
                    //panicButton.setVisibility(View.GONE);

                    if (linka.isConnected && linka.isLockSettled) {
                        // connected
                        int linkaRssi = linka.rssi;
                        int highBoundRssi = AppDelegate.rssi_initial;
                        int midRssi = highBoundRssi - rssiInterval;
                        int midlowRssi = midRssi - rssiInterval;
                        int lowRssi = midlowRssi - rssiInterval;
                        int lockConnectIcon = R.drawable.icon_wireless_connected_full;

                        if (linkaRssi >= midlowRssi && linkaRssi < midRssi){
                            lockConnectIcon = R.drawable.icon_wireless_connected_mid;
                        }else if (linkaRssi < midlowRssi){
                            lockConnectIcon = R.drawable.icon_wireless_connected_low;
                        }

                        lockConnectionTitleIcon.setImageResource(lockConnectIcon);
                        lockConnectionTitle.setText(R.string.connected);
                        lockConnectionTitle.setTextColor(getResources().getColor(R.color.linka_blue));
                        lockReconnectLinka.setVisibility(View.GONE);
                        notice.setVisibility(View.VISIBLE);
                        if (linka.isLocked) {
                            notice.setText(R.string.unlock_instructions);
                            //panicButton.setVisibility(View.VISIBLE);
                        } else {
                            notice.setText(R.string.lock_instructions);
                        }
                    } else if (linka.isConnected) {
                        // disconnected

                        lockReconnectLinka.setClickable(false);
                        lockReconnectLinka.setEnabled(false);
                        lockReconnectLinka.setAlpha(1.0f);
                        lockReconnectLinka.setText(R.string.connecting_to_linka);
                        lockConnectionTitleIcon.setImageResource(R.drawable.icon_wireless_disconnected);
                        lockConnectionTitle.setText(R.string.disconnected);
                        lockConnectionTitle.setTextColor(getResources().getColor(R.color.bike_profile_red));
                        lockReconnectLinka.setVisibility(View.VISIBLE);

                        lockWidget.setOnClickListener(null);
                        notice.setVisibility(View.GONE);
                    } else {
                        // disconnected
                        lockConnectionTitleIcon.setImageResource(R.drawable.icon_wireless_disconnected);
                        lockConnectionTitle.setText(R.string.disconnected);
                        lockConnectionTitle.setTextColor(getResources().getColor(R.color.bike_profile_red));
                        notice.setVisibility(View.GONE);
                        lockReconnectLinka.setVisibility(View.VISIBLE);

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


                            // Enabling this button so that when clicked, it enables scanning to happen immediately
                            lockReconnectLinka.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //When clicked, enable scanning to take place
                                    LockController targetLockController = LocksController.getInstance().getLockController();
                                    targetLockController.repeatConnectionUntilSuccessful = true;
                                  //  targetLockController.doConnectDevice();
                                }
                            });
                        }else{
                            searchingString = _.i(R.string.turn_on_bluetooth); //If bluetooth is off, tell user to turn on bluetooth
                            lockReconnectLinka.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //When clicked, popup to ask to enable bluetooth
                                    bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
                                    if (bluetoothAdapter != null) {
                                        if (!bluetoothAdapter.isEnabled()) {
                                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                            getAppMainActivity().startActivityForResult(enableBtIntent, BLEHelpers.REQUEST_ENABLE_BT);

                                        }
                                    }
                                }
                            });



                        }
                        // Enable Button
                        lockReconnectLinka.setClickable(true);
                        lockReconnectLinka.setEnabled(true);
                        lockReconnectLinka.setAlpha(1.0f);
                        lockReconnectLinka.setText(searchingString);



                        //If we disconnect while locking or unlocking, make it locked or unlocked
                        if(linka.isLocking){
                            linka.isLocking = false;
                            linka.isLocked = true;
                            linka.lockState = LockState.LOCK_LOCKED;
                            linka.saveSettings();
                        }else if(linka.isUnlocking){
                            linka.isUnlocking = false;
                            linka.isLocked = false;
                            linka.lockState = LockState.LOCK_UNLOCKED;
                            linka.saveSettings();
                        }
                    }

                    lockWidget.setLinka(linka);
                }
            });
        }
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

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;
        if (object instanceof String && ((String) object).equals(LOCKSCONTROLLER_NOTIFY_REFRESHED)) {

            linka = Linka.getLinkaFromLockController();

            refreshDisplay();

        } else if (object != null && object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {

            linka = Linka.getLinkaFromLockController();

            refreshDisplay();
        } else if (object != null && object.equals(LockGattUpdateReceiver.GATT_UPDATE_RECEIVER_NOTIFY_DISCONNECTED)) {
            LogHelper.e("MyLinkasPageFrag", "[EVENTBUS] GATT DISCONNECT Notified");
            refreshDisplay();
        }
    }
}

package com.linka.lockapp.aos.module.pages.setup;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.linka.Lock.BLE.BluetoothLEDevice;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.BluetoothLEDeviceListAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceManager;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.eventbus.SuccessConnectBusEventMessage;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.dialogs.SuccessConnectionDialogFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.AccessLockFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetupLinka2 extends WalkthroughFragment {

    RecyclerView recyclerView;
    private static final int NO_BLUETOOTH = 0;
    private static final int SEARCH_WITH_BLUETOOTH = 1;
    private static final int SCAN_RESULT = 2;
    private static final int TURN_ON_LINKA = 3;
    private static final int NO_INTERNET = 4;

    boolean hasReceivedScanCallback;
    ScanCallback scanCallback;
    Bundle savedState;
    private int currentFragment;
    private boolean isInternet = false;
    private Linka currentLinka;

    BluetoothAdapter bluetoothAdapter;
    List<BluetoothLEDevice> devices = new ArrayList<>();
    List<Linka> linkaList = new ArrayList<>();
    BluetoothLEDeviceListAdapter adapter;

    Handler scanHandler = new Handler();
    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            linkaList.clear();
            devices.clear();
            refresh();
            if(currentFragment != SEARCH_WITH_BLUETOOTH){
                currentFragment = SEARCH_WITH_BLUETOOTH;
                updateLayouts(R.layout.fragment_searching_linka_with_bluetooth, 1);
            }
        }
    };

    boolean mScanning = false;
    Handler handler = new Handler();
    Runnable runnableStopLeDevice = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            refresh();
        }
    };

    private Handler closeLoadingHandler = null;
    private Runnable closeLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            closeLoadingHandler = null;
            setBlur(false);
//            threeDotsDialogFragment.dismiss();
            LinkaAPIServiceManager.getClient().dispatcher().cancelAll();
            String message = getString(R.string.fail_pairing) + " " + currentLinka.getName();
            stopLeScan();
            LinkaNotificationSettings.disconnect_all_linka();
            AppBluetoothService.getInstance().enableFixedTimeScanning(false);
            devices = new ArrayList<>();
            linkaList = new ArrayList<>();
            if (getInternetConnectivity()) {
                if (bluetoothAdapter != null) {
                    if (bluetoothAdapter.isEnabled()) {
                        currentFragment = SEARCH_WITH_BLUETOOTH;
                        updateLayouts(R.layout.fragment_searching_linka_with_bluetooth, 1);
                        scanLeDevice();
                    } else {
                        currentFragment = NO_BLUETOOTH;
                        updateLayouts(R.layout.fragment_no_bluetooth_connectivity, 1);
                    }
                }
            } else {
                isInternet = false;
                currentFragment = NO_INTERNET;
                updateLayouts(R.layout.fragment_no_internet_connectivity, 1);
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    };

    public static SetupLinka2 newInstance() {
        Bundle bundle = new Bundle();
        SetupLinka2 fragment = new SetupLinka2();
        fragment.setArguments(bundle);
        return fragment;
    }

    private final BroadcastReceiver blueToothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        scanHandler.removeCallbacks(scanRunnable);
                        currentFragment = NO_BLUETOOTH;
                        updateLayouts(R.layout.fragment_no_bluetooth_connectivity, 1);
                        if (mScanning) {
                            stopLeScan();
                        }
                        linkaList.clear();
                        devices.clear();
                        LinkaNotificationSettings.disconnect_all_linka();
                        AppBluetoothService.getInstance().enableFixedTimeScanning(false);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (getInternetConnectivity()) {
                            currentFragment = SEARCH_WITH_BLUETOOTH;
                            updateLayouts(R.layout.fragment_searching_linka_with_bluetooth, 1);
                            scanLeDevice();
                        }
                        break;
                }

            }
        }
    };

    private final BroadcastReceiver internetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getInternetConnectivity()) {
                if (!isInternet) {
                    isInternet = true;
                    currentFragment = SEARCH_WITH_BLUETOOTH;
                    bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
                    if (bluetoothAdapter != null) {
                        if (!bluetoothAdapter.isEnabled()) {
                            currentFragment = NO_BLUETOOTH;
                            updateLayouts(R.layout.fragment_no_bluetooth_connectivity, 1);
                        } else {
                            currentFragment = SEARCH_WITH_BLUETOOTH;
                            updateLayouts(R.layout.fragment_searching_linka_with_bluetooth, 1);
                            scanLeDevice();
                        }
                    } else {
                        currentFragment = NO_BLUETOOTH;
                        updateLayouts(R.layout.fragment_no_bluetooth_connectivity, 1);
                    }
                }
            } else {
                if (isInternet) {
                    isInternet = false;
                    currentFragment = NO_INTERNET;
                    updateLayouts(R.layout.fragment_no_internet_connectivity, 1);
                }
            }
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] layouts = new int[]{
                R.layout.fragment_setup_turn_bluetooth_on,
                R.layout.fragment_searching_linka_with_bluetooth
        };

        isInternet = getInternetConnectivity();
        currentFragment = TURN_ON_LINKA;
        setLayouts(layouts);
        if (!isInternet) {
            updateLayouts(R.layout.fragment_no_internet_connectivity, 1);
        }

        setLayoutView(new LayoutView() {
            @Override
            public void onViewCreated(View view, int position) {
                setView(view, position);
            }

            @Override
            public void onViewChanged(int position) {
                setPages(position);
            }
        });

        if (getArguments() != null) {
            Bundle bundle = getArguments();
        }
        savedState = savedInstanceState;

    }

    private void setPages(int position){
        if (position == 1) {
            registerReceivers(true);
            if (!getInternetConnectivity()) {
                currentFragment = NO_INTERNET;
                updateLayouts(R.layout.fragment_no_internet_connectivity, 1);
            } else {
                bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
                if (bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {
                        currentFragment = NO_BLUETOOTH;
                        updateLayouts(R.layout.fragment_no_bluetooth_connectivity, 1);
//                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                            getAppMainActivity().startActivityForResult(enableBtIntent, BLEHelpers.REQUEST_ENABLE_BT);

                        //Close this fragment to avoid crash if bluetooth denied
                    } else {
                        devices = new ArrayList<>();
                        linkaList = new ArrayList<>();
                        currentFragment = SEARCH_WITH_BLUETOOTH;
                        updateLayouts(R.layout.fragment_searching_linka_with_bluetooth, 1);
                        scanLeDevice();
                    }
                } else {
                    currentFragment = NO_BLUETOOTH;
                    updateLayouts(R.layout.fragment_no_bluetooth_connectivity, 1);
                }
            }
        } else if (position == 0) {
            registerReceivers(false);
            currentFragment = TURN_ON_LINKA;
        }
    }

    private boolean getInternetConnectivity() {
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        return connected;
    }

    void setView(View view, int position) {
        if (position == 1 && currentFragment == SCAN_RESULT) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

            if (recyclerView != null) {
                this.recyclerView = recyclerView;
            }

            if (recyclerView != null) {
                init();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.context = null;
        }
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        scanHandler.removeCallbacks(scanRunnable);
        //scanHandler.postDelayed(scanRunnable, 1000); //Why wait 1 second? Disabled.

//        scanHandler.post(scanRunnable);
        if (currentFragment != TURN_ON_LINKA) {
            registerReceivers(true);
        }
        if (closeLoadingHandler != null) {

            closeLoadingHandler.postDelayed(closeLoadingRunnable, 30000);
        }
        EventBus.getDefault().register(this);
        if (currentFragment != TURN_ON_LINKA) {
           setPages(1);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        scanHandler.removeCallbacks(scanRunnable);
        stopLeScan();

        //Set to true to stop all callbacks
        hasReceivedScanCallback = true;
        if (currentFragment != TURN_ON_LINKA) {
            registerReceivers(false);
        }
        if (closeLoadingHandler != null) {
            closeLoadingHandler.removeCallbacks(closeLoadingRunnable);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Subscribe
    public void dialogClosed(SuccessConnectBusEventMessage connectBusEventMessage) {
        setBlur(false);
        getAppMainActivity().pushFragment(AutoUpdateFragment.newInstance(Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()),AutoUpdateFragment.WALKTHROUGH));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && recyclerView != null && recyclerView.getLayoutManager() != null) {
            outState.putParcelable("ss", recyclerView.getLayoutManager().onSaveInstanceState());
        }
    }


    void init() {

        //Need to set to null, or else sometimes the callback won't work
        scanCallback = null;

        if (savedState != null) {
            if (recyclerView != null) {
                Parcelable ss = savedState.getParcelable("ss");
                if (ss != null) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(ss);
                }
            }
        }

        adapter = new BluetoothLEDeviceListAdapter(getContext());
        adapter.setOnClickDeviceItemListener(new BluetoothLEDeviceListAdapter.OnClickDeviceItemListener() {
            @Override
            public void onClickDeviceItem(Linka item, int position) {
                currentLinka = item;
                scanHandler.removeCallbacks(scanRunnable);
                tryPreparePairingUp(devices.get(position));
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        refresh();
    }

    private void registerReceivers(boolean register) {
        if (register) {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(blueToothReceiver, filter);
            IntentFilter filter1 = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            getActivity().registerReceiver(internetReceiver, filter1);
        } else {
            getActivity().unregisterReceiver(blueToothReceiver);
            getActivity().unregisterReceiver(internetReceiver);
        }
    }

    void refresh() {
        if (!isAdded()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;
                if (adapter == null) return;
                adapter.setList(linkaList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    void scanLeDevice() {
        if (bluetoothAdapter == null) return;
        mScanning = true;

        initializeScanCallback();
        bluetoothAdapter.startLeScan(mLeScanCallback);
        refresh();
    }

    void stopLeScan() {
        LogHelper.e("SCAN", "stopping Scan");
        if (handler != null) {
            handler.post(runnableStopLeDevice);
        }
        mScanning = false;
    }

    private void initializeScanCallback() {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                if (!isAdded()) return;

                if (device != null) {
                    LogHelper.e("SCAN", "Got Device " + device.getName() + device.getAddress());
                } else {
                    return;
                }
                int result = BLEHelpers.upsertBluetoothLEDeviceList(devices, linkaList, device, rssi, scanRecord);
                if (result == 0) {
                    if (currentFragment != SCAN_RESULT && currentFragment != NO_BLUETOOTH && currentFragment != NO_INTERNET) {
                        currentFragment = SCAN_RESULT;
                        updateLayouts(R.layout.fragment_setup_search_for_linkas_page, 1);
                    }
                    refresh();
                    if (!linkaList.isEmpty()) {
                        scanHandler.removeCallbacks(scanRunnable);
                        scanHandler.postDelayed(scanRunnable, 4000);
                    }
                } else if (result == 1) {
                    if (!linkaList.isEmpty()) {
                        scanHandler.removeCallbacks(scanRunnable);
                        scanHandler.postDelayed(scanRunnable, 4000);
                    }
                }
            }
        };
    }

    BluetoothAdapter.LeScanCallback mLeScanCallback;


    void tryPreparePairingUp(final BluetoothLEDevice item) {
        closeLoadingHandler = new Handler();
        closeLoadingHandler.postDelayed(closeLoadingRunnable, 30000);
        setBlur(true);
        final Linka linka = Linka.makeLinka(item);
        LinkaAccessKey.tryRegisterLock(getAppMainActivity(), linka, new LinkaAccessKey.LinkaAccessKeyDetailedErrorCallback() {
            @Override
            public void onObtain(LinkaAccessKey accessKey, boolean isValid, boolean showError, int code, String error) {

                if (accessKey == null && showError) {
                    if (!isAdded()) return;

                    LinkaAPIServiceImpl.post_error(
                            getAppMainActivity(),
                            linka,
                            "" + code,
                            error,
                            null
                    );

                    showAlert("", error);
                    setBlur(false);
//                    threeDotsDialogFragment.dismiss();
                    if (closeLoadingHandler != null) {
                        closeLoadingHandler.removeCallbacks(closeLoadingRunnable);
                        closeLoadingHandler = null;
                    }
                    return;
                }

                if (accessKey == null) {
                    return;
                }

                if (!isValid) {
                    if (!isAdded()) return;
                    if (accessKey.access_key_admin.equals("")) {
                        setBlur(false);
//                        threeDotsDialogFragment.dismiss();
                        if (closeLoadingHandler != null) {
                            closeLoadingHandler.removeCallbacks(closeLoadingRunnable);
                            closeLoadingHandler = null;
                        }
                        getAppMainActivity().pushFragment(AccessLockFragment.newInstance(linka));

                        return;
                    } else {
                        setBlur(false);
//                        threeDotsDialogFragment.dismiss();
                        if (closeLoadingHandler != null) {
                            closeLoadingHandler.removeCallbacks(closeLoadingRunnable);
                            closeLoadingHandler = null;
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(_.i(R.string.reactivate_access_keys))
                                .setMessage(_.i(R.string.wish_to_reactivate_access_keys))
                                .setNegativeButton(R.string.no, null)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        trySendAdminPermissionRequest(linka);
                                    }
                                })
                                .show();
                        return;
                    }
                } else {
                    tryPairup(item);
                    return;
                }
            }
        });
    }


    void trySendAdminPermissionRequest(Linka linka) {
        setBlur(true);
        LinkaAPIServiceImpl.send_request_for_user_permission(getActivity(), linka, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                setBlur(false);
                if (!isAdded()) return;
                if (LinkaAPIServiceImpl.check(response, false, getActivity())) {
                    showAlert(_.i(R.string.almost_done), _.i(R.string.permission_being_processed));
                    return;
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                setBlur(false);
            }
        });
    }


    void tryPairup(final BluetoothLEDevice item) {

//        showLoading(_.i(R.string.pairing_up));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Linka linka = Linka.saveLinka(item, true);
                getAppMainActivity().saveLatestLinka(linka);

                removeDialog();
//                threeDotsDialogFragment.dismiss();
                if (closeLoadingHandler != null) {
                    closeLoadingHandler.removeCallbacks(closeLoadingRunnable);
                    closeLoadingHandler = null;
                }
                SuccessConnectionDialogFragment.newInstance(getString(R.string.connected_lower)).show(getFragmentManager(), null);

            }
        }, 500);

    }

    /* ON EVENT */

    @Subscribe
    public void onEvent(Object object) {
    }

}
package com.linka.lockapp.aos.module.pages.setup;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.linka.Lock.BLE.BluetoothLEDevice;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.BluetoothLEDeviceListAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.helpers.BLEHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughFragment;

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

    boolean hasReceivedScanCallback;
    ScanCallback scanCallback;
    Bundle savedState;

    public static SetupLinka2 newInstance() {
        Bundle bundle = new Bundle();
        SetupLinka2 fragment = new SetupLinka2();
        fragment.setArguments(bundle);
        return fragment;
    }

    ImageView lockImage;

    public SetupLinka2() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        int[] layouts = new int[]{
                R.layout.fragment_setup_turn_bluetooth_on,
                R.layout.fragment_setup_search_for_linkas_page
        };

        setLayouts(layouts);

        setLayoutView(new LayoutView() {
            @Override
            public void onViewCreated(View view) {

                setView(view);
            }
            @Override
            public void onViewChanged(int position){

            }
        });

        if (getArguments() != null) {
            Bundle bundle = getArguments();
        }
        savedState = savedInstanceState;

    }

    void setView(View view){

        lockImage = (ImageView) view.findViewById(R.id.lock_image);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        if(recyclerView != null){
            this.recyclerView = recyclerView;
        }

        if(recyclerView != null) {
            init();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null)
        {
            adapter.context = null;
        }
        if (recyclerView != null)
        {
            recyclerView.setAdapter(null);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        scanHandler.removeCallbacks(scanRunnable);
        //scanHandler.postDelayed(scanRunnable, 1000); //Why wait 1 second? Disabled.

        scanHandler.post(scanRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanHandler.removeCallbacks(scanRunnable);
        stopLeScan();

        //Set to true to stop all callbacks
        hasReceivedScanCallback = true;
    }


    Handler scanHandler = new Handler();
    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanLeDevice();
        }
    };


    BluetoothAdapter bluetoothAdapter;
    List<BluetoothLEDevice> devices = new ArrayList<>();
    List<Linka> linkaList = new ArrayList<>();
    BluetoothLEDeviceListAdapter adapter;



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ss", recyclerView.getLayoutManager().onSaveInstanceState());
    }


    void init() {



        //Need to set to null, or else sometimes the callback won't work
        scanCallback = null;

        bluetoothAdapter = BLEHelpers.checkBLESupportForAdapter(getContext());
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                getAppMainActivity().startActivityForResult(enableBtIntent, BLEHelpers.REQUEST_ENABLE_BT);

                //Close this fragment to avoid crash if bluetooth denied
                getAppMainActivity().popFragment();

            }
        }

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
                tryPreparePairingUp(devices.get(position));
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        refresh();
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

                if (linkaList.size() <= 0) {
                    lockImage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    lockImage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

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

    void scanLeDevice() {
        if (bluetoothAdapter == null) return;
        mScanning = true;

        bluetoothAdapter.startLeScan(mLeScanCallback);
        refresh();
    }

    void stopLeScan(){
        LogHelper.e("SCAN", "stopping Scan");
        if (handler != null) {
            handler.post(runnableStopLeDevice);
        }
        mScanning = false;
    }


    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) return;

                    if(device != null) {
                        LogHelper.e("SCAN", "Got Device " + device.getName() + device.getAddress());
                    }

                    if (adapter == null) {
                        LogHelper.e("Bluetooth Adapter", "is NULL!!!");
                        return;
                    }

                    if (BLEHelpers.upsertBluetoothLEDeviceList(devices, linkaList, device, rssi, scanRecord))
                    {
                        refresh();
                    }
                }
            });
        }
    };


    void tryPreparePairingUp(final BluetoothLEDevice item) {
        showLoading(_.i(R.string.preparing_to_pair_up));
        final Linka linka = Linka.makeLinka(item);
        LinkaAccessKey.tryRegisterLock(getAppMainActivity(), linka, new LinkaAccessKey.LinkaAccessKeyDetailedErrorCallback() {
            @Override
            public void onObtain(LinkaAccessKey accessKey, boolean isValid, boolean showError, int code, String error) {
                hideLoading();

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
                    return;
                }

                if (accessKey == null)
                {
                    return;
                }

                if (!isValid) {
                    if (!isAdded()) return;
                    if (accessKey.access_key_admin.equals("")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("")
                                .setMessage(_.i(R.string.wish_to_request_user_permission))
                                .setNegativeButton(R.string.no, null)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        trySendUserPermissionRequest(linka);
                                    }
                                })
                                .show();
                        return;
                    } else {
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


    void trySendUserPermissionRequest(Linka linka) {
        showLoading(_.i(R.string.requesting_permission));
        LinkaAPIServiceImpl.send_request_for_user_permission(getActivity(), linka, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                hideLoading();
                if (!isAdded()) return;
                if (LinkaAPIServiceImpl.check(response, false, getActivity())) {
                    showAlert(_.i(R.string.almost_done), _.i(R.string.permission_being_approved));
                    return;
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                hideLoading();
            }
        });
    }


    void trySendAdminPermissionRequest(Linka linka) {
        showLoading(_.i(R.string.requesting_permission));
        LinkaAPIServiceImpl.send_request_for_user_permission(getActivity(), linka, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                hideLoading();
                if (!isAdded()) return;
                if (LinkaAPIServiceImpl.check(response, false, getActivity())) {
                    showAlert(_.i(R.string.almost_done), _.i(R.string.permission_being_processed));
                    return;
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                hideLoading();
            }
        });
    }



    void tryPairup(final BluetoothLEDevice item) {

        showLoading(_.i(R.string.pairing_up));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Linka linka = Linka.saveLinka(item, true);
                getAppMainActivity().saveLatestLinka(linka);

                getAppMainActivity().pushFragment(SetupLinka3.newInstance());

            }
        }, 500);

    }

}
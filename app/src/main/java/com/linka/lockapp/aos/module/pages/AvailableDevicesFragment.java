package com.linka.lockapp.aos.module.pages;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.AvailableDevicesAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.other.RecyclerItemTouchHelper;
import com.linka.lockapp.aos.module.other.Utils;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableDevicesFragment extends CoreFragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    @BindView(R.id.root)
    ConstraintLayout root;
    @BindView(R.id.recycler_view)
    RecyclerView devicesList;

    private AvailableDevicesAdapter adapter;
    private List<Pair<String,Linka>> devices;
    private Snackbar snackbar;

    public static AvailableDevicesFragment newInstance() {

        Bundle args = new Bundle();
        AvailableDevicesFragment fragment = new AvailableDevicesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_available_devices, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        getAppMainActivity().setTitle(getString(R.string.available_devices));
        init();
    }

    private void init(){
        devices = new ArrayList<>();
        devicesList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        devicesList.setItemAnimator(new DefaultItemAnimator());
        adapter = new AvailableDevicesAdapter();
        devicesList.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(devicesList);
        updateList();

    }

    private void updateList(){
        List<Linka> list = LocksController.getInstance().getLinkas();
        for (Linka linka:list){
            getLockPermissions(linka);
        }
    }

    void getLockPermissions(final Linka linka) {
        Utils.showLoading(getContext(),root);
        //Get lock permissions
        LinkaAPIServiceImpl.lock_permissions(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.LockPermissionsResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Response<LinkaAPIServiceResponse.LockPermissionsResponse> response) {

                if (LinkaAPIServiceImpl.check(response, false, null)) {

                    String addedEmail = null;
                    Linka addedLinka = null;
                    for (LinkaAPIServiceResponse.LockPermissionsResponse.Data userData : response.body().data) {

                        if (!userData.owner) {
                            if(userData.email.equals(LinkaAPIServiceImpl.getUserEmail())){
                                addedLinka = linka;
                            }
                            adapter.notifyItemInserted(devices.size()-1);
                        }else {
                            addedEmail = userData.email;
                        }
                    }
                    if(addedEmail != null && addedLinka != null){
                        Pair<String,Linka> pair = new Pair<>(addedEmail,addedLinka);
                        devices.add(pair);
                        adapter.insertDevice(pair);
                        adapter.notifyItemInserted(devices.size()-1);
                    }
                }
                Utils.cancelLoading();
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Throwable t) {
                Utils.cancelLoading();
            }
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof AvailableDevicesAdapter.DeviceHolder) {
            // get the removed item name to display it in snack bar
            String name = devices.get(viewHolder.getAdapterPosition()).second.getName();

            // backup of removed item for undo purpose
            final Pair<String,Linka> deletedItem = devices.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            adapter.removeDevice(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            snackbar = Snackbar
                    .make(root, name + " removed from list!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    adapter.restoreDevice(deletedItem, deletedIndex);
                }
            });
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    if(event != DISMISS_EVENT_ACTION) {
                        revokeAccess(deletedItem.second);
                    }
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    void revokeAccess(Linka linka) {
        LinkaAPIServiceImpl.revoke_access(getAppMainActivity(), linka, LinkaAPIServiceImpl.getUserID(), new Callback<LinkaAPIServiceResponse>() {

            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
//                    if(snackbar == null || !snackbar.isShown()) {
//
//                    }
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }
}

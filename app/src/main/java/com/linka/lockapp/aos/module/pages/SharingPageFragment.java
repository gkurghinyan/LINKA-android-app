package com.linka.lockapp.aos.module.pages;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.SharingAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.User;
import com.linka.lockapp.aos.module.widget.LinkaButton;
import com.linka.lockapp.aos.module.widget.LinkaTextView;
import com.linka.lockapp.aos.module.widget.LockController;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED;

/**
 * Created by kyle on 5/9/18.
 */

public class SharingPageFragment extends CoreFragment {

    public static int active_page = 0;
    public static int rssiInterval = 10;

    RecyclerView recyclerView;

    @BindView(R.id.owner_email)
    LinkaTextView ownerEmail;
    @BindView(R.id.owner_name)
    LinkaTextView ownerName;
    @BindView(R.id.add_user)
    LinkaButton addUser;

    SharingAdapter adapter;
    Unbinder unbinder;

    List<User> userList = new ArrayList<>();

    boolean selfIsOwner = false;

    public static SharingPageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SharingPageFragment fragment = new SharingPageFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    boolean isInitBLE = false;
    Linka linka;
    LockController lockController;

    public SharingPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sharing_page, container, false);
        Unbinder unbinder = ButterKnife.bind(this, rootView);

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

        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);


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


    void init(){

        getLockPermissions();

        adapter = new SharingAdapter(getContext());
        adapter.setOnClickDeviceItemListener(new SharingAdapter.OnClickDeviceItemListener() {
            @Override
            public void onClickDeviceItem(final User item, int position) {

                //If not the owner, then you can't do anything
                if(!selfIsOwner){

                    //If this is themself, then ask them if they want to revoke their own access
                    if(item.email.equals(LinkaAPIServiceImpl.getUserEmail())){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getAppMainActivity());
                        builder.setTitle("Would you like to revoke your own access?");
                        builder.setItems(new CharSequence[]
                                        {"Revoke my access", "Cancel"},
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                        switch (which) {
                                            case 0:
                                                Toast.makeText(getAppMainActivity(), "Revoking Access", Toast.LENGTH_SHORT).show();
                                                revokeAccess(item.email);
                                                break;
                                            case 1:
                                                break;
                                        }
                                    }
                                });
                        builder.create().show();
                    }

                    return;
                }

                //Has no permissions
                if(!item.isPendingApproval) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getAppMainActivity());
                    builder.setTitle("Select Action");
                    builder.setItems(new CharSequence[]
                                    {"Revoke Access", "Transfer ownership to this user", "Cancel"},
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    switch (which) {
                                        case 0:
                                            Toast.makeText(getAppMainActivity(), "Revoking access", Toast.LENGTH_SHORT).show();
                                            revokeAccess(item.email);
                                            break;
                                        case 1:
                                            confirmTransferOwnership(item.email);
                                            break;
                                        case 2:
                                            break;
                                    }
                                }
                            });
                    builder.create().show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getAppMainActivity());
                    builder.setTitle("Select Action");
                    builder.setItems(new CharSequence[]
                                    {"Grant Access", "Cancel"},
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    switch (which) {
                                        case 0:
                                            Toast.makeText(getAppMainActivity(), "Granting Access", Toast.LENGTH_SHORT).show();
                                            inviteUser(item.email);
                                            break;
                                        case 1:
                                            break;
                                    }
                                }
                            });
                    builder.create().show();

                }

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

    }


    @OnClick(R.id.add_user)
    void onAddUser(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getAppMainActivity());
        builder.setTitle("Enter email address of the person you are inviting");

        final EditText input = new EditText(getAppMainActivity());

        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString();

                inviteUser(email);
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    void getLockPermissions(){
        addUser.setVisibility(View.GONE);
        userList.clear();
        selfIsOwner = false;

        //Get lock permissions
        LinkaAPIServiceImpl.lock_permissions(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.LockPermissionsResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Response<LinkaAPIServiceResponse.LockPermissionsResponse> response) {

                if(LinkaAPIServiceImpl.check(response, false, null)) {

                    for (LinkaAPIServiceResponse.LockPermissionsResponse.Data userData : response.body().data) {
                        User newUser = User.saveUserForEmail(userData.email, userData.first_name, userData.last_name, userData.name, userData.owner, userData.isPendingApproval);

                        if(ownerEmail == null || ownerName == null){
                            return;
                        }

                        if(userData.owner){
                            ownerEmail.setText(userData.email);
                            ownerName.setText(userData.name);

                            //If we are the owner, then add the add user button
                            if (userData.email.equals(LinkaAPIServiceImpl.getUserEmail())) {
                                addUser.setVisibility(View.VISIBLE);

                                ownerName.setText(userData.name + " (You)");
                                selfIsOwner = true;
                            }

                        }else {
                            userList.add(newUser);

                        }

                    }

                    if (adapter == null) return;
                    adapter.setList(userList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Throwable t) {

            }
        });
    }

    void inviteUser(String email){
        LinkaAPIServiceImpl.send_invite(getAppMainActivity(), linka, email, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if(LinkaAPIServiceImpl.check(response, false, null)){
                    getLockPermissions();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }
    void transferOwnership(String email){
        Toast.makeText(getAppMainActivity(), "Transfering Ownership", Toast.LENGTH_SHORT).show();
        LinkaAPIServiceImpl.transfer_ownership(getAppMainActivity(), linka, email, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if(LinkaAPIServiceImpl.check(response, false, null)){
                    getLockPermissions();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }
    void revokeAccess(String email){
        LinkaAPIServiceImpl.revoke_access(getAppMainActivity(), linka, email, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if(LinkaAPIServiceImpl.check(response, false, null)){
                    getLockPermissions();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }

    void confirmTransferOwnership(final String email){
        new AlertDialog.Builder(getAppMainActivity())
                .setTitle("Are you sure you want to transfer ownership?")
                .setMessage("After you transfer ownership, you will no longer be able to control any of the lock functions.")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        transferOwnership(email);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

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

    public void onEvent(Object object) {
        if (!isAdded()) return;
        if (object instanceof String && ((String) object).equals(LOCKSCONTROLLER_NOTIFY_REFRESHED)) {

            /*Linka newLinka = Linka.getLinkaFromLockController(linka);
            if(newLinka != null){
                linka = newLinka;
            }*/
        }
    }
}

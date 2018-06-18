package com.linka.lockapp.aos.module.pages;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.SharingAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.eventbus.InviteUserBusEvent;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.User;
import com.linka.lockapp.aos.module.other.RecyclerItemTouchHelper;
import com.linka.lockapp.aos.module.pages.dialogs.InviteUserDialogFragment;
import com.linka.lockapp.aos.module.widget.LockController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED;

/**
 * Created by kyle on 5/9/18.
 */

public class SharingPageFragment extends CoreFragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    public static int active_page = 0;
    public static int rssiInterval = 10;

    RecyclerView recyclerView;
    private ConstraintLayout root;

    //    @BindView(R.id.owner_email)
//    LinkaTextView ownerEmail;
//    @BindView(R.id.owner_name)
//    LinkaTextView ownerName;
    @BindView(R.id.title)
    TextView title;
//    @BindView(R.id.search_friends)
//    EditText search;

    private Snackbar snackbar;
    private Unbinder unbinder;
    private View rootView;

    SharingAdapter adapter;

    List<User> userList = new ArrayList<>();
    List<User> searchList = new ArrayList<>();

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
        rootView = inflater.inflate(R.layout.fragment_sharing_page, container, false);
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

        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        root = (ConstraintLayout) view.findViewById(R.id.users_page_root);


        init();
        implementTitle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        editChangeDisp.dispose();
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


    void init() {
        getLockPermissions();

        adapter = new SharingAdapter(getContext());
        adapter.setOnClickDeviceItemListener(new SharingAdapter.OnClickDeviceItemListener() {
            @Override
            public void onClickDeviceItem(final User item, int position) {
                //If not the owner, then you can't do anything
                if (!selfIsOwner) {

                    //If this is themself, then ask them if they want to revoke their own access
                    if (item.email.equals(LinkaAPIServiceImpl.getUserEmail())) {
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
                                                revokeAccess(item.userId);
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
                if (!item.isPendingApproval) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getAppMainActivity());
                    builder.setTitle("Select Action");
                    builder.setItems(new CharSequence[]
                                    {"Transfer ownership to this user", "Cancel"},
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    switch (which) {
                                        case 0:
                                            confirmTransferOwnership(item.email);
                                            break;
                                        case 1:
                                            break;
                                    }
                                }
                            });
                    builder.create().show();
                } else {
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

            @Override
            public void onAddButtonClicked() {
                ArrayList<String> emails = new ArrayList<>();
                if(!userList.isEmpty()){
                    for(User user:userList){
                        emails.add(user.email);
                    }
                }
                getAppMainActivity().curFragmentCount ++;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.users_page_root, InviteUserDialogFragment.newInstance(emails))
                        .commit();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
//        initEditTextSubscription();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    }

//    private Disposable editChangeDisp;
//    private void initEditTextSubscription() {
//        editChangeDisp = RxTextView.textChanges(search)
//                .debounce(400, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.computation())
//                .map(new Function<CharSequence, List<User>>() {
//                    @Override
//                    public List<User> apply(CharSequence charSequence) throws Exception {
//                        if(!charSequence.toString().isEmpty()){
//                            searchList.clear();
//                            for(User user : userList){
//                                if(user.name.contains(charSequence)){
//                                    searchList.add(user);
//                                }
//                            }
//                            return searchList;
//                        } else {
//                            return userList;
//                        }
//                    }
//                }).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<User>>(){
//
//                    @Override
//                    public void onNext(List<User> users) {
//                        adapter.setList(users);
//                        adapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//
//    }

    void getLockPermissions() {
//        addUser.setVisibility(View.GONE);
        userList.clear();
        selfIsOwner = false;

        //Get lock permissions
        LinkaAPIServiceImpl.lock_permissions(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.LockPermissionsResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Response<LinkaAPIServiceResponse.LockPermissionsResponse> response) {

                if (LinkaAPIServiceImpl.check(response, false, null)) {

                    for (LinkaAPIServiceResponse.LockPermissionsResponse.Data userData : response.body().data) {
                        User newUser = User.saveUserForEmail(userData.email, userData.first_name, userData.last_name, userData.name, userData.userId, userData.owner, userData.isPendingApproval);

//                        if(ownerEmail == null || ownerName == null){
//                            return;
//                        }

                        if (userData.owner) {
//                            ownerEmail.setText(userData.email);
//                            ownerName.setText(userData.name);

                            //If we are the owner, then add the add user button
                            if (userData.email.equals(LinkaAPIServiceImpl.getUserEmail())) {
//                                addUser.setVisibility(View.VISIBLE);
//
//                                ownerName.setText(userData.name + " (You)");
                                selfIsOwner = true;
                            }

                        } else {
                            userList.add(newUser);

                        }

                    }

                    if (adapter == null) return;
                    adapter.setList(userList);
                    if(title == null){
                        unbinder = ButterKnife.bind(SharingPageFragment.this,rootView);
                    }
                    if (userList.isEmpty()) {
                        title.setText(R.string.no_one_to_access_your_bike);
                    } else {
                        title.setText(R.string.tap_to_modify_details);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Throwable t) {

            }
        });
    }

    void inviteUser(String email) {
        LinkaAPIServiceImpl.send_invite(getAppMainActivity(), linka, email, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    getLockPermissions();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }

    void transferOwnership(String email) {
        Toast.makeText(getAppMainActivity(), "Transfering Ownership", Toast.LENGTH_SHORT).show();
        LinkaAPIServiceImpl.transfer_ownership(getAppMainActivity(), linka, email, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    getLockPermissions();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }

    void revokeAccess(String userId) {
        LinkaAPIServiceImpl.revoke_access(getAppMainActivity(), linka, userId, new Callback<LinkaAPIServiceResponse>() {

            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    if(snackbar == null || !snackbar.isShown()) {
                        getLockPermissions();
                    }
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }

    void confirmTransferOwnership(final String email) {
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
        getAppMainActivity().setTitle("USERS");
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

            /*Linka newLinka = Linka.getLinkaFromLockController(linka);
            if(newLinka != null){
                linka = newLinka;
            }*/
        }
        if (object instanceof InviteUserBusEvent) {
            inviteUser(((InviteUserBusEvent) object).getEmail());
        }
        if (object instanceof String && object.equals("closeInvite")) {
            if (getFragmentManager().findFragmentById(R.id.users_page_root) != null &&
                    getFragmentManager().findFragmentById(R.id.users_page_root) instanceof InviteUserDialogFragment) {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof SharingAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = userList.get(viewHolder.getAdapterPosition()).name;

            // backup of removed item for undo purpose
            final User deletedItem = userList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            adapter.removeUser(viewHolder.getAdapterPosition());
            if (adapter.getItemCount() <= 1) {
                title.setText(R.string.no_one_to_access_your_bike);
            }

            // showing snack bar with Undo option
            snackbar = Snackbar
                    .make(root, name + " removed from list!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    adapter.restoreUser(deletedItem, deletedIndex);
                    title.setText(R.string.tap_to_modify_details);
                }
            });
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    if(event != DISMISS_EVENT_ACTION) {
                        revokeAccess(deletedItem.userId);
                    }
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}

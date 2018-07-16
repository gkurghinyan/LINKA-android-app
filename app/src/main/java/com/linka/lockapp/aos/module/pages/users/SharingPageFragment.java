package com.linka.lockapp.aos.module.pages.users;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.SharingAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.User;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.ThreeDotsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

public class SharingPageFragment extends CoreFragment {
    public static final String REFRESH_LIST_OF_USERS = "RefreshListOfUsers";
    private SimpleDateFormat allDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.UK);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd', 'h:mma", Locale.UK);

    public static int active_page = 0;
    public static int rssiInterval = 10;

    //This fragment position in viewpager of MainTabBarPageFragment
    private final int thisPage = 1;

    RecyclerView recyclerView;
    private ConstraintLayout root;

    @BindView(R.id.owner_name)
    TextView ownerName;

    @BindView(R.id.last_used)
    TextView ownerLastUsed;

    @BindView(R.id.owner_avatar)
    ImageView ownerAvatar;

    @BindView(R.id.three_dots)
    ThreeDotsView threeDotsView;

    private Unbinder unbinder;
    private View rootView;

    SharingAdapter adapter;

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


    void init() {
        getLockPermissions();

        adapter = new SharingAdapter(getContext());
        adapter.setOnClickDeviceItemListener(new SharingAdapter.OnClickDeviceItemListener() {
            @Override
            public void onClickDeviceItem(final User item, int position) {
                //If not the owner, then you can't do anything
                if (!selfIsOwner) {
                    return;
                }

                //Has no permissions
                if (!item.isPendingApproval) {
                    getAppMainActivity().curFragmentCount++;
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.users_page_root, GuestUserFragment.newInstance(item, linka))
                            .commit();
                } else {
                    getAppMainActivity().curFragmentCount++;
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.users_page_root, NeedApprovalUserFragment.newInstance(item, linka))
                            .commit();
                }
            }

            @Override
            public void onAddButtonClicked() {
                ArrayList<String> emails = new ArrayList<>();
                if (!userList.isEmpty()) {
                    for (User user : userList) {
                        emails.add(user.email);
                    }
                }
                getAppMainActivity().curFragmentCount++;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.users_page_root, InviteUserDialogFragment.newInstance(emails, linka))
                        .commit();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private Handler lockPermissionsHandler = null;
    private Runnable lockPermissionsRunnable = new Runnable() {
        @Override
        public void run() {
            //Get lock permissions
            LinkaAPIServiceImpl.lock_permissions(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.LockPermissionsResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Response<LinkaAPIServiceResponse.LockPermissionsResponse> response) {

                    if (LinkaAPIServiceImpl.check(response, false, null)) {

                        for (final LinkaAPIServiceResponse.LockPermissionsResponse.Data userData : response.body().data) {
                            User newUser = User.saveUserForEmail(userData.email,
                                    userData.first_name,
                                    userData.last_name,
                                    userData.name,
                                    userData.userId,
                                    userData.owner,
                                    userData.isPendingApproval,
                                    userData.lastUsed);

                            if (ownerName == null) {
                                unbinder = ButterKnife.bind(SharingPageFragment.this, rootView);
                            }

                            if (userData.owner) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ownerAvatar.setVisibility(View.VISIBLE);
                                            ownerAvatar.setColorFilter(getResources().getColor(R.color.linka_black_transparent), PorterDuff.Mode.MULTIPLY);
                                            ownerName.setVisibility(View.VISIBLE);
                                            ownerName.setText(userData.name);
                                            ownerLastUsed.setVisibility(View.VISIBLE);
                                            if (userData.lastUsed != null) {
                                                Date date = null;
                                                try {
                                                    date = allDateFormat.parse(userData.lastUsed);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                if (date != null) {
                                                    ownerLastUsed.setText("Last used: " + dateFormat.format(date));
                                                }
                                            }

                                            //If we are the owner, then add the add user button
                                            if (userData.email.equals(LinkaAPIServiceImpl.getUserEmail())) {
                                                adapter.setAddButtonVisibility(SharingAdapter.VISIBLE);

                                                selfIsOwner = true;
                                            }
                                        }
                                    });
                                }

                            } else {
                                userList.add(newUser);
                            }

                        }

                        if (adapter == null) return;
                        adapter.setList(userList);


                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                threeDotsView.setVisibility(View.GONE);
                            }
                        });
                    }
                    lockPermissionsHandler = null;
                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Throwable t) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                threeDotsView.setVisibility(View.GONE);
                            }
                        });
                    }
                    lockPermissionsHandler = null;
                }
            });
        }
    };

    void getLockPermissions() {
        if (MainTabBarPageFragment.currentPosition == thisPage) {
            threeDotsView.setVisibility(View.VISIBLE);
        } else {
            threeDotsView.setVisibility(View.GONE);
        }
        userList.clear();
        selfIsOwner = false;
        if(lockPermissionsHandler == null){
            lockPermissionsHandler = new Handler();
            lockPermissionsHandler.post(lockPermissionsRunnable);
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

    @Override
    public void onStop() {
        super.onStop();
        if(lockPermissionsHandler != null){
            lockPermissionsHandler.removeCallbacks(lockPermissionsRunnable);
            lockPermissionsHandler = null;
        }
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
        if (object instanceof String && object.equals(MainTabBarPageFragment.CLOSE_PAGES_IN_USERS_SCREEN)) {
            if (getFragmentManager().findFragmentById(R.id.users_page_root) != null) {
                getFragmentManager().popBackStack();
            }
        }
        if (object instanceof String && object.equals(REFRESH_LIST_OF_USERS)) {
            getLockPermissions();
        }
    }
}

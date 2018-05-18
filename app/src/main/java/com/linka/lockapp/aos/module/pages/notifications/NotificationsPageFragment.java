package com.linka.lockapp.aos.module.pages.notifications;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.NotificationListAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.Notification;
import com.linka.lockapp.aos.module.widget.SuperBackToTopRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 17/2/16.
 */
public class NotificationsPageFragment extends CoreFragment {

    Linka linka;

    @BindView(R.id.recycler_view)
    SuperBackToTopRecyclerView recyclerView;
    @BindView(R.id.loading)
    RelativeLayout loading;

    Unbinder unbinder;

    boolean shouldLoadSettings = true;
    View.OnClickListener mOnClickListener;

    public static NotificationsPageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        NotificationsPageFragment fragment = new NotificationsPageFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public NotificationsPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications_page, container, false);
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
            init(savedInstanceState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null)
        {
            adapter.context = null;
        }
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recyclerView != null) {
            outState.putParcelable("ss", recyclerView.getRecyclerView().getLayoutManager().onSaveInstanceState());
        }
    }

    List<Notification> notifications = new ArrayList<>();
    NotificationListAdapter adapter = new NotificationListAdapter(getActivity());


    void init(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (recyclerView != null) {
                Parcelable ss = savedInstanceState.getParcelable("ss");
                recyclerView.getRecyclerView().getLayoutManager().onRestoreInstanceState(ss);
            }
        }

        if (recyclerView != null && recyclerView.getRecyclerView().getLayoutManager() == null)
        {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
        }

        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shouldLoadSettings = true;
                fetch_activities();
            }
        });

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getTag() instanceof Notification){
                    onItemClick((Notification) v.getTag());
                }else{
                    //no correct data
                }

            }
        };
        adapter.setOnclickListener(mOnClickListener);
        recyclerView.setAdapter(adapter);


        adapter.setLoadMore(false);


        refresh();
    }

    private void onItemClick(Notification notification){
        if(TextUtils.isEmpty(notification.longitude)||TextUtils.isEmpty(notification.latitude)){
            return;
        }
        try {
            Float latitude = Float.parseFloat(notification.latitude);
            Float longitude = Float.parseFloat(notification.longitude);
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            getActivity().startActivity(intent);
        }catch (Exception ex){

        }
    }




    void refresh() {
        if (!isAdded()) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;
                if (recyclerView == null)
                {
                    return;
                }

                if (adapter == null)
                {
                    return;
                }

                recyclerView.getSwipeToRefresh().setRefreshing(false);
                List<LinkaActivity> activities = LinkaActivity.getLinkaActivitiesByLinka(linka);
                notifications = Notification.fromLinkaActivities(activities);

                adapter.setList(notifications);
                adapter.notifyDataSetChanged();

                if (shouldLoadSettings)
                {
                    fetch_activities();
                }
            }
        });
    }




    void fetch_activities()
    {
        if (!isAdded()) return;

        if (linka == null) {
            return;
        }
        if(getAppMainActivity().isNetworkAvailable()) {
            LinkaAPIServiceImpl.fetch_activities(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.ActivitiesResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse.ActivitiesResponse> call, Response<LinkaAPIServiceResponse.ActivitiesResponse> response) {
                    if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                        shouldLoadSettings = false;
                        LinkaAPIServiceResponse.ActivitiesResponse body = response.body();
                        List<LinkaActivity> activities = new ArrayList<LinkaActivity>();

                        if (body == null || body.data == null) {
                            return;
                        }

                        for (LinkaAPIServiceResponse.ActivitiesResponse.Data data : body.data) {
                            LinkaActivity activity = data.makeLinkaActivity(linka);
                            activities.add(activity);
                        }

                        LinkaActivity.saveAndOverwriteActivities(activities, linka);
                        refresh();
                    }
                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse.ActivitiesResponse> call, Throwable t) {

                }
            });
        }
    }





    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }


    @Subscribe
    public void onEvent(Object object) {
        if (object != null && object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {
            refresh();
        }
    }
}
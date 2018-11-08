package com.linka.lockapp.aos.module.pages.notifications;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.adapters.NotificationListAdapter;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.Notification;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.widget.SuperBackToTopRecyclerView;
import com.linka.lockapp.aos.module.widget.ThreeDotsView;

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

    @BindView(R.id.three_dots)
    ThreeDotsView threeDotsView;

    @BindView(R.id.textView)
    TextView noRecordsText;

    Unbinder unbinder;

    boolean shouldLoadSettings = false;
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
            init(savedInstanceState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            if (adapter.isUnreadItemExist()) {
                adapter.updateReadState();
            }
            adapter.context = null;
        }
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recyclerView != null) {
            outState.putParcelable("ss", recyclerView.getRecyclerView().getLayoutManager().onSaveInstanceState());
        }
    }

    List<Notification> notifications = new ArrayList<>();
    NotificationListAdapter adapter;


    void init(Bundle savedInstanceState) {
        adapter = new NotificationListAdapter(getActivity());

        if (savedInstanceState != null) {
            if (recyclerView != null && recyclerView.getRecyclerView()!=null ) {
                Parcelable ss = savedInstanceState.getParcelable("ss");
                if (recyclerView.getRecyclerView().getLayoutManager()==null) {
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                }
                recyclerView.getRecyclerView().getLayoutManager().onRestoreInstanceState(ss);
            }
        }

        if (recyclerView != null && recyclerView.getRecyclerView().getLayoutManager() == null) {
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
                if (v.getTag() instanceof Notification) {
                    onItemClick((Notification) v.getTag());
                } else {
                    //no correct data
                }

            }
        };
        adapter.setOnclickListener(mOnClickListener);
        recyclerView.setAdapter(adapter);


        adapter.setLoadMore(false);

        refresh();
      //  fetch_activities();
    }

    private void onItemClick(Notification notification) {
        if (TextUtils.isEmpty(notification.longitude) || TextUtils.isEmpty(notification.latitude)) {
            return;
        }
        try {
            Float latitude = Float.parseFloat(notification.latitude);
            Float longitude = Float.parseFloat(notification.longitude);
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            getActivity().startActivity(intent);
        } catch (Exception ex) {

        }
    }

    private void refresh(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                  if (linka != null && linka.getId() != null) {
                List<LinkaActivity> activities = LinkaActivity.getLinkaActivitiesByLinka(linka);
                notifications = Notification.fromLinkaActivities(activities);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) return;
                        if (recyclerView == null) {
                            return;
                        }

                        if (adapter == null) {
                            return;
                        }

                        recyclerView.getSwipeToRefresh().setRefreshing(false);

                        adapter.setList(notifications);
                        adapter.notifyDataSetChanged();
                        if(notifications.isEmpty()){
                            threeDotsView.setVisibility(View.INVISIBLE);
                            noRecordsText.setVisibility(View.VISIBLE);
                        }else {
                            threeDotsView.setVisibility(View.VISIBLE);
                            noRecordsText.setVisibility(View.INVISIBLE);
                        }

                        if (shouldLoadSettings) {
                            fetch_activities();
                        }
                    }
                });
            }
            }
        }).start();
    }

    private FetchTask fetchTask = null;
    private boolean isFetchTaskRunning = false;

    void fetch_activities() {
        if (!isAdded()) return;

        if (linka == null || isFetchTaskRunning) {
            return;
        }
        isFetchTaskRunning = true;
        if (getAppMainActivity().isNetworkAvailable()) {
            LinkaAPIServiceImpl.fetch_activities(getAppMainActivity(), linka, new Callback<LinkaAPIServiceResponse.ActivitiesResponse>() {
                @Override
                public void onResponse(Call<LinkaAPIServiceResponse.ActivitiesResponse> call, final Response<LinkaAPIServiceResponse.ActivitiesResponse> response) {
                    if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                        fetchTask = new FetchTask();
                        fetchTask.execute(response);
                    }
                }

                @Override
                public void onFailure(Call<LinkaAPIServiceResponse.ActivitiesResponse> call, Throwable t) {
                    isFetchTaskRunning = false;
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

    @Override
    public void onStop() {
        super.onStop();
        if (isFetchTaskRunning && fetchTask != null) {
            fetchTask.cancel(true);
        }
    }

    @Subscribe
    public void onEvent(Object object) {
        if (object != null && object instanceof String) {
            if (object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE) && MainTabBarPageFragment.currentPosition == MainTabBarPageFragment.NOTIFICATION_SCREEN) {
                refresh();
            } else if (((String) object).substring(0, 8).equals("Selected")) {
                if (object.equals("Selected-" + String.valueOf(MainTabBarPageFragment.NOTIFICATION_SCREEN))) {
                    refresh();
                } else if (adapter.isUnreadItemExist()) {
                    adapter.updateReadState();
                    refresh();
                }
            } else if (object.equals(MainTabBarPageFragment.UPDATE_NOTIFICATIONS)) {
                if (MainTabBarPageFragment.currentPosition == MainTabBarPageFragment.NOTIFICATION_SCREEN) {
                    refresh();
                }
            }
        }
    }

    private class FetchTask extends AsyncTask<Response<LinkaAPIServiceResponse.ActivitiesResponse>, Void, Void> {

        @Override
        protected Void doInBackground(Response<LinkaAPIServiceResponse.ActivitiesResponse>... responses) {
            shouldLoadSettings = false;
            LinkaAPIServiceResponse.ActivitiesResponse body = responses[0].body();
            List<LinkaActivity> activities = new ArrayList<>();

            if (body == null || body.data == null) {
                return null;
            }

            for (LinkaAPIServiceResponse.ActivitiesResponse.Data data : body.data) {
                LinkaActivity activity = data.makeLinkaActivity(linka);
                activities.add(activity);
            }

            LinkaActivity.saveAndOverwriteActivities(activities, linka);

            List<LinkaActivity> activities2 = LinkaActivity.getLinkaActivitiesByLinka(linka);
            notifications = Notification.fromLinkaActivities(activities2);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) return;
                    if (recyclerView == null) {
                        return;
                    }

                    if (adapter == null) {
                        return;
                    }

                    recyclerView.getSwipeToRefresh().setRefreshing(false);
                    if(notifications.isEmpty()){
                        threeDotsView.setVisibility(View.INVISIBLE);
                        noRecordsText.setVisibility(View.VISIBLE);
                    }else {
                        threeDotsView.setVisibility(View.VISIBLE);
                        noRecordsText.setVisibility(View.INVISIBLE);
                    }
                    adapter.setList(notifications);
                    adapter.notifyDataSetChanged();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fetchTask = null;
            isFetchTaskRunning = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            fetchTask = null;
            isFetchTaskRunning = false;
        }
    }
}
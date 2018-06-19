package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.eventbus.SuccessConnectBusEventMessage;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.other.Utils;
import com.linka.lockapp.aos.module.pages.dialogs.SuccessConnectionDialogFragment;
import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AccessLockFragment extends CoreFragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.send_button)
    TextView send;
    @BindView(R.id.back_button)
    ImageView back;
    @BindView(R.id.root)
    ConstraintLayout root;


    private Unbinder unbinder;

    public static AccessLockFragment newInstance(Linka linka) {

        Bundle args = new Bundle();
        args.putSerializable(LINKA_ARGUMENT,linka);
        AccessLockFragment fragment = new AccessLockFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_access_lock, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);
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
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.send_button)
    void onSendClicked(){
        trySendUserPermissionRequest(((Linka) getArguments().getSerializable(LINKA_ARGUMENT)));
    }

    @OnClick(R.id.back_button)
    void onBackClicked(){
        getAppMainActivity().popFragment();
    }

    void trySendUserPermissionRequest(Linka linka) {
        setBlur(true);
        final ThreeDotsDialogFragment threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance();
        threeDotsDialogFragment.show(getFragmentManager(), null);
        LinkaAPIServiceImpl.send_request_for_user_permission(getActivity(), linka, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                threeDotsDialogFragment.dismiss();
                if (!isAdded()) return;
                if (LinkaAPIServiceImpl.check(response, false, getActivity())) {
                    SuccessConnectionDialogFragment.newInstance(getString(R.string.request_sent)).show(getFragmentManager(),null);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                setBlur(false);
                threeDotsDialogFragment.dismiss();
            }
        });
    }

    public void setBlur(boolean isBlur){
        if(isBlur){
            Utils.getInstance(getActivity()).showLoading(root);
        }else {
            Utils.getInstance(getActivity()).cancelLoading();
        }
    }

    @Subscribe
    public void dialogClosed(SuccessConnectBusEventMessage connectBusEventMessage) {
        setBlur(false);
        getAppMainActivity().popFragment();
    }

}

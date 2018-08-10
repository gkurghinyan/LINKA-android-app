package com.linka.lockapp.aos.module.pages.users;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.User;
import com.linka.lockapp.aos.module.other.Utils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NeedApprovalUserFragment extends Fragment {
    private static final String USER_ARGUMENT = "UserArgument";
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.root)
    ConstraintLayout root;

    @BindView(R.id.user_name)
    TextView userName;

    @BindView(R.id.used_date)
    TextView usedDate;

    @BindView(R.id.grant_button)
    TextView grantButton;

    private Unbinder unbinder;
    private User user;

    public static NeedApprovalUserFragment newInstance(User user, Linka linka) {
        Bundle args = new Bundle();
        args.putSerializable(USER_ARGUMENT, user);
        args.putSerializable(LINKA_ARGUMENT, linka);
        NeedApprovalUserFragment fragment = new NeedApprovalUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_need_approval_user, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        user = ((User) getArguments().getSerializable(USER_ARGUMENT));
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init() {
        userName.setText(user.name);
        usedDate.setText(user.lastUsed);
    }

    @OnClick(R.id.grant_button)
    void onClickGrantButton() {
        grantButton.setClickable(false);
        inviteUser();
    }

    @OnClick(R.id.deny_button)
    void onClickDenyButton() {
        revokeAccess();
    }

    private void inviteUser() {
        Utils.showLoading(getActivity(),root);
        LinkaAPIServiceImpl.send_invite(getActivity(), ((Linka) getArguments().getSerializable(LINKA_ARGUMENT)), user.userId, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    Utils.cancelLoading();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Access Granted").
                            setMessage("This user can now lock and unlock your bike.")
                            .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getFragmentManager().popBackStack();
                                    EventBus.getDefault().post(SharingPageFragment.REFRESH_LIST_OF_USERS);
                                }
                            });
                    builder.create().show();
                }else {
                    Utils.cancelLoading();
                }
                grantButton.setClickable(true);
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                Utils.cancelLoading();
                grantButton.setClickable(true);
            }
        });
    }

    private void revokeAccess() {
        LinkaAPIServiceImpl.revoke_access(getActivity(), ((Linka) getArguments().getSerializable(LINKA_ARGUMENT)), user.userId, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Access Denied").
                            setMessage("The user has been notified.")
                            .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getFragmentManager().popBackStack();
                                    EventBus.getDefault().post(SharingPageFragment.REFRESH_LIST_OF_USERS);
                                }
                            });
                    builder.create().show();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }
}

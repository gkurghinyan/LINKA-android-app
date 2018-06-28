package com.linka.lockapp.aos.module.pages.users;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.User;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestUserFragment extends Fragment {
    private static final String USER_ARGUMENT = "UserArgument";
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.user_name)
    TextView userName;

    @BindView(R.id.used_date)
    TextView usedDate;

    private Unbinder unbinder;
    private User user;

    public static GuestUserFragment newInstance(User user, Linka linka) {
        Bundle args = new Bundle();
        args.putSerializable(USER_ARGUMENT, user);
        args.putSerializable(LINKA_ARGUMENT, linka);
        GuestUserFragment fragment = new GuestUserFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guest_user, container, false);
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

    @OnClick(R.id.revoke_button)
    void onClickRevokeButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure?").
                setMessage("This user will no longer have access to your bike.").
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Revoke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        revokeAccess();
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.make_owner_button)
    void onClickMakeOwnerButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Transfer ownership?").
                setMessage("You will no longer have master control of this lock and its settings.").
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Transfer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        transferOwnership();
                    }
                });
        builder.create().show();
    }

    private void revokeAccess() {
        LinkaAPIServiceImpl.revoke_access(getActivity(), ((Linka) getArguments().getSerializable(LINKA_ARGUMENT)), user.userId, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    getFragmentManager().popBackStack();
                    EventBus.getDefault().post(SharingPageFragment.REFRESH_LIST_OF_USERS);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }

    private void transferOwnership() {
        Toast.makeText(getActivity(), "Transferring Ownership", Toast.LENGTH_SHORT).show();
        LinkaAPIServiceImpl.transfer_ownership(getActivity(), ((Linka) getArguments().getSerializable(LINKA_ARGUMENT)), user.userId, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    getFragmentManager().popBackStack();
                    EventBus.getDefault().post(SharingPageFragment.REFRESH_LIST_OF_USERS);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
    }

}

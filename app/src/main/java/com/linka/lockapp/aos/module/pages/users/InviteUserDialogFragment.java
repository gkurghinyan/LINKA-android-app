package com.linka.lockapp.aos.module.pages.users;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.model.Linka;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InviteUserDialogFragment extends Fragment {
    private static final String EMAIL_ADDRESSES = "EmailAddresses";
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.email_edit)
    EditText emailEdit;
    @BindView(R.id.cancel_button)
    TextView cancel;
    @BindView(R.id.invite_button)
    TextView invite;

    private static final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    public static InviteUserDialogFragment newInstance(ArrayList<String> emails, Linka linka) {
        Bundle args = new Bundle();
        args.putStringArrayList(EMAIL_ADDRESSES,emails);
        args.putSerializable(LINKA_ARGUMENT,linka);
        InviteUserDialogFragment fragment = new InviteUserDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invite_user_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        emailEdit.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @OnClick(R.id.cancel_button)
    void onCancelClicked(){
        getFragmentManager().popBackStack();
    }

    @OnClick(R.id.invite_button)
    void onInviteClicked(){
        if(!emailEdit.getText().toString().equals("") && emailEdit.getText().toString().matches(emailPattern)) {
            if(!isUserExisting(Objects.requireNonNull(getArguments().getStringArrayList(EMAIL_ADDRESSES)))) {
                inviteUser(emailEdit.getText().toString());
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }else {
                Toast.makeText(getActivity(), getString(R.string.user_exist), Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getActivity(), "Not valid email", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUserExisting(ArrayList<String> users){
        boolean isExist = false;
        String email = emailEdit.getText().toString();
        for (String user:users){
            if(email.equals(user)){
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    private void inviteUser(String email) {
        LinkaAPIServiceImpl.send_invite_with_email(getActivity(), ((Linka) getArguments().getSerializable(LINKA_ARGUMENT)), email, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Invite Sent").
                            setMessage("This user will appear in your Guest Users list once they create a LINKA account.").
                            setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getFragmentManager().popBackStack();
                                    EventBus.getDefault().post(SharingPageFragment.REFRESH_LIST_OF_USERS);
                                }
                            })
                            .setPositiveButton("Invite another", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    emailEdit.setText("");
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

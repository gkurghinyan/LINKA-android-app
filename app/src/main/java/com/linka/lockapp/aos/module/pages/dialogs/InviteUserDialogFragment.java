package com.linka.lockapp.aos.module.pages.dialogs;


import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.eventbus.InviteUserBusEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class InviteUserDialogFragment extends Fragment {
    private static final String EMAIL_ADDRESSES = "EmailAddresses";
    @BindView(R.id.name_edit)
    EditText nameEdit;
    @BindView(R.id.email_edit)
    EditText emailEdit;
    @BindView(R.id.cancel_button)
    TextView cancel;
    @BindView(R.id.invite_button)
    TextView invite;

    private static final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    public static InviteUserDialogFragment newInstance(ArrayList<String> emails) {
        
        Bundle args = new Bundle();
        args.putStringArrayList(EMAIL_ADDRESSES,emails);
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
        nameEdit.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
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
                getFragmentManager().popBackStack();
                EventBus.getDefault().post(new InviteUserBusEvent(emailEdit.getText().toString()));
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

}

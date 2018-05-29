package com.linka.lockapp.aos.module.pages.dialogs;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.eventbus.InviteUserBusEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class InviteUserDialogFragment extends Fragment {
    @BindView(R.id.name_edit)
    EditText nameEdit;
    @BindView(R.id.email_edit)
    EditText emailEdit;
    @BindView(R.id.cancel_button)
    TextView cancel;
    @BindView(R.id.invite_button)
    TextView invite;

    public static InviteUserDialogFragment newInstance() {
        
        Bundle args = new Bundle();
        
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
        if(!emailEdit.getText().toString().equals("")) {
            getFragmentManager().popBackStack();
            EventBus.getDefault().post(new InviteUserBusEvent(emailEdit.getText().toString()));
        }else {
            Toast.makeText(getActivity(), "Not valid email", Toast.LENGTH_SHORT).show();
        }
    }

}

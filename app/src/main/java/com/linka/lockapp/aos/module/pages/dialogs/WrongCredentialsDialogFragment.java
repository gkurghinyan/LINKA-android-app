package com.linka.lockapp.aos.module.pages.dialogs;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.eventbus.WrongCredentialsBusEventMessage;

import org.greenrobot.eventbus.EventBus;

public class WrongCredentialsDialogFragment extends DialogFragment {

    public static WrongCredentialsDialogFragment newInstance() {
        WrongCredentialsDialogFragment fragment = new WrongCredentialsDialogFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wrong_credentials_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new WrongCredentialsBusEventMessage(WrongCredentialsBusEventMessage.CLOSE));
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getDialog().getWindow().setLayout(width-width/4,height-height/2);
    }
}

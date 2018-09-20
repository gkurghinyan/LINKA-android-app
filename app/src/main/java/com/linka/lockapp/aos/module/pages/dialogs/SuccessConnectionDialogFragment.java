package com.linka.lockapp.aos.module.pages.dialogs;


import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.eventbus.SuccessConnectBusEventMessage;

import org.greenrobot.eventbus.EventBus;

public class SuccessConnectionDialogFragment extends BaseDialogFragment {
    private static final String DIALOG_MESSAGE = "DialogMessage";
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler = null;
            getDialog().dismiss();
        }
    };

    public static SuccessConnectionDialogFragment newInstance(String message) {

        Bundle args = new Bundle();
        args.putString(DIALOG_MESSAGE,message);
        SuccessConnectionDialogFragment fragment = new SuccessConnectionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_success_connection_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.text)).setText(getArguments().getString(DIALOG_MESSAGE));

        view.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler();
        handler.postDelayed(runnable,5000);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(handler != null){
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ConstraintLayout root = new ConstraintLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getDialog().getWindow().setLayout(width - width / 3 - height/20, height - height / 2 - height/12);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new SuccessConnectBusEventMessage());
    }
}

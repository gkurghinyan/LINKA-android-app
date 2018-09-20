package com.linka.lockapp.aos.module.pages.dialogs;


import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.linka.lockapp.aos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ThreeDotsDialogFragment extends BaseDialogFragment {
    private boolean isSetConnecting = false;
    private String text;
    @BindView(R.id.connecting_text)
    TextView connecting;

    private Unbinder unbinder;

    public static ThreeDotsDialogFragment newInstance() {
        return new ThreeDotsDialogFragment();
    }

    public ThreeDotsDialogFragment setConnectingText(boolean isSet,String text){
        isSetConnecting = isSet;
        this.text = text;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_three_dots_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);
        if(isSetConnecting){
            connecting.setVisibility(View.VISIBLE);
            connecting.setText(text);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ConstraintLayout root = new ConstraintLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setCancelable(false);

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

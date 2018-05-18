package com.linka.lockapp.aos.module.core;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;

/**
 * Created by van on 13/7/15.
 */
public class CoreDialogFragment extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }



}
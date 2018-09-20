package com.linka.lockapp.aos.module.pages.dialogs;

import android.support.v4.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {
    private boolean isDialogStop;
    private boolean isDialogTryDismiss;


    @Override
    public void dismiss() {
        if (!isDialogStop) {
            super.dismiss();
        } else {
            isDialogTryDismiss = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isDialogStop = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isDialogStop = false;
        if (isDialogTryDismiss) {
            dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isDialogStop = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        isDialogStop = false;
    }

}

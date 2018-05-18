package com.linka.lockapp.aos.module.pages.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LockController;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Vanson on 6/8/2016.
 */
public class RevocationController {

    public static class RevocationRevokeAccessKeyNotification
    {
        public String reactivation_key;
    }

    public static class RevocationResetFactorySettingsNotification
    {
        public String reactivation_key;
    }


    public void onResume()
    {
        EventBus.getDefault().register(this);
    }

    public void onPause()
    {
        EventBus.getDefault().unregister(this);
    }


    public static final String NOTIFICATION_REVOKE_ACCESS_KEY = "NOTIFICATION_REVOKE_ACCESS_KEY";
    public static final String NOTIFICATION_RESET_FACTORY_SETTINGS = "NOTIFICATION_RESET_FACTORY_SETTINGS";

    Context context;
    Linka linka;
    LockController lockController;

    ProgressDialog progressDialog;
    AlertDialog alertDialog;

    public void implement(Context context, Linka linka, LockController lockController) {
        this.context = context;
        this.linka = linka;
        this.lockController = lockController;
    }

    public void showLoading(String title, String message)
    {
        hideLoading();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void showLoading(String title, String message, String cancel, final DialogInterface.OnClickListener onCancelListener)
    {
        hideLoading();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setButton(cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onCancelListener.onClick(dialog, which);
                return;
            }
        });
        progressDialog.show();
    }

    public void hideLoading()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }



    public void showAlert(String title, String message, String cancel, DialogInterface.OnClickListener onCancel)
    {
        hideAlert();
        alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(cancel, onCancel)
                .setCancelable(false)
                .show();
    }

    public void showDialog(String title, String message, String yes, DialogInterface.OnClickListener yesClick, String no, DialogInterface.OnClickListener noClick)
    {
        hideAlert();
        alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(yes, yesClick)
                .setNegativeButton(no, noClick)
                .setCancelable(false)
                .show();
    }

    public void hideAlert()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }


}

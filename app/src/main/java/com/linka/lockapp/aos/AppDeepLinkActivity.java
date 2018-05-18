package com.linka.lockapp.aos;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.linka.lockapp.aos.module.pages.settings.RevocationController.RevocationResetFactorySettingsNotification;
import com.linka.lockapp.aos.module.pages.settings.RevocationController.RevocationRevokeAccessKeyNotification;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Vanson on 6/8/2016.
 */
public class AppDeepLinkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        String scheme = data.getScheme();
        String host = data.getHost();

        final String action = data.getQueryParameter("action");
        final String reactivation_key = data.getQueryParameter("reactivation_key");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("action", action + " " + reactivation_key);

                if (action != null && reactivation_key != null && !reactivation_key.equals("")) {
                    if (action.equals("resetfactory")) {
                        RevocationResetFactorySettingsNotification notification = new RevocationResetFactorySettingsNotification();
                        notification.reactivation_key = reactivation_key;
                        EventBus.getDefault().post(notification);
                    } else if (action.equals("revoke")) {
                        RevocationRevokeAccessKeyNotification notification = new RevocationRevokeAccessKeyNotification();
                        notification.reactivation_key = reactivation_key;
                        EventBus.getDefault().post(notification);
                    }
                }

                finish();
            }
        }, 1000);

    }
}

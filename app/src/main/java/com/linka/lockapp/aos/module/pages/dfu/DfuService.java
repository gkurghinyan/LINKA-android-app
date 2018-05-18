package com.linka.lockapp.aos.module.pages.dfu;

import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Created by Vanson on 6/8/2016.
 */

public class DfuService extends DfuBaseService {

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return NotificationActivity.class;
    }
}
package com.linka.Lock.Utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Loren-Admin on 8/27/2015.
 */
public class AndroidHelper {
    public static String getAppVersion (Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //Handle exception
            return "[Unknown]";
        }
    }
}

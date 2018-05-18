package com.linka.lockapp.aos.module.helpers;

import android.util.Log;


/**
 * Created by Vanson on 20/4/2016.
 */
public class LogHelper {
    public String prefix;
    public String suffix;
    public static LogLevel logLevel = LogLevel.DEBUG;

    public enum LogLevel {
        NONE,
        ERROR,
        INFO,
        DEBUG
    }

    public static void e(String prefix, String suffix) {
        switch (logLevel) {
            case ERROR:
            case INFO:
            case DEBUG:
                Log.e("LK-" + prefix, suffix);
                LogHelper logHelper = new LogHelper();
                logHelper.prefix = prefix;
                logHelper.suffix = suffix;
                break;
            case NONE:
            default:
                // Do nothing
        }
    }

    public static void i(String prefix, String suffix) {
        switch (logLevel) {
            case INFO:
            case DEBUG:
                Log.i("LK-" + prefix, suffix);
                LogHelper logHelper = new LogHelper();
                logHelper.prefix = prefix;
                logHelper.suffix = suffix;
                break;
            case ERROR:
            case NONE:
            default:
                // Do nothing
        }
    }

    public static void d(String prefix, String suffix) {
        switch (logLevel) {
            case DEBUG:
                Log.i("LK-" + prefix, suffix);
                LogHelper logHelper = new LogHelper();
                logHelper.prefix = prefix;
                logHelper.suffix = suffix;
                break;
            case ERROR:
            case INFO:
            case NONE:
            default:
                // Do nothing
        }
    }
}


package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public final class DimensionUtils {

    public DimensionUtils() {
    }

    public static float converPixelsToSp(float px, Context context) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

    //From https://stackoverflow.com/questions/4605527/converting-pixels-to-dp

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}

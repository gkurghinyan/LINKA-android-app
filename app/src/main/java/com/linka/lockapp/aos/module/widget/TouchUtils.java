package com.linka.lockapp.aos.module.widget;

import android.view.MotionEvent;
import android.view.View;

public final class TouchUtils {
    public TouchUtils() {
    }

    public static boolean isTouchOutsideInitialPosition(MotionEvent event, View view) {

        boolean isOutsideHeightShort = event.getY() > view.getY() + view.getHeight();
        boolean isOutsideHeightLong = event.getY() < view.getY() ;
        boolean isOutsideWidthShort = event.getX() > view.getX() + view.getWidth();
        boolean isOutsideWidthLong = event.getX() < view.getX();

        return  isOutsideHeightLong || isOutsideHeightShort || isOutsideWidthLong || isOutsideWidthShort;
    }
}

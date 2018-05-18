package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Vanson on 17/11/15.
 */
public class ToggleSwipeableViewPager extends ViewPager {

    public boolean isSwipable = true;

    public ToggleSwipeableViewPager(Context context) {
        super(context);
    }

    public ToggleSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        if (!isSwipable) return false;
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        if (!isSwipable) return false;
        return super.onTouchEvent(event);
    }
}
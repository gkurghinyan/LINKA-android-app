package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;


public class LinkaTouchableLinearLayout extends LinearLayout {
    public LinkaTouchableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        getAttrs(attrs);
    }

    public LinkaTouchableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        getAttrs(attrs);
    }

    public LinkaTouchableLinearLayout(Context context) {
        super(context);
        init();
    }

    private void init() {

    }

    void getAttrs(AttributeSet attrs) {

    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int maskedAction = event.getActionMasked();
        if (maskedAction == MotionEvent.ACTION_DOWN) {
            if (getBackground() != null) getBackground().setColorFilter(Color.argb(100, 155, 155, 155), PorterDuff.Mode.DST_IN);
        }
        else if (maskedAction == MotionEvent.ACTION_UP
                || maskedAction == MotionEvent.ACTION_CANCEL) {
            if (getBackground() != null) getBackground().setColorFilter(null);
        }
        if (getBackground() != null) getBackground().invalidateSelf();
        return super.onTouchEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refreshState();
    }

    void refreshState()
    {
        if (!isEnabled())
        {
            setAlpha(0.2f);
        }
        else
        {
            setAlpha(1.0f);
        }
    }

}

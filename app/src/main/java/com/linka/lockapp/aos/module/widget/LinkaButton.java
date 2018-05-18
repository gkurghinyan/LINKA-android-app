package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.linka.lockapp.aos.R;


public class LinkaButton extends Button {
    private static final String CUSTOM_FONT_NAME = "Lato-Regular.ttf";

    public LinkaButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        getAttrs(attrs);
    }

    public LinkaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        getAttrs(attrs);
    }

    public LinkaButton(Context context) {
        super(context);
        init();
    }

    private void init() {

    }

    void getAttrs(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LinkaWidget);

        setFontFace(a);

        a.recycle();

    }


    void setFontFace(TypedArray a) {
        Typeface myTypeface;
        String fontName = (a != null) ? a.getString(R.styleable.LinkaWidget_fontName) : null;
        if (fontName != null) {
            myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);

        } else {
            myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + CUSTOM_FONT_NAME);

        }
        setTypeface(myTypeface);
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

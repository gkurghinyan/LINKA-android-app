package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Vanson on 17/2/16.
 */

public class LinkaTextViewRegular extends TextView {
    private static final String CUSTOM_FONT_NAME = "Lato-Regular.ttf";
    public LinkaTextViewRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public LinkaTextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public LinkaTextViewRegular(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+ CUSTOM_FONT_NAME);
        setTypeface(myTypeface);
    }

}

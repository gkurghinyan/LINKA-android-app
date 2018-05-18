package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Vanson on 21/8/15.
 */

public class AspectRatioImageView extends android.support.v7.widget.AppCompatImageView {

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getDrawable() == null) {
            setMeasuredDimension(0,0);
            return;
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
        setMeasuredDimension(width, height);
    }
}
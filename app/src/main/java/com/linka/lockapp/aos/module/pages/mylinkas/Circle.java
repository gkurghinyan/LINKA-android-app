package com.linka.lockapp.aos.module.pages.mylinkas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.linka.lockapp.aos.module.widget.DimensionUtils;

/**
 * Created by kyle on 2/19/18.
 */

public class Circle extends View {

    private static final int START_ANGLE_POINT = 270;

    public static float CIRCLE_SIZE_DP = 220;
    public static float CIRCLE_STROKE_WIDTH_DP = 30;

    private final Paint paint;
    private RectF rect;

    private float angle;
    public static int strokeWidth;
    public static int circleSize;

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);

        strokeWidth = (int) DimensionUtils.convertDpToPixel(CIRCLE_STROKE_WIDTH_DP, context);
        circleSize = (int) DimensionUtils.convertDpToPixel(CIRCLE_SIZE_DP, context);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.parseColor("#0878ce"));

        rect = new RectF();

        //Initial Angle (optional, it can be zero)
        angle = 10;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = canvas.getHeight();
        int width = canvas.getWidth() / 2;

        //size 200x200 example
        rect.set(width - circleSize/2 + strokeWidth/2, strokeWidth/2, width + circleSize/2 - strokeWidth/2, circleSize - strokeWidth/2);

        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setColor(int color){
        paint.setColor(color);
    }
}

package com.linka.lockapp.aos.module.pages.mylinkas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by kyle on 2/19/18.
 */

public class CircleBackground extends View {

    private static final int START_ANGLE_POINT = 270;

    private final Paint paint;
    private RectF rect;

    private float angle;

    public CircleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);

        final int strokeWidth = 100;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.parseColor("#32c967"));

        //size 200x200 example
        rect = new RectF();

        //Initial Angle (optional, it can be zero)
        angle = 365;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = canvas.getHeight() / 2;
        int width = canvas.getWidth() / 2;

        //size 200x200 example
        rect.set(width - 400, height - 400, width + 400, height + 400);

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

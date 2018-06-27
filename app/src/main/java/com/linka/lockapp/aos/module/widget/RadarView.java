package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.linka.lockapp.aos.R;

public class RadarView extends View {
    private Paint paint;
    private int screenWidth;
    private int screenHeight;
    private static final int strokeWidth = 3;
    private int firstCircleRadius;
    private int secondCircleRadius;
    private int thirdCircleRadius;
    private int fourthCircleRadius;
    private int fifthCircleRadius;
    private static int strokeColor;
    private static int checkedStrokeColor;
    private static int firstCircleColor;
    private static int secondCircleColor;
    private static int thirdCircleColor;
    private static int fourthCircleColor;
    private static int fifthCircleColor;

    private static int pressedCircle;
    public static final int FIRST_CIRCLE = 1;
    public static final int SECOND_CIRCLE = 2;
    public static final int THIRD_CIRCLE = 3;
    public static final int FOURTH_CIRCLE = 4;
    public static final int FIFTH_CIRCLE = 5;

    private Bitmap bitmap;
    private float bitmapProp;
    private RectF rectF;
    private OnRadarRadiusChangeListener radarRadiusChangeListener;


    public RadarView(Context context) {
        super(context);
        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        strokeColor = context.getResources().getColor(R.color.gray);
        checkedStrokeColor = context.getResources().getColor(R.color.linka_blue);
        firstCircleColor = context.getResources().getColor(R.color.firstCircle);
        secondCircleColor = context.getResources().getColor(R.color.secondCircle);
        thirdCircleColor = context.getResources().getColor(R.color.thirdCircle);
        fourthCircleColor = context.getResources().getColor(R.color.fourthCircle);
        fifthCircleColor = context.getResources().getColor(R.color.fifthCircle);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        pressedCircle = FIFTH_CIRCLE;
        bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.set_range_pin);
        rectF = new RectF();
        bitmapProp = ((float) bitmap.getHeight()) / ((float)bitmap.getWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        screenWidth = canvas.getWidth();
        screenHeight = canvas.getHeight();
        int padding = 30;
        if(screenWidth>screenHeight){
            padding = screenWidth - screenHeight;
        }
        firstCircleRadius = screenWidth / 2 - padding;
        secondCircleRadius = firstCircleRadius * 4 / 5;
        thirdCircleRadius = firstCircleRadius * 3 / 5;
        fourthCircleRadius = firstCircleRadius * 2 / 5;
        fifthCircleRadius = firstCircleRadius / 5;
        drawFillCircles(canvas);
        rectF.set(screenWidth/2 - fifthCircleRadius/bitmapProp,screenHeight/2 - fifthCircleRadius,screenWidth/2 + fifthCircleRadius/bitmapProp, screenHeight/2 + fifthCircleRadius);
        canvas.drawBitmap(bitmap,null,rectF,null);
        drawStrokeCircles(canvas);
        drawCheckedCircle(canvas);
    }

    private void drawFillCircles(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(firstCircleColor);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, firstCircleRadius, paint);
        paint.setColor(secondCircleColor);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, secondCircleRadius, paint);
        paint.setColor(thirdCircleColor);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, thirdCircleRadius, paint);
        paint.setColor(fourthCircleColor);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, fourthCircleRadius, paint);
        paint.setColor(fifthCircleColor);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, fifthCircleRadius, paint);
    }

    private void drawStrokeCircles(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(strokeColor);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, firstCircleRadius, paint);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, secondCircleRadius, paint);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, thirdCircleRadius, paint);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, fourthCircleRadius, paint);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, fifthCircleRadius, paint);
    }

    private void drawCheckedCircle(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(checkedStrokeColor);
        switch (pressedCircle) {
            case FIRST_CIRCLE:
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, firstCircleRadius, paint);
                break;
            case SECOND_CIRCLE:
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, secondCircleRadius, paint);
                break;
            case THIRD_CIRCLE:
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, thirdCircleRadius, paint);
                break;
            case FOURTH_CIRCLE:
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, fourthCircleRadius, paint);
                break;
            case FIFTH_CIRCLE:
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, fifthCircleRadius, paint);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean mustInvalidate = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mustInvalidate = getCheckedPosition(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mustInvalidate = getCheckedPosition(event.getX(), event.getY());
                break;
        }
        if (mustInvalidate) {
            invalidate();
            if(radarRadiusChangeListener != null){
                radarRadiusChangeListener.radiusChanged(pressedCircle);
            }
        }
        return true;
    }

    private boolean getCheckedPosition(float x, float y) {
        int firstSide = Math.abs(screenWidth / 2 - Math.round(x));
        int secondSide = Math.abs(screenHeight / 2 - Math.round(y));

        int circle = 0;
        double radius = Math.sqrt((firstSide * firstSide) + (secondSide * secondSide));
        if (radius < fifthCircleRadius) {
            circle = FIFTH_CIRCLE;
        } else if (radius < fourthCircleRadius) {
            circle = FOURTH_CIRCLE;
        } else if (radius < thirdCircleRadius) {
            circle = THIRD_CIRCLE;
        } else if (radius < secondCircleRadius) {
            circle = SECOND_CIRCLE;
        } else if (radius < firstCircleRadius) {
            circle = FIRST_CIRCLE;
        }
        if (circle == pressedCircle || circle == 0) {
            return false;
        } else {
            pressedCircle = circle;
            return true;
        }
    }

    public interface OnRadarRadiusChangeListener {
        void radiusChanged(int radius);
    }

    public void setOnRadarRadiusChangeListener(OnRadarRadiusChangeListener radarRadiusChangeListener){
        this.radarRadiusChangeListener = radarRadiusChangeListener;
    }

    public void setCurrentRadius(int radius){
        if (radius != pressedCircle) {
            pressedCircle = radius;
            invalidate();
        }
    }
}

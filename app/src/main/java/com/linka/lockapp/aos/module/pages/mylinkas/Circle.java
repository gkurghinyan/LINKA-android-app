package com.linka.lockapp.aos.module.pages.mylinkas;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.widget.DimensionUtils;

/**
 * Created by kyle on 2/19/18.
 */

public class Circle extends View {
    public static final int NO_CONNECTION_STATE = 0;
    public static final int UNLOCKED_STATE = 1;
    public static final int LOCKED_STATE = 2;
    public static final int UNLOCKING_STATE = 3;
    public static final int LOCKING_STATE = 4;

    private int currentState = NO_CONNECTION_STATE;

    private static final int START_ANGLE_POINT = 270;

    public static float CIRCLE_SIZE_DP = 200;

    private final Paint paint;
    private RectF rect;

    private float angle;
    public static int circleSize;
    private Bitmap lockedLinka;
    private Bitmap unlockedLinka;
    private float bitmapProp;

    private ObjectAnimator animator;

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);

        circleSize = (int) DimensionUtils.convertDpToPixel(CIRCLE_SIZE_DP, context);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        rect = new RectF();
        angle = 0;
        lockedLinka = BitmapFactory.decodeResource(getResources(), R.drawable.close_white_linka);
        unlockedLinka = BitmapFactory.decodeResource(getResources(),R.drawable.open_white_linka);
        bitmapProp = ((float) lockedLinka.getHeight()) / ((float) lockedLinka.getWidth());

        animator = ObjectAnimator.ofFloat(this, "angle", 360).setDuration(3200);
        animator.setRepeatCount(0);
        animator.setInterpolator(null);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth() / 2;

        switch (currentState) {
            case NO_CONNECTION_STATE:
                paint.setColor(getResources().getColor(R.color.panic_color));
                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
                canvas.drawOval(rect, paint);
                rect.set(width - (circleSize - 120)/(2*bitmapProp),60,width + (circleSize - 120)/(2*bitmapProp),circleSize - 60);
                canvas.drawBitmap(lockedLinka,null,rect,null);
                break;
            case UNLOCKED_STATE:
                paint.setColor(getResources().getColor(R.color.unlocked_green));
                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
                canvas.drawOval(rect, paint);
                rect.set(width - (circleSize - 120)/(2*bitmapProp),60,width + (circleSize - 120)/(2*bitmapProp),circleSize - 60);
                canvas.drawBitmap(unlockedLinka,null,rect,null);
                break;
            case LOCKED_STATE:
                paint.setColor(getResources().getColor(R.color.locked_red));
                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
                canvas.drawOval(rect, paint);
                rect.set(width - (circleSize - 120)/(2*bitmapProp),60,width + (circleSize - 120)/(2*bitmapProp),circleSize - 60);
                canvas.drawBitmap(lockedLinka,null,rect,null);
                break;
            case LOCKING_STATE:
                paint.setColor(getResources().getColor(R.color.unlocked_green));
                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
                canvas.drawOval(rect, paint);
                paint.setColor(getResources().getColor(R.color.locked_red));
                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
                canvas.drawArc(rect, START_ANGLE_POINT, angle, true, paint);
                break;
            case UNLOCKING_STATE:
                paint.setColor(getResources().getColor(R.color.unlocked_green));
                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
                canvas.drawOval(rect, paint);
//                paint.setColor(getResources().getColor(R.color.unlocked_green));
//                rect.set(width - circleSize / 2, 0, width + circleSize / 2, circleSize);
//                canvas.drawArc(rect, START_ANGLE_POINT, angle, true, paint);
                break;
        }
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        invalidate();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void drawState(int state) {
        if(currentState != state) {
            if ((state == UNLOCKED_STATE || state == LOCKED_STATE) && !animator.isRunning()) {
                currentState = state;
                invalidate();
            } else if (state == LOCKING_STATE && !animator.isStarted()){
                currentState = state;
                animator.start();
            }else {
                currentState = state;
                invalidate();
            }
        }
    }

    public void cancelAnimation(){
        animator.cancel();
        if(currentState == LOCKING_STATE){
            currentState = UNLOCKED_STATE;
        }else if(currentState == UNLOCKING_STATE){
            currentState = LOCKED_STATE;
        }
        invalidate();
    }

    public int getCurrentState() {
        return currentState;
    }
}

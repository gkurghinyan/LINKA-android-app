package com.linka.lockapp.aos.module.pages;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.pages.mylinkas.Circle;
import com.linka.lockapp.aos.module.widget.DimensionUtils;
import com.linka.lockapp.aos.module.widget.TouchUtils;

import java.util.Calendar;

/**
 * Created by leandroferreira on 07/03/17.
 */

public class SwipeButton extends RelativeLayout {

    public interface OnStateChangeListener {
        void onStateChange(boolean active);
    }

    public interface OnActiveListener {
        void onActive();
    }

    private float initialX;
    private boolean active;
    private TextView centerText;
    private TextView massageText;
    public ViewGroup background;

    private Drawable disabledDrawable;
    private Drawable enabledDrawable;

    private OnStateChangeListener onStateChangeListener;
    private OnActiveListener onActiveListener;

    private static final int ENABLED = 0;
    private static final int DISABLED = 1;

    private int collapsedWidth;
    private int collapsedHeight;

    CircleAngleAnimation animation;
    private Rect rect;

    private LinearLayout layer;
    private boolean hasActivationState = false;

    private static final int MIN_CLICK_DURATION = 2000;
    private long startClickTime;
    private boolean longClickActive = false;

    public float clickPosition; //The position on the circle that is clicked
    public float clickPositionChanged;

    public Circle circleView;
    //    public Circle circleViewBackground;
//    public ImageView circleViewInner;
    private ImageView bottomImageView;

    private boolean isCircleEnable = true;
    private boolean isCircleClickable = false;

    Context context;
    OnSwipeCompleteListener swipeListener;

    public void setSwipeCompleteListener(OnSwipeCompleteListener swipeListener) {
        this.swipeListener = swipeListener;
    }

    public SwipeButton(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    public boolean isActive() {
        return active;
    }

    public void setText(String text) {
        centerText.setText(text);
    }

    public void setBackground(Drawable drawable) {
        background.setBackground(drawable);
    }

    public void setSlidingButtonBackground(Drawable drawable) {
        background.setBackground(drawable);
    }

    public void setDisabledDrawable(Drawable drawable) {
        disabledDrawable = drawable;

        if (!active) {
            //circleView.setImageDrawable(drawable);
        }
    }

    public void setButtonBackground(Drawable buttonBackground) {
        if (buttonBackground != null) {
            circleView.setBackground(buttonBackground);
        }
    }

    public void setEnabledDrawable(Drawable drawable) {
        enabledDrawable = drawable;

        if (active) {
            //circleView.setImageDrawable(drawable);
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void setOnActiveListener(OnActiveListener onActiveListener) {
        this.onActiveListener = onActiveListener;
    }

    public void setInnerTextPadding(int left, int top, int right, int bottom) {
        centerText.setPadding(left, top, right, bottom);
    }

    public void setSwipeButtonPadding(int left, int top, int right, int bottom) {
        circleView.setPadding(left, top, right, bottom);
    }

    public void setHasActivationState(boolean hasActivationState) {
        this.hasActivationState = hasActivationState;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        hasActivationState = true;
        this.context = context;

        background = new RelativeLayout(context);


        LayoutParams layoutParamsView = new LayoutParams(
                (int) DimensionUtils.convertDpToPixel(Circle.CIRCLE_SIZE_DP, context),
                ViewGroup.LayoutParams.MATCH_PARENT);

        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(background, layoutParamsView);

        bottomImageView = new ImageView(context);
        bottomImageView.setId(R.id.bottom_image);
        bottomImageView.setColorFilter(getResources().getColor(R.color.bottom_image_color), PorterDuff.Mode.MULTIPLY);
        bottomImageView.setPadding(20,20,20,20);

        LayoutParams imageLayoutParams = new LayoutParams((int) DimensionUtils.convertDpToPixel(Circle.CIRCLE_SIZE_DP, context) - 120,
                (int) DimensionUtils.convertDpToPixel(Circle.CIRCLE_SIZE_DP, context) - 80);
        imageLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        addView(bottomImageView,imageLayoutParams);

        final TextView centerText = new TextView(context);
        this.centerText = centerText;
        centerText.setGravity(Gravity.CENTER);

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setLayoutDirection(Gravity.CENTER_HORIZONTAL);
        //layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //background.addView(centerText, layoutParams);

        circleView = new Circle(context, null);
//        circleViewBackground = new Circle(context, null);
//        circleViewInner = new ImageView(context);

//        circleViewInner.setImageResource(R.drawable.circle_center);

        circleView.setId(R.id.circle);
//        circleViewBackground.setId(R.id.circle_background);

        animation = new CircleAngleAnimation(circleView, 358); //Full circle = 360 degrees
        animation.setDuration(2000);


        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeButton,
                    defStyleAttr, defStyleRes);

            collapsedWidth = (int) typedArray.getDimension(R.styleable.SwipeButton_button_image_width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            collapsedHeight = (int) typedArray.getDimension(R.styleable.SwipeButton_button_image_height,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.SwipeButton_inner_text_background);

           /*if (backgroundDrawable != null) {
                background.setBackground(backgroundDrawable);
            } else {*/
            LogHelper.e("Setting Background ...", "background");
            background.setBackground(ContextCompat.getDrawable(context, R.color.linka_transparent));
            //}


            centerText.setText(typedArray.getText(R.styleable.SwipeButton_inner_text));
            centerText.setTextColor(typedArray.getColor(R.styleable.SwipeButton_inner_text_color,
                    Color.WHITE));

            float textSize = DimensionUtils.converPixelsToSp(
                    typedArray.getDimension(R.styleable.SwipeButton_inner_text_size, 0), context);

            if (textSize != 0) {
                centerText.setTextSize(textSize);
            } else {
                centerText.setTextSize(12);
            }

            disabledDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_image_disabled);
            enabledDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_image_enabled);
            float innerTextLeftPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_left_padding, 0);
            float innerTextTopPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_top_padding, 0);
            float innerTextRightPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_right_padding, 0);
            float innerTextBottomPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_bottom_padding, 0);

            int initialState = typedArray.getInt(R.styleable.SwipeButton_initial_state, DISABLED);

            LayoutParams layoutParamsButton = new LayoutParams(collapsedWidth, collapsedHeight);

            layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParamsButton.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

            //circleView.setImageDrawable(disabledDrawable);

//            addView(circleViewInner, layoutParamsButton);
//            addView(circleViewBackground, layoutParamsButton);
            addView(circleView, layoutParamsButton);

            active = false;
            massageText = new TextView(context);
            massageText.setText(context.getString(R.string.asleep_or_out));
            massageText.setTextAppearance(context, R.style.fontForTextViewInSwipeButton);
            massageText.setGravity(Gravity.CENTER);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.circle);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            addView(massageText, layoutParams);

            centerText.setPadding((int) innerTextLeftPadding,
                    (int) innerTextTopPadding,
                    (int) innerTextRightPadding,
                    (int) innerTextBottomPadding);

            Drawable buttonBackground = typedArray.getDrawable(R.styleable.SwipeButton_button_background);

            if (buttonBackground != null) {
                circleView.setBackground(buttonBackground);
            } else {
                circleView.setBackground(ContextCompat.getDrawable(context, R.color.linka_transparent));
            }

            float buttonLeftPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_left_padding, 0);
            float buttonTopPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_top_padding, 0);
            float buttonRightPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_right_padding, 0);
            float buttonBottomPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_bottom_padding, 0);

            circleView.setPadding((int) buttonLeftPadding,
                    (int) buttonTopPadding,
                    (int) buttonRightPadding,
                    (int) buttonBottomPadding);

            hasActivationState = typedArray.getBoolean(R.styleable.SwipeButton_has_activate_state, true);

            typedArray.recycle();
        }
        setOnTouchListener(getButtonTouchListener());
    }

    private void startCircleAnimation(){
        if(circleView.getCurrentState() == Circle.UNLOCKED_STATE) {
            circleView.drawState(Circle.LOCKING_STATE);
        }else if(circleView.getCurrentState() == Circle.LOCKED_STATE){
            circleView.drawState(Circle.UNLOCKING_STATE);
        }
    }

    private void cancelCircleAnimation(){
        circleView.cancelAnimation();
    }

    private OnTouchListener getButtonTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        if(isCircleClickable) {

                            boolean isOutsideButtonPressed = TouchUtils.isTouchOutsideInitialPosition(event, circleView);
                            if (!isOutsideButtonPressed) {
                                isCircleEnable = false;

                                if (!longClickActive) {
                                    longClickActive = true;
                                    if(circleView.getCurrentState() == Circle.LOCKED_STATE){
                                        setLockingUnlockingStates();
                                    }else {
                                        handler.postDelayed(setColor, MIN_CLICK_DURATION);
                                    }
                                    startClickTime = Calendar.getInstance().getTimeInMillis();
                                }


                                if (swipeListener != null) {
                                    swipeListener.clickStarted();
                                }

                                startCircleAnimation();

                                clickPosition = event.getY();

                            }

                            return !isOutsideButtonPressed;
                        }else {
                            return false;
                        }


                    case MotionEvent.ACTION_MOVE:
                        if (longClickActive) {
                            long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;


                            if (clickDuration >= MIN_CLICK_DURATION || circleView.getCurrentState() == Circle.UNLOCKING_STATE) {

                                clickPositionChanged = event.getY() - clickPosition;

                                background.setBackground(ContextCompat.getDrawable(context, R.drawable.lock_shape_button));

                                if (swipeListener != null) {
                                    swipeListener.swipeStarted();
                                }

                                //setBackground(AppMainActivity.instance.getResources().getDrawable(R.drawable.shape_rounded));
                                if (initialX == 0) {
                                    initialX = circleView.getY();
                                }

                                if (clickPositionChanged < 0) {
                                    circleView.setY(0);
//                                    circleViewInner.setY(0);
//                                    circleViewBackground.setY(0);
                                } else if (clickPositionChanged + circleView.getHeight() > getHeight()) {
                                    circleView.setY(getHeight() - circleView.getHeight());
//                                    circleViewInner.setY(getHeight() - circleView.getHeight());
//                                    circleViewBackground.setY(getHeight() - circleView.getHeight());

                                } else {


                                    circleView.setY(clickPositionChanged);
//                                    circleViewBackground.setY(clickPositionChanged);
//                                    circleViewInner.setY(clickPositionChanged);
                                }
                                return false;
                            } else {
                                boolean isOutsideButton = TouchUtils.isTouchOutsideInitialPosition(event, circleView);

                                //Make sure that they're still holding on and they're not trying to cheat by pulling it too soon
                                //If they pull it more than 10% then it means that they're cheating
                                if (isOutsideButton) {
                                    longClickActive = false;
                                    setBackground(AppMainActivity.instance.getResources().getDrawable(R.color.linka_transparent));
                                    handler.removeCallbacks(setColor);

                                    cancelCircleAnimation();


                                    if (swipeListener != null) {
                                        swipeListener.clickCancelled();
                                    }
                                    isCircleEnable = true;

                                    return false;
                                }
                                clickPosition = event.getY();
                            }
                        }
                        return false;

                    case MotionEvent.ACTION_UP:
                        longClickActive = false;
                        setBackground(AppMainActivity.instance.getResources().getDrawable(R.color.linka_transparent));
                        handler.removeCallbacks(setColor);

                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;

                        if (circleView.getY() + circleView.getHeight() > getHeight() * 0.95) {
                            if (hasActivationState) {
                                //expandButton();
                                if (swipeListener != null) {
                                    setCircleClickable(false);
                                    swipeListener.onSwipeComplete(true);
                                }

                                moveButtonBack();

                            } else if (onActiveListener != null) {
                                onActiveListener.onActive();
                                moveButtonBack();
                            }
                        } else {

                            if (swipeListener != null) {
                                swipeListener.clickCancelled();
                                swipeListener.swipeCancelled();
                            }

                            moveButtonBack();
                        }

                        return false;
                }

                return false;
            }
        };
    }

    private void startAnimation() {

    }

    Handler handler = new Handler();
    Runnable setColor = new Runnable() {
        @Override
        public void run() {
            setLockingUnlockingStates();

        }
    };

    private void setLockingUnlockingStates(){
        background.setBackground(ContextCompat.getDrawable(context, R.drawable.lock_shape_button));
        bottomImageView.setVisibility(VISIBLE);
        if(circleView.getCurrentState() == Circle.LOCKING_STATE){
            bottomImageView.setImageDrawable(getResources().getDrawable(R.drawable.close_white_linka));
        }else if(circleView.getCurrentState() == Circle.LOCKED_STATE){
            bottomImageView.setImageDrawable(getResources().getDrawable(R.drawable.open_white_linka));
        }

        if (swipeListener != null) {
            swipeListener.clickComplete();
        }
    }


    @NonNull
    private GradientDrawable createFrame(int count) {
        int length = 100;
        int[] colors = new int[length];
        for (int i = 0; i < length; i++) {
            colors[i] = i < (count) ? Color.parseColor("#CDCDCD") : Color.parseColor("#F3F2F6");
        }
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        gradientDrawable.setCornerRadius(150.0f);
        return gradientDrawable;
    }

    public void moveButtonBack() {
        cancelCircleAnimation();
        bottomImageView.setVisibility(GONE);
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(circleView.getY(), 0);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                circleView.setY(x);
//                circleViewInner.setY(x);
//                circleViewBackground.setY(x);

            }
        });
        positionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(circleView.getCurrentState() == Circle.LOCKED_STATE ||
                        circleView.getCurrentState() == Circle.UNLOCKED_STATE) {
                    isCircleEnable = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        positionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (layer != null) {
                    layer.setVisibility(View.GONE);
                }
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        positionAnimator.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        animatorSet.start();
    }


    public interface OnSwipeCompleteListener {
        void clickStarted(); //When the initially press the button

        void clickCancelled();

        void clickComplete(); //When they've held the button for 2.8 seconds

        void swipeStarted(); //When they start sliding

        void swipeCancelled();

        void onSwipeComplete(boolean swiped);
    }

    public void setCurrentState(int state){
        if(isCircleEnable && state == Circle.LOCKING_STATE || state == Circle.UNLOCKING_STATE){
            if(state == Circle.LOCKING_STATE){
                if(circleView.getCurrentState() == Circle.UNLOCKED_STATE) {
                    circleView.drawState(Circle.LOCKED_STATE);
                }
            }else if(circleView.getCurrentState() == Circle.LOCKED_STATE) {
                circleView.drawState(Circle.UNLOCKED_STATE);
            }
            return;
        }
        if(isCircleEnable) {
            circleView.drawState(state);
        }
    }

    public int getCurrentState(){
        return circleView.getCurrentState();
    }

    public void setCircleClickable(boolean clickable){
        isCircleClickable = clickable;
    }

    public void setMassageText(String text){
        if (massageText!=null){
            massageText.setText(text);
        }
    }

    public void setMassageTextVisibility(int visibility){
        if (massageText!=null) {
            massageText.setVisibility(visibility);
        }
    }

}

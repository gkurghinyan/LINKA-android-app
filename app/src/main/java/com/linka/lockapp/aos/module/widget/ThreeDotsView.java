package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.linka.lockapp.aos.R;

public class ThreeDotsView extends LinearLayout {

    private ImageView firstDot;
    private ImageView secondDot;
    private ImageView thirdDot;

    private Animation animFirstIn;
    private Animation animSecondIn;
    private Animation animThirdIn;

    private Animation animFirstOut;
    private Animation animSecondOut;
    private Animation animThirdOut;


    public ThreeDotsView(Context context) {
        super(context);
        initAnimations(context);
    }

    public ThreeDotsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAnimations(context);
    }

    public ThreeDotsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnimations(context);
    }


    private void initAnimations(Context context) {

        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.setOrientation(HORIZONTAL);

        final float scale = context.getResources().getDisplayMetrics().density;
        int sizeOfImages = 12;
        int pixels = (int) (sizeOfImages * scale + 0.5f);

        LayoutParams imageLayoutParam = new LayoutParams(pixels, pixels);
        imageLayoutParam.setMargins(5, 5, 5, 5);

        firstDot = new ImageView(context);
        firstDot.setImageResource(R.drawable.ic_fiber_manual_record);
        firstDot.setLayoutParams(imageLayoutParam);
        firstDot.setColorFilter(ContextCompat.getColor(context, R.color.linka_white), android.graphics.PorterDuff.Mode.MULTIPLY);

        secondDot = new ImageView(context);
        secondDot.setImageResource(R.drawable.ic_fiber_manual_record);
        secondDot.setLayoutParams(imageLayoutParam);
        secondDot.setColorFilter(ContextCompat.getColor(context, R.color.linka_white), android.graphics.PorterDuff.Mode.MULTIPLY);

        thirdDot = new ImageView(context);
        thirdDot.setImageResource(R.drawable.ic_fiber_manual_record);
        thirdDot.setLayoutParams(imageLayoutParam);
        thirdDot.setColorFilter(ContextCompat.getColor(context, R.color.linka_white), android.graphics.PorterDuff.Mode.MULTIPLY);


        this.addView(firstDot);
        this.addView(secondDot);
        this.addView(thirdDot);

        animFirstOut = new AlphaAnimation(0f, 1f);
        animSecondOut = new AlphaAnimation(0f, 1f);
        animThirdOut = new AlphaAnimation(0f, 1f);
        animFirstIn = new AlphaAnimation(1f, 0f);
        animSecondIn = new AlphaAnimation(1f, 0f);
        animThirdIn = new AlphaAnimation(1f, 0f);

        int duration = 400;
        animFirstOut.setDuration(duration);
        animSecondOut.setDuration(duration);
        animSecondOut.setStartOffset(duration / 3);
        animThirdOut.setDuration(duration);
        animThirdOut.setStartOffset(duration / 3 * 2);
        animFirstIn.setDuration(duration);
        animSecondIn.setDuration(duration);
        animThirdIn.setDuration(duration);

        startAnimation();

    }


    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
            case INVISIBLE:
                stopAnimation();
                break;
            case VISIBLE:
                startAnimation();
                break;
            case View.GONE:
                break;
        }
    }

    private void startAnimation() {
        setAnimationListener(animFirstOut, animFirstIn, firstDot, false);
        setAnimationListener(animSecondOut, animSecondIn, secondDot, false);
        setAnimationListener(animThirdOut, animThirdIn, thirdDot, true);

        firstDot.startAnimation(animFirstOut);
        secondDot.startAnimation(animSecondOut);
        thirdDot.startAnimation(animThirdOut);
    }

    private void stopAnimation() {
        if (firstDot.getAnimation() != null) {
            firstDot.getAnimation().setAnimationListener(null);
        }
        if (secondDot.getAnimation() != null) {
            secondDot.getAnimation().setAnimationListener(null);
        }
        if (thirdDot.getAnimation() != null) {
            thirdDot.getAnimation().setAnimationListener(null);
        }
        firstDot.clearAnimation();
        secondDot.clearAnimation();
        thirdDot.clearAnimation();

    }


    private void setAnimationListener(final Animation animation1, final Animation animation2, final ImageView imageView, final boolean isLast) {
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                animation2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageView.setVisibility(View.INVISIBLE);
                        if (isLast) {
                            firstDot.startAnimation(animFirstOut);
                            secondDot.startAnimation(animSecondOut);
                            thirdDot.startAnimation(animThirdOut);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageView.startAnimation(animation2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


}

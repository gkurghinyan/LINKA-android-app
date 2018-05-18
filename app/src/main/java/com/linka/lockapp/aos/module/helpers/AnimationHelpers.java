package com.linka.lockapp.aos.module.helpers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;

/**
 * Created by vanson on 25/7/15.
 */
public class AnimationHelpers {

    public static void RunViewFadeInTop(final View view)
    {
        view.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(AppDelegate.getInstance(), R.anim.anim_top_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);

    }

    public static void RunViewFadeOutTop(final View view)
    {
        view.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(AppDelegate.getInstance(), R.anim.anim_top_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim);
    }






    public static void RunViewFadeInBottom(final View view)
    {
        view.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(AppDelegate.getInstance(), R.anim.anim_bottom_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);

    }

    public static void RunViewFadeOutBottom(final View view)
    {
        view.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(AppDelegate.getInstance(), R.anim.anim_bottom_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim);
    }


}

package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.AnimationHelpers;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

/**
 * Created by Vanson on 8/12/15.
 */
public class SuperBackToTopRecyclerView extends SuperRecyclerView {
    boolean shouldFadeOut = false;
    boolean shouldFadeIn = true;
    boolean lockFadeIn = false;

    public SuperBackToTopRecyclerView(Context context) {
        super(context);
    }

    public SuperBackToTopRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SuperBackToTopRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void fadeOutHeader(View v)
    {
        if (shouldFadeOut) return;
        shouldFadeIn = false;
        shouldFadeOut = true;
        AnimationHelpers.RunViewFadeOutBottom(v);
    }

    public void fadeInHeader(View v)
    {
        if (shouldFadeIn) return;
        shouldFadeOut = false;
        shouldFadeIn = true;
        AnimationHelpers.RunViewFadeInBottom(v);
    }


    public void activateBackToTopButton() {
        if (lockFadeIn) return;
        View view = (View)this.getParent();
        if (view instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout)view;
            if (relativeLayout.findViewWithTag("back_to_top") == null) {
                TextView tv = new TextView(this.getContext());
                tv.setTag("back_to_top");
                tv.setText(R.string.back_to_top);
                tv.setTextColor(getResources().getColor(R.color.linka_white));
                tv.setBackgroundResource(R.drawable.grey_button_rounded);

                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deactivateBackToTopButton();
                        SuperBackToTopRecyclerView.this.getRecyclerView().smoothScrollToPosition(0);
                        lockBackToTopButton();
                    }
                });

                RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lay.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lay.setMargins(
                        (int) getResources().getDimension(R.dimen.activity_horizontal_margin_sm),
                        (int) getResources().getDimension(R.dimen.activity_horizontal_margin_sm),
                        (int) getResources().getDimension(R.dimen.activity_horizontal_margin_sm),
                        (int) getResources().getDimension(R.dimen.activity_horizontal_margin_sm));

                ((RelativeLayout) view).addView(tv, lay);
                fadeInHeader(tv);

            } else {
                View tv = relativeLayout.findViewWithTag("back_to_top");
                fadeInHeader(tv);

            }
        }
    }

    public void deactivateBackToTopButton() {
        unlockBackToTopButton();
        View view = (View)this.getParent();
        if (view instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout) view;
            View v = relativeLayout.findViewWithTag("back_to_top");
            if (v == null) return;
            fadeOutHeader(v);
        }
    }

    public void lockBackToTopButton() {
        lockFadeIn = true;
    }

    public void unlockBackToTopButton() {
        lockFadeIn = false;
    }
}

package com.linka.lockapp.aos.module.pages.walkthrough;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MountingItemFragment extends Fragment {
    private static final String DRAWABLE_ID = "DrawableId";
    private static final String IS_BIG_PADDING = "IsBigPadding";
    private boolean fragmentOnCreated = false;

    @BindView(R.id.mounting_image)
    ImageView mountingImage;

    @BindView(R.id.dot1_text)
    TextView dot1Text;

    private Unbinder unbinder;

    public static MountingItemFragment newInstance(int drawableId, boolean isBigPadding) {
        Bundle args = new Bundle();
        args.putInt(DRAWABLE_ID, drawableId);
        args.putBoolean(IS_BIG_PADDING, isBigPadding);
        MountingItemFragment fragment = new MountingItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mounting_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        init();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            fragmentOnCreated = true;
            startAnimation();
        } else if (isVisibleToUser) {
            fragmentOnCreated = true;
        } else if (fragmentOnCreated) {
            stopAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init() {
        mountingImage.setImageResource(getArguments().getInt(DRAWABLE_ID));
        if (getArguments().getInt(DRAWABLE_ID) == R.drawable.first_mounting_animation) {
            ((AnimationDrawable) mountingImage.getDrawable()).start();
            dot1Text.setVisibility(View.VISIBLE);
        } else {
            dot1Text.setVisibility(View.GONE);
        }
    }

    public void startAnimation() {
        if (mountingImage != null) {
            mountingImage.setImageResource(getArguments().getInt(DRAWABLE_ID));
            ((AnimationDrawable) mountingImage.getDrawable()).start();
        }
    }

    public void stopAnimation() {
        if (mountingImage != null) {
            if (mountingImage.getDrawable() != null) {
                ((AnimationDrawable) mountingImage.getDrawable()).stop();
            }
            mountingImage.setImageResource(getArguments().getInt(DRAWABLE_ID));
        }
    }

}

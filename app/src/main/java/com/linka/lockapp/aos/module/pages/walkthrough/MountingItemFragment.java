package com.linka.lockapp.aos.module.pages.walkthrough;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.linka.lockapp.aos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MountingItemFragment extends Fragment {
    private static final String DRAWABLE_ID = "DrawableId";
    private static final String IS_BIG_PADDING = "IsBigPadding";

    @BindView(R.id.mounting_image)
    ImageView mountingImage;

    private Unbinder unbinder;

    public static MountingItemFragment newInstance(int drawableId,boolean isBigPadding) {
        Bundle args = new Bundle();
        args.putInt(DRAWABLE_ID,drawableId);
        args.putBoolean(IS_BIG_PADDING,isBigPadding);
        MountingItemFragment fragment = new MountingItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mounting_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init(){
        if(getArguments().getBoolean(IS_BIG_PADDING)){
            int dimen = getResources().getDimensionPixelOffset(R.dimen.margin_l);
            mountingImage.setPadding(dimen,dimen,dimen,dimen);
        }
        mountingImage.setImageResource(getArguments().getInt(DRAWABLE_ID));
        ((AnimationDrawable)mountingImage.getDrawable()).start();
    }

    @OnClick(R.id.mounting_image)
    void onclickMountin(){
        init();
    }
}

package com.linka.lockapp.aos.module.pages.walkthrough;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by kyle on 3/8/18.
 */

public class WalkthroughFragment extends CoreFragment {

    private WalkthroughFragment.MyViewPagerAdapter myViewPagerAdapter;
    private int[] layouts;

    LayoutView layoutView;

    @BindView(R.id.walkthrough_dots)
    LinearLayout walkthroughDots;
    @BindView(R.id.dot1)
    ImageView dot1;
    @BindView(R.id.dot1_selected)
    ImageView dot1selected;
    @BindView(R.id.dot2)
    ImageView dot2;
    @BindView(R.id.dot2_selected)
    ImageView dot2selected;
    @BindView(R.id.dot3)
    ImageView dot3;
    @BindView(R.id.dot3_selected)
    ImageView dot3selected;
    @BindView(R.id.dot4)
    ImageView dot4;
    @BindView(R.id.dot4_selected)
    ImageView dot4selected;
    @BindView(R.id.dot5)
    ImageView dot5;
    @BindView(R.id.dot5_selected)
    ImageView dot5selected;
    @BindView(R.id.dot6)
    ImageView dot6;
    @BindView(R.id.dot6_selected)
    ImageView dot6selected;
    @BindView(R.id.back_button)
    ImageView backButton;
    //    @BindView(R.id.button2)
//    Button button;
    @BindView(R.id.relative)
    RelativeLayout relativeLayout;

    @BindView(R.id.walkthrough_view_pager)
    ViewPager viewPager;

    //    @BindView(R.id.skip_button)
//    TextView skipButton;
    public WalkthroughFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.walkthrough_layout, container, false);
        ButterKnife.bind(this, rootView);

//        skipButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int current = getItem(+1);
//                if (current < layouts.length) {
//                    viewPager.setCurrentItem(current);
//                } else {
//                    launchHomeScreen();
//                }
//            }
//        });

        return rootView;
    }

    ThreeDotsDialogFragment threeDotsDialogFragment;
    public void setBlur(boolean isBlur,String text) {
        if (isBlur) {
            Blurry.with(getContext()).radius(25).sampling(2).onto(relativeLayout);
            threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance().setConnectingText(true,text);
            threeDotsDialogFragment.show(getFragmentManager(), null);
        } else {
            Blurry.delete(relativeLayout);
            if(threeDotsDialogFragment != null) {
                threeDotsDialogFragment.dismiss();
                threeDotsDialogFragment = null;
            }
        }
    }

    public void removeDialog(){
        threeDotsDialogFragment.dismiss();
    }

    public void setBackButtonVisibility(int visibility) {
        backButton.setVisibility(visibility);
    }

    public void setLayouts(int[] layouts) {

        this.layouts = layouts;

        myViewPagerAdapter = new WalkthroughFragment.MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setDots(0);

    }

    public void updateLayouts(int layout, int position) {
        this.layouts[position] = layout;
        myViewPagerAdapter.notifyDataSetChanged();
    }

//    @OnClick(R.id.button2)
//    void onButtonClicked(){
//        if(viewPager.getCurrentItem() == 1) {
//            layoutView.onViewChanged(3);
//        }
//    }

    @OnClick(R.id.back_button)
    void onBackClicked() {
        if (getActivity() instanceof WalkthroughActivity) {
            ((WalkthroughActivity) getActivity()).popFragment();
        } else {
            getAppMainActivity().onBackPressed();
        }
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        /*
        WalkthroughActivity walkthroughActivity = (WalkthroughActivity) getActivity();
        walkthroughActivity.finish();
        */
    }

    void setDots(int position) {

        dot1.setVisibility(View.GONE);
        dot2.setVisibility(View.GONE);
        dot3.setVisibility(View.GONE);
        dot4.setVisibility(View.GONE);
        dot5.setVisibility(View.GONE);
        dot6.setVisibility(View.GONE);
        dot1selected.setVisibility(View.GONE);
        dot2selected.setVisibility(View.GONE);
        dot3selected.setVisibility(View.GONE);
        dot4selected.setVisibility(View.GONE);
        dot5selected.setVisibility(View.GONE);
        dot6selected.setVisibility(View.GONE);

        switch (layouts.length) {
            case 6:
                dot6.setVisibility(View.VISIBLE);
            case 5:
                dot5.setVisibility(View.VISIBLE);
            case 4:
                dot4.setVisibility(View.VISIBLE);
            case 3:
                dot3.setVisibility(View.VISIBLE);
            case 2:
                dot2.setVisibility(View.VISIBLE);
            case 1:
                dot1.setVisibility(View.VISIBLE);
        }

        switch (position + 1) {
            case 6:
                dot6selected.setVisibility(View.VISIBLE);
                dot6.setVisibility(View.GONE);
            case 5:
                dot5selected.setVisibility(View.VISIBLE);
                dot5.setVisibility(View.GONE);
                break;
            case 4:
                dot4selected.setVisibility(View.VISIBLE);
                dot4.setVisibility(View.GONE);
                break;
            case 3:
                dot3selected.setVisibility(View.VISIBLE);
                dot3.setVisibility(View.GONE);
                break;
            case 2:
                dot2selected.setVisibility(View.VISIBLE);
                dot2.setVisibility(View.GONE);
                break;
            case 1:
                dot1selected.setVisibility(View.VISIBLE);
                dot1.setVisibility(View.GONE);
                break;
        }
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (position == layouts.length - 1) {
                //btnNext.setText(R.string.done);
            } else {
                //btnNext.setText(R.string.next);

            }
            setDots(position);

            if (layoutView != null) {
                layoutView.onViewChanged(position);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            //This will be called once per slide in the view
            if (layoutView != null) {
                layoutView.onViewCreated(view, position);
            }

            return view;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    //Need a callback for when the view is created. This is cause view pager adapter loads after the views, so if we instantiate it before view is created, it will be null
    public void setLayoutView(LayoutView layoutView) {
        this.layoutView = layoutView;
    }

    public interface LayoutView {
        void onViewCreated(View view, int position);

        void onViewChanged(int position);
    }
}

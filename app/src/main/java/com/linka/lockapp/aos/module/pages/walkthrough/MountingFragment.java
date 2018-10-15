package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MountingFragment extends Fragment {

    @BindView(R.id.mounting_pager)
    ViewPager mountingPager;

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

    @BindView(R.id.mounting_text)
    TextView mountingText;

    private Unbinder unbinder;

    public static MountingFragment newInstance() {
        Bundle args = new Bundle();
        MountingFragment fragment = new MountingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mounting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init() {
        MountingPagerAdapter mountingPagerAdapter = new MountingPagerAdapter(getChildFragmentManager());
        mountingPager.setAdapter(mountingPagerAdapter);
        ArrayList<Integer> drawables = new ArrayList<>();
        drawables.add(R.drawable.first_mounting_animation);
        drawables.add(R.drawable.second_mounting_animation);
        drawables.add(R.drawable.third_mounting_animation);
        drawables.add(R.drawable.fourth_mounting_animation);
        mountingPagerAdapter.updateList(drawables);
        updateDots(0);
        mountingPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateDots(int position) {
        dot1selected.setVisibility(View.GONE);
        dot2selected.setVisibility(View.GONE);
        dot3selected.setVisibility(View.GONE);
        dot4selected.setVisibility(View.GONE);
        dot1.setVisibility(View.VISIBLE);
        dot2.setVisibility(View.VISIBLE);
        dot3.setVisibility(View.VISIBLE);
        dot4.setVisibility(View.VISIBLE);
        switch (position + 1) {
            case 1:
                dot1.setVisibility(View.GONE);
                dot1selected.setVisibility(View.VISIBLE);
                mountingText.setText(R.string.mounting_text_dot1);
                break;
            case 2:
                dot2.setVisibility(View.GONE);
                dot2selected.setVisibility(View.VISIBLE);
                mountingText.setText(R.string.mounting_text_dot2);
                break;
            case 3:
                dot3.setVisibility(View.GONE);
                dot3selected.setVisibility(View.VISIBLE);
                mountingText.setText(R.string.mounting_text_dot3);
                break;
            case 4:
                dot4.setVisibility(View.GONE);
                dot4selected.setVisibility(View.VISIBLE);
                mountingText.setText(R.string.mounting_text_dot4);
                break;
        }
    }

    private class MountingPagerAdapter extends FragmentPagerAdapter {
        List<Integer> list;

        MountingPagerAdapter(FragmentManager fm) {
            super(fm);
            list = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return MountingItemFragment.newInstance(list.get(position), (position == 2 || position == 3));
        }

        @Override
        public int getCount() {
            return list.size();
        }

        void updateList(ArrayList<Integer> items) {
            list.clear();
            list.addAll(items);
            notifyDataSetChanged();
        }
    }
}

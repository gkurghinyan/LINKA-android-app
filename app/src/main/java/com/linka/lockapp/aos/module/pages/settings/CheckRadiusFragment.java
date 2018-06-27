package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.RadarView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class CheckRadiusFragment extends Fragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.radar)
    RadarView radarView;

    @BindView(R.id.meters)
    TextView meters;

    private Linka linka;
    private int currentMeter;
    private Unbinder unbinder;

    public static CheckRadiusFragment newInstance(Linka linka) {
        Bundle args = new Bundle();
        args.putSerializable(LINKA_ARGUMENT, linka);
        CheckRadiusFragment fragment = new CheckRadiusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_radius, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        linka = ((Linka) getArguments().getSerializable(LINKA_ARGUMENT));
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init() {
        currentMeter = linka.getAuto_unlock_radius();
        switch (currentMeter){
            case 50:
                radarView.setCurrentRadius(RadarView.FIFTH_CIRCLE);
                break;
            case 100:
                radarView.setCurrentRadius(RadarView.FOURTH_CIRCLE);
                break;
            case 200:
                radarView.setCurrentRadius(RadarView.THIRD_CIRCLE);
                break;
            case 300:
                radarView.setCurrentRadius(RadarView.SECOND_CIRCLE);
                break;
            case 400:
                radarView.setCurrentRadius(RadarView.FIRST_CIRCLE);
                break;
        }
        meters.setText(String.valueOf(currentMeter) + " " + "meters");

        radarView.setOnRadarRadiusChangeListener(new RadarView.OnRadarRadiusChangeListener() {
            @Override
            public void radiusChanged(int radius) {
                switch (radius) {
                    case RadarView.FIRST_CIRCLE:
                        currentMeter = 400;
                        break;
                    case RadarView.SECOND_CIRCLE:
                        currentMeter = 300;
                        break;
                    case RadarView.THIRD_CIRCLE:
                        currentMeter = 200;
                        break;
                    case RadarView.FOURTH_CIRCLE:
                        currentMeter = 100;
                        break;
                    case RadarView.FIFTH_CIRCLE:
                        currentMeter = 50;
                        break;
                }
                linka.setAuto_unlock_radius(currentMeter);
                linka.save();
                meters.setText(String.valueOf(currentMeter) + " " + "meters");
            }
        });
    }
}

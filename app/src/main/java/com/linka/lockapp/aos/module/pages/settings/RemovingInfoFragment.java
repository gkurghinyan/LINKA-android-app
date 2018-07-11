package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;

import org.greenrobot.eventbus.EventBus;

public class RemovingInfoFragment extends CoreFragment {

    public static RemovingInfoFragment newInstance() {

        Bundle args = new Bundle();
        RemovingInfoFragment fragment = new RemovingInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_removing_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppMainActivity().setTitle("");
        EventBus.getDefault().post(SettingsPageFragment.FRAGMENT_ADDED);
        getAppMainActivity().setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                SettingsPageFragment.currentFragment = SettingsPageFragment.NO_FRAGMENT;
                getAppMainActivity().setTitle("SETTINGS");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().removeBackListener();
    }
}

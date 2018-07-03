package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;


public class AppSettingsFragment extends CoreFragment {

    public static AppSettingsFragment newInstance() {
        Bundle args = new Bundle();
        AppSettingsFragment fragment = new AppSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppMainActivity().setTitle(getString(R.string.account_settings));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setTitle("");
    }
}

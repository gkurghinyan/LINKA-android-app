package com.linka.lockapp.aos.module.pages.pac;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;
import com.pixplicity.easyprefs.library.Prefs;

public class PacTutorialFragment extends Fragment {

    public static PacTutorialFragment newInstance() {

        Bundle args = new Bundle();
        PacTutorialFragment fragment = new PacTutorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pac_tutorial, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() instanceof AppMainActivity){
            ((AppMainActivity) getActivity()).setTitle(getString(R.string.set_pac));
        }else {
            SharedPreferences.Editor editor = Prefs.edit();
            editor.putInt(Constants.SHOWING_FRAGMENT, Constants.SET_PAC_FRAGMENT);
            editor.apply();
        }

        view.findViewById(R.id.set_pac_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() instanceof WalkthroughActivity) {
                    ((WalkthroughActivity) getActivity()).nextTutorial(SetPac3.newInstance(LinkaNotificationSettings.get_latest_linka(), SetPac3.WALKTHROUGH));
                }else if(getActivity() instanceof AppMainActivity){
                    ((AppMainActivity) getActivity()).pushFragment(SetPac3.newInstance(LinkaNotificationSettings.get_latest_linka(), SetPac3.SETTINGS));
                }
            }
        });
    }
}

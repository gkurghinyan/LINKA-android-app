package com.linka.lockapp.aos.module.pages.setup;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;
import com.pixplicity.easyprefs.library.Prefs;

public class AutoUpdateFragment extends CoreFragment {

    public static AutoUpdateFragment newInstance() {
        Bundle args = new Bundle();
        AutoUpdateFragment fragment = new AutoUpdateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auto_update, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).getName() != null &&
                            !Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).getName().equals("") && !Prefs.getBoolean(Constants.SHOW_SETUP_NAME,false)) {
                        SharedPreferences.Editor editor = Prefs.edit();
                        if ((!Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).pacIsSet && Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).pac == 0) ||
                                Prefs.getBoolean(Constants.SHOW_SETUP_PAC,false)) {
                            editor.putInt(Constants.SHOWING_FRAGMENT, Constants.SET_PAC_FRAGMENT);
                            editor.apply();
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), WalkthroughActivity.class));
                        } else {
                            if (Prefs.getBoolean("show-walkthrough", false) || Prefs.getBoolean(Constants.SHOW_TUTORIAL_WALKTHROUGH,false)) {
                                editor.putInt(Constants.SHOWING_FRAGMENT, Constants.TUTORIAL_FRAGMENT);
                            } else {
                                editor.putInt(Constants.SHOWING_FRAGMENT, Constants.DONE_FRAGMENT);
                            }
                            editor.apply();
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), WalkthroughActivity.class));
                        }
                    }else {
                        getAppMainActivity().pushFragment(SetupLinka3.newInstance(SetupLinka3.WALKTHROUGH));
                    }
                }
            }, 3000);
    }
}

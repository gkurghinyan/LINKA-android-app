package com.linka.lockapp.aos.module.pages;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingFragment extends CoreFragment {
    @BindView(R.id.tutorial_toggle)
    Switch tutorialToggle;
    @BindView(R.id.setup_name_toggle)
    Switch nameToggle;
    @BindView(R.id.setup_pac_toggle)
    Switch pacToggle;

    private Unbinder unbinder;

    public static TestingFragment newInstance() {

        Bundle args = new Bundle();
        TestingFragment fragment = new TestingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testing, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);

        tutorialToggle.setChecked(Prefs.getBoolean(Constants.SHOW_TUTORIAL_WALKTHROUGH,false));
        nameToggle.setChecked(Prefs.getBoolean(Constants.SHOW_SETUP_NAME,false));
        pacToggle.setChecked(Prefs.getBoolean(Constants.SHOW_SETUP_PAC,false));
    }

    @OnCheckedChanged(R.id.tutorial_toggle)
    void onTutorialSelected(boolean checked){
            SharedPreferences.Editor editor = Prefs.edit();
            editor.putBoolean(Constants.SHOW_TUTORIAL_WALKTHROUGH,checked);
            editor.apply();
    }

    @OnCheckedChanged(R.id.setup_name_toggle)
    void onNameSelected(boolean checked){
        SharedPreferences.Editor editor = Prefs.edit();
        editor.putBoolean(Constants.SHOW_SETUP_NAME,checked);
        editor.apply();
    }

    @OnCheckedChanged(R.id.setup_pac_toggle)
    void onPacSelected(boolean checked){
        SharedPreferences.Editor editor = Prefs.edit();
        editor.putBoolean(Constants.SHOW_SETUP_PAC,checked);
        editor.apply();
    }
}

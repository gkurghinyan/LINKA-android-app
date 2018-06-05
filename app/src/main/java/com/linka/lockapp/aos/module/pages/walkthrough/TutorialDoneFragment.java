package com.linka.lockapp.aos.module.pages.walkthrough;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TutorialDoneFragment extends Fragment {
    @BindView(R.id.mount_lock)
    TextView mountLock;
    @BindView(R.id.take_app)
    TextView takeApp;

    public static TutorialDoneFragment newInstance() {
        
        Bundle args = new Bundle();
        
        TutorialDoneFragment fragment = new TutorialDoneFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial_done, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences.Editor editor = Prefs.edit();
        editor.putInt(Constants.SHOWING_FRAGMENT,Constants.DONE_FRAGMENT);
        editor.apply();
        view.setBackgroundResource(R.drawable.blue_gradient);
        ButterKnife.bind(this,view);
    }

    @OnClick(R.id.mount_lock)
    void onMountClicked(){
        ((WalkthroughActivity) getActivity()).nextTutorial(MountingFragment.newInstance());
    }

    @OnClick(R.id.take_app)
    void onTakeAppClicked(){
        SharedPreferences.Editor editor = Prefs.edit();
        editor.putInt(Constants.SHOWING_FRAGMENT,Constants.LAUNCHER_FRAGMENT);
        editor.apply();
        Intent intent = new Intent(getActivity(), AppMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();
        startActivity(intent);
    }

}

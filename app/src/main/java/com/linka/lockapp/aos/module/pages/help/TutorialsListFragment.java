package com.linka.lockapp.aos.module.pages.help;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.MountingFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialAccessCodeFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialAutoUnlockFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialQuickLockFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialShareFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialTamperFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class TutorialsListFragment extends CoreFragment {

    private Unbinder unbinder;

    public static TutorialsListFragment newInstance() {
        Bundle args = new Bundle();
        TutorialsListFragment fragment = new TutorialsListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorials_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);
        getAppMainActivity().setBackIconVisible(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.tamper_alerts)
    void onTamperAlertsClicked(){
        getAppMainActivity().pushFragment(TutorialTamperFragment.newInstance());
    }

    @OnClick(R.id.share_bike)
    void onShareBikeClicked(){
        getAppMainActivity().pushFragment(TutorialShareFragment.newInstance());
    }

    @OnClick(R.id.auto_unlocking)
    void onAutoUnlockingClicked(){
        getAppMainActivity().pushFragment(TutorialAutoUnlockFragment.newInstance());
    }

    @OnClick(R.id.quick_lock)
    void onQuickLockClicked(){
        getAppMainActivity().pushFragment(TutorialQuickLockFragment.newInstance());
    }

    @OnClick(R.id.access_code)
    void onAccessCodeClicked(){
        getAppMainActivity().pushFragment(TutorialAccessCodeFragment.newInstance());
    }

    @OnClick(R.id.mounting_guide)
    void onMounitingGuideClicked(){
        getAppMainActivity().pushFragment(MountingFragment.newInstance());
    }
}

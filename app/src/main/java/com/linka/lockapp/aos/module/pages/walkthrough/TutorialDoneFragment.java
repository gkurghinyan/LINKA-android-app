package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TutorialDoneFragment extends Fragment {
    @BindView(R.id.mount_lock)
    TextView mountLock;

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
        ButterKnife.bind(this,view);
    }

    @OnClick(R.id.mount_lock)
    void onMountClicked(){
        ((WalkthroughActivity) getActivity()).nextTutorial(MountingFragment.newInstance());
    }

}

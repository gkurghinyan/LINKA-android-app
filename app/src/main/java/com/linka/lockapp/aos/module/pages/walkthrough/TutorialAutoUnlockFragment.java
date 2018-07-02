package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;


public class TutorialAutoUnlockFragment extends Fragment {

    public static TutorialAutoUnlockFragment newInstance() {

        Bundle args = new Bundle();
        TutorialAutoUnlockFragment fragment = new TutorialAutoUnlockFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial_auto_unlock, container, false);
    }

}

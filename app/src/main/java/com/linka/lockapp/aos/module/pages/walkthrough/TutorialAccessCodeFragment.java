package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;

public class TutorialAccessCodeFragment extends Fragment {

    public static TutorialAccessCodeFragment newInstance() {

        Bundle args = new Bundle();
        TutorialAccessCodeFragment fragment = new TutorialAccessCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial_access_code, container, false);
    }

}

package com.linka.lockapp.aos.module.pages.pac;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.pages.walkthrough.EmptyFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;

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

        view.findViewById(R.id.set_pac_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WalkthroughActivity) getActivity()).nextTutorial(EmptyFragment.newInstance(1));
            }
        });
    }
}

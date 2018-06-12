package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyFragment extends Fragment {
    private static final String NUMBER = "Number";

    @BindView(R.id.skip)
    TextView button;
    @BindView(R.id.text)
    TextView text;

    @OnClick(R.id.skip)
    void onButtonClicked() {
        if (getArguments().getInt(NUMBER) == 0) {
            ((WalkthroughActivity) getActivity()).nextTutorial(EmptyFragment.newInstance(1));
        } else if (getArguments().getInt(NUMBER) == 1) {

            if(Prefs.getBoolean("show-walkthrough",false)){
                ((WalkthroughActivity) getActivity()).nextTutorial(TutorialsPagerFragment.newInstance(false));
            }else {
                ((WalkthroughActivity) getActivity()).nextTutorial(TutorialDoneFragment.newInstance());
            }


        } else if (getArguments().getInt(NUMBER) == 2) {
//            ((WalkthroughActivity) getActivity()).nextTutorial(EmptyFragment.newInstance(3));
            ((WalkthroughActivity) getActivity()).nextTutorial(TutorialShareFragment.newInstance(getString(R.string.two_press),
                    getString(R.string.press_power_button),
                    R.drawable.wi_fi_connection,
                    TutorialShareFragment.DONE_FRAGMENT));
        } else if (getArguments().getInt(NUMBER) == 3) {
//            ((WalkthroughActivity) getActivity()).nextTutorial(TutorialDoneFragment.newInstance());

        }
    }

    public static EmptyFragment newInstance(int number) {

        Bundle args = new Bundle();
        args.putInt(NUMBER, number);
        EmptyFragment fragment = new EmptyFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        text.setText(String.valueOf(getArguments().getInt(NUMBER)));
    }
}

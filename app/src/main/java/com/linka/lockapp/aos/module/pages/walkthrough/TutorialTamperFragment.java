package com.linka.lockapp.aos.module.pages.walkthrough;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;

import butterknife.ButterKnife;

public class TutorialTamperFragment extends Fragment {
    private static final String NUMBER = "Number";

//    @BindView(R.id.next)
//    TextView next;

    public static TutorialTamperFragment newInstance(int number) {
        Bundle args = new Bundle();
        args.putInt(NUMBER, number);
        TutorialTamperFragment fragment = new TutorialTamperFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial_tamper, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

//    @OnClick(R.id.next)
//    void onNextClicked() {
//
//        ((WalkthroughActivity) getActivity()).nextTutorial(TutorialShareFragment.newInstance(getString(R.string.share_bike),
//                getString(R.string.share_access),
//                R.drawable.wi_fi_connection,
//                TutorialShareFragment.EMPTY_FRAGMENT));
//    }
}

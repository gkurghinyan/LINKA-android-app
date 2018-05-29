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
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifImageView;

public class TutorialShareFragment extends Fragment {
    private static final String HEADER_ARGUMENT = "HeaderArgument";
    private static final String IMAGE_ARGUMENT = "ImageArgument";
    private static final String FOOTER_ARGUMENT = "FooterArgument";
    private static final String OPEN_FRAGMENT = "OpenFragment";
    public static final int EMPTY_FRAGMENT = 0;
    public static final int DONE_FRAGMENT = 1;

    @BindView(R.id.header)
    TextView header;
    @BindView(R.id.image)
    GifImageView image;
    @BindView(R.id.footer)
    TextView footer;
//    @BindView(R.id.skip)
//    TextView skip;

    private Unbinder unbinder;

    public static TutorialShareFragment newInstance(String header, String footer, int image, int openFragment) {

        Bundle args = new Bundle();
        args.putString(HEADER_ARGUMENT, header);
        args.putString(FOOTER_ARGUMENT, footer);
        args.putInt(IMAGE_ARGUMENT, image);
        args.putInt(OPEN_FRAGMENT,openFragment);
        TutorialShareFragment fragment = new TutorialShareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_share, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        initViews();
    }

    private void initViews() {
        header.setText(getArguments().getString(HEADER_ARGUMENT));
        footer.setText(getArguments().getString(FOOTER_ARGUMENT));
        image.setImageResource(getArguments().getInt(IMAGE_ARGUMENT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

//    @OnClick(R.id.skip)
//    void onSkipClicked() {
//        switch (getArguments().getInt(OPEN_FRAGMENT)) {
//            case EMPTY_FRAGMENT:
//                ((WalkthroughActivity) getActivity()).nextTutorial(EmptyFragment.newInstance(2));
//                break;
//            case DONE_FRAGMENT:
//                ((WalkthroughActivity) getActivity()).nextTutorial(TutorialDoneFragment.newInstance());
//                break;
//        }
//    }
}

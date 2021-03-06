package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class TutorialShareFragment extends Fragment {

    @BindView(R.id.gif_tutorial)
    GifImageView gifTutorial;

    private Unbinder unbinder;

    public static TutorialShareFragment newInstance() {

        Bundle args = new Bundle();
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
        unbinder = ButterKnife.bind(this,view);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void init(){
        ((GifDrawable) gifTutorial.getDrawable()).start();
    }

   /* @OnClick(R.id.gif_tutorial)
    void onGifTutorialClicked(){
        if(!((GifDrawable) gifTutorial.getDrawable()).isPlaying()) {
            ((GifDrawable) gifTutorial.getDrawable()).reset();
            ((GifDrawable) gifTutorial.getDrawable()).start();
        }
    }*/
}

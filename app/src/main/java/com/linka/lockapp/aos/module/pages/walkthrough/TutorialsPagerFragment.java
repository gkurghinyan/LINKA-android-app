package com.linka.lockapp.aos.module.pages.walkthrough;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.pixplicity.easyprefs.library.Prefs;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class TutorialsPagerFragment extends WalkthroughFragment {
    private TextView mount;

    private View.OnClickListener onGifTutorialClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!((GifDrawable) ((GifImageView) v).getDrawable()).isPlaying()){
                ((GifDrawable) ((GifImageView) v).getDrawable()).reset();
                ((GifDrawable) ((GifImageView) v).getDrawable()).start();
            }
        }
    };

    public static TutorialsPagerFragment newInstance() {
        Bundle args = new Bundle();
        TutorialsPagerFragment fragment = new TutorialsPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setBackButtonVisibility(View.INVISIBLE);
        getWalkthroughActivity().setBackButtonAvaiable(false);

        SharedPreferences.Editor editor = Prefs.edit();
        editor.putInt(Constants.SHOWING_FRAGMENT, Constants.TUTORIAL_FRAGMENT);
        editor.apply();

        int[] layouts = new int[]{
                R.layout.fragment_tutorial_tamper,
                R.layout.fragment_tutorial_share,
                R.layout.fragment_tutorial_auto_unlock,
                R.layout.fragment_tutorial_quick_lock,
                R.layout.fragment_tutorial_access_code,
                R.layout.fragment_tutorial_done
        };

        setLayouts(layouts);

        setLayoutView(new LayoutView() {
            @Override
            public void onViewCreated(View view, int position) {
                if (position == 5) {
                    mount = (TextView) view.findViewById(R.id.mount_lock);

                    mount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((WalkthroughActivity) getActivity()).nextTutorial(MountingFragment.newInstance());
                        }
                    });
                    view.findViewById(R.id.take_app).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPreferences.Editor editor = Prefs.edit();
                            editor.putInt(Constants.SHOWING_FRAGMENT, Constants.LAUNCHER_FRAGMENT);
                            editor.apply();
                            if (Prefs.getBoolean("show-walkthrough", false)) {
                                SharedPreferences.Editor editor1 = Prefs.edit();
                                editor1.putBoolean("show-walkthrough", false);
                                editor1.apply();
                            }
                            Intent intent = new Intent(getActivity(), AppMainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            getWalkthroughActivity().setBackButtonAvaiable(true);
                            getActivity().finish();
                            startActivity(intent);
                        }
                    });
                }else {
                    GifImageView gifTutorial = ((GifImageView) view.findViewById(R.id.gif_tutorial));
                    ((GifDrawable) gifTutorial.getDrawable()).setLoopCount(1);
                    gifTutorial.setOnClickListener(onGifTutorialClickListener);
                }
            }

            @Override
            public void onViewChanged(int position) {

            }
        });
    }
}

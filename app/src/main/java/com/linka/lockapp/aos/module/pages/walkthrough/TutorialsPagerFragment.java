package com.linka.lockapp.aos.module.pages.walkthrough;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.pixplicity.easyprefs.library.Prefs;


public class TutorialsPagerFragment extends WalkthroughFragment {
    private TextView mount;

    public static TutorialsPagerFragment newInstance() {

        Bundle args = new Bundle();
        TutorialsPagerFragment fragment = new TutorialsPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] layouts = new int[]{
                R.layout.fragment_tutorial_tamper,
                R.layout.fragment_tutorial_share,
                R.layout.fragment_empty,
                R.layout.fragment_tutorial_press_lock_unlock,
                R.layout.fragment_tutorial_done
        };

        setLayouts(layouts);

        setLayoutView(new LayoutView() {
            @Override
            public void onViewCreated(View view, int position) {
                if(position == 4){
                    mount = (TextView) view.findViewById(R.id.mount_lock);

                    mount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(Prefs.getBoolean("show-walkthrough",false)){
                                SharedPreferences.Editor editor = Prefs.edit();
                                editor.putBoolean("show-walkthrough",false);
                                editor.apply();
                            }
                            ((WalkthroughActivity) getActivity()).nextTutorial(MountingFragment.newInstance());
                        }
                    });
                }
            }

            @Override
            public void onViewChanged(int position) {

            }
        });
    }
}

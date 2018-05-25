package com.linka.lockapp.aos.module.pages.walkthrough;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.linka.lockapp.aos.R;

import pl.droidsonroids.gif.GifImageView;

public class MountingFragment extends WalkthroughFragment {


    public static MountingFragment newInstance() {

        Bundle args = new Bundle();
        MountingFragment fragment = new MountingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] layouts = new int[]{
                R.layout.fragment_mounting,
                R.layout.fragment_mounting,
                R.layout.fragment_mounting,
                R.layout.fragment_mounting
        };

        setLayouts(layouts);

        setLayoutView(new LayoutView() {
            @Override
            public void onViewCreated(View view,int position) {
                switch (position){
                    case 0:
                        ((GifImageView) view.findViewById(R.id.gif_image)).setImageResource(R.drawable.wi_fi_connection);
                        ((TextView) view.findViewById(R.id.reserve_text)).setText(getString(R.string.recommended));
                        ((TextView) view.findViewById(R.id.footer)).setText(getString(R.string.determine_whether));
                        break;
                    case 1:
                        ((GifImageView) view.findViewById(R.id.gif_image)).setImageResource(R.drawable.wi_fi_connection);
                        ((TextView) view.findViewById(R.id.footer)).setText(getString(R.string.you_can_mount_linka));
                        break;
                    case 2:
                        ((GifImageView) view.findViewById(R.id.gif_image)).setImageResource(R.drawable.wi_fi_connection);
                        ((TextView) view.findViewById(R.id.footer)).setText(getString(R.string.use_provided_screws));
                        break;
                    case 3:
                        ((GifImageView) view.findViewById(R.id.gif_image)).setImageResource(R.drawable.wi_fi_connection);
                        ((TextView) view.findViewById(R.id.footer)).setText(getString(R.string.if_no_holes));
                        break;
                }
            }

            @Override
            public void onViewChanged(int position) {

            }
        });
    }
}

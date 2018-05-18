package com.linka.lockapp.aos.module.pages.walkthrough;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.pac.SetPac3;
import com.rey.material.widget.Switch;

/**
 * Created by kyle on 3/8/18.
 */

public class WalkthroughTamper extends WalkthroughFragment {


    boolean isTamperOn = true;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        int[] layouts = new int[]{
                R.layout.walkthrough_tones_1,
                R.layout.walkthrough_tones_2
        };

        setLayouts(layouts);

        setLayoutView(new WalkthroughFragment.LayoutView() {
            @Override
            public void onViewCreated(View view) {
                setTonesSwitch(view);
            }
            @Override
            public void onViewChanged(int position){

            }
        });
    }


    void setTonesSwitch(View view){
        Switch tonesSwitch = (Switch) view.findViewById(R.id.walkthrough_tones_switch);
        if(tonesSwitch != null){

            tonesSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {

               @Override
               public void onCheckedChanged(Switch view, boolean checked) {
                   isTamperOn = checked;
               }

           });
        }

        Button saveTamper = (Button) view.findViewById(R.id.save_tamper);
        if(saveTamper != null){

            saveTamper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getAppMainActivity().setFragment(AppMainActivity.WalkthroughOrder.TAMPER);
                }
            });
        }
    }
}

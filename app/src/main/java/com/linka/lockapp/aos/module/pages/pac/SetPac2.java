package com.linka.lockapp.aos.module.pages.pac;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughFragment;

/**
 * Created by kyle on 3/6/18.
 */

public class SetPac2 extends WalkthroughFragment {

    public SetPac2(){

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        int[] layouts = new int[]{
                R.layout.walkthrough_pac_1,
                R.layout.walkthrough_pac_2,
                R.layout.walkthrough_pac_3
        };

        setLayouts(layouts);

        setLayoutView(new LayoutView() {
            @Override
            public void onViewCreated(View view,int position) {
                setPacButton(view);
            }
            @Override
            public void onViewChanged(int position){

            }
        });
    }


    void setPacButton(View view){
        Button setPacButton = (Button) view.findViewById(R.id.set_pac_button);
        if(setPacButton != null){

            setPacButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getAppMainActivity().pushFragment(SetPac3.newInstance(LinkaNotificationSettings.get_latest_linka(),SetPac3.SETTINGS));
                }
            });
        }
    }
}

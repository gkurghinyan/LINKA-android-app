package com.linka.lockapp.aos.module.pages.walkthrough;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreActivity;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.pages.pac.SetPac2;
import com.linka.lockapp.aos.module.pages.pac.SetPac3;

/**
 * Created by kyle on 3/6/18.
 */

public class WalkthroughActivity extends CoreActivity {

    FrameLayout frameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.walkthrough_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        frameLayout = (FrameLayout) findViewById(R.id.walkthrough_frame);

        changeStatusBarColor();

        init();
    }


    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }


    public void init(){

        Fragment fragment = new SetPac2();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.walkthrough_frame, fragment)
                .addToBackStack(null)
                .commit();

    }

    public void setPacFragment(){
        LogHelper.e("Set pac button clicked", "2");

        Fragment fragment = new SetPac3();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.walkthrough_frame, fragment)
                .addToBackStack(null)
                .commit();

    }

}

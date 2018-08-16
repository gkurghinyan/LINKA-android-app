package com.linka.lockapp.aos.module.pages.update;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.FontHelpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.pages.dfu.DfuManager;
import com.linka.lockapp.aos.module.widget.BadgeIconView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FirmwareUpdateActivity extends AppCompatActivity implements FirmwareUpdateActivityCallback {
    public static final String LINKA_EXTRA = "LinkaExtra";

    @BindView(R.id.back)
    BadgeIconView back;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.first_tab)
    View firstTab;

    @BindView(R.id.second_tab)
    View secondTab;

    @BindView(R.id.third_tab)
    View thirdTab;

    @BindView(R.id.fourth_tab)
    View fourthTab;

    private Unbinder unbinder;

    private Linka linka;
    private int currentPage = 1;

    //Trigger to track if we're in BLOD Recovery Mode
    public boolean blod_firmware_mode = false;
    public boolean blod_firmware_try_again = false; //Are they doing a firmware update after pressing "Yes" to the blod popup?

    DfuManager dfuManager;
    boolean wasDFUSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        unbinder = ButterKnife.bind(this);
        if(getIntent() != null && getIntent().getSerializableExtra(LINKA_EXTRA) != null){
            linka = ((Linka) getIntent().getSerializableExtra(LINKA_EXTRA));
        }
        init();

        // HTC Models have a BTLE customization that causes a crash withing android BTLE stack
        // if you turn off the bluetooth during a scan.  This is unfortuantely required for DFU
        // So let's catch the exception and do nothing
        if (Build.MANUFACTURER.equalsIgnoreCase("htc")) {
            LogHelper.e("DfuManagerPageFrag", "Current device is a HTC device, setting uncaught exception handler");

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    if (ex instanceof IllegalStateException && ex.getMessage()
                            .contains("BT Adapter is not turned ON")) {

                        LogHelper.e("DfuManagerPageFrag", "Got a BT Adapter exception.");
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void init() {
        FontHelpers.setFontFaceLight(this, title);
        updateCurrentPage(currentPage);

        dfuManager = new DfuManager();
    }

    private void updateCurrentPage(int page) {
        currentPage = page;
        changeTabsAlpha();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        switch (page) {
            case 1:
                fragmentTransaction.replace(R.id.firmware_update_container, FirstUpdateFragment.newInstance());
                break;
            case 2:
                fragmentTransaction.replace(R.id.firmware_update_container, SecondUpdateFragment.newInstance());
                break;
            case 3:
                fragmentTransaction.replace(R.id.firmware_update_container,ThirdUpdateFragment.newInstance());
                break;
            case 4:
                fragmentTransaction.replace(R.id.firmware_update_container,FourthUpdateFragment.newInstance(linka));
                break;
            case 5:
                this.finish();
                break;
            default:
                fragmentTransaction.commit();
                fragmentTransaction = null;
        }
        if (fragmentTransaction != null) {
            fragmentTransaction.commit();
        }
    }

    private void changeTabsAlpha(){
        switch (currentPage){
            case 1:
                secondTab.setAlpha(0.3f);
                thirdTab.setAlpha(0.3f);
                fourthTab.setAlpha(0.3f);
                break;
            case 2:
                secondTab.setAlpha(1.0f);
                thirdTab.setAlpha(0.3f);
                fourthTab.setAlpha(0.3f);
                break;
            case 3:
                secondTab.setAlpha(1.0f);
                thirdTab.setAlpha(1.0f);
                fourthTab.setAlpha(0.3f);
                break;
            case 4:
                secondTab.setAlpha(1.0f);
                thirdTab.setAlpha(1.0f);
                fourthTab.setAlpha(1.0f);
                break;
        }
    }

    @Override
    public void changeCurrentPage(int page) {
        if(currentPage == page){
            getFragmentManager().popBackStack();
        }
        updateCurrentPage(page);
    }

    @Override
    public void changeTitle(String text) {
        title.setText(text);
    }

    @Override
    public void setBackButtonVisibility(int visibility) {
        back.setVisibility(visibility);
    }

    @Override
    public DfuManager getDfuManager() {
        return dfuManager;
    }

    @Override
    public void onBackPressed() {
        if(currentPage == 1){
            new AlertDialog.Builder(this)
                    .setMessage("Do you really want to cancel updating?")
                    .setNegativeButton(R.string.no,null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirmwareUpdateActivity.this.finish();
                        }
                    }).create().show();
        }else {
            currentPage --;
            super.onBackPressed();
        }
        changeTabsAlpha();
    }

    @OnClick(R.id.back)
    void onClickBack(){
        onBackPressed();
    }
}

package com.linka.lockapp.aos.module.pages.update;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.helpers.AppBluetoothService;
import com.linka.lockapp.aos.module.model.Linka;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FourthUpdateFragment extends Fragment {
    private static final String LINKA_ARGUMENT = "LinkaArgument";

    @BindView(R.id.update_text)
    TextView updateText;

    private Unbinder unbinder;

    private FirmwareUpdateActivityCallback activityCallback;

    public static FourthUpdateFragment newInstance(Linka linka) {

        Bundle args = new Bundle();
        args.putSerializable(LINKA_ARGUMENT,linka);
        FourthUpdateFragment fragment = new FourthUpdateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fourth_update, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        activityCallback = null;
    }

    private void init() {
        if (getActivity() != null && getActivity() instanceof FirmwareUpdateActivityCallback) {
            activityCallback = ((FirmwareUpdateActivityCallback) getActivity());
        }
        activityCallback.setBackButtonVisibility(View.GONE);
        activityCallback.changeTitle("Update Successful");

        quitDfu();
    }

    @OnClick(R.id.finish_button)
    void onClickLetsGo() {
        getActivity().finish();
    }


    public void quitDfu() {
        // Reset the view controller
        AppBluetoothService.getInstance().enableFixedTimeScanning(true);

        // If success, popup specific 1.4.3 text and notify the LockController
        // to update the Lock Settings Profile next Context Packet received
        if (((FirmwareUpdateActivity) getActivity()).wasDFUSuccessful) {

            Linka linka = ((Linka) getArguments().getSerializable(LINKA_ARGUMENT));
            if(linka != null) {
                linka.updateLockSettingsProfile = true;
                linka.saveSettings();
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
//                            .setTitle(R.string.fw_1_4_3_post_update_title)
//                            .setMessage(R.string.fw_1_4_3_post_update_desc)
//                            .setNegativeButton(R.string.ok, null)
//                            .create();
//                    alertDialog.show();
                    updateText.setText(getString(R.string.update_is_done_successfully));

                }
            });
        }else {
            updateText.setText(getString(R.string.update_is_not_done));
        }
    }


}

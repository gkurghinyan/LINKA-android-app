package com.linka.lockapp.aos.module.pages.setup;

import android.app.AlertDialog;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetupLinka1 extends CoreFragment {


    @BindView(R.id.search_for_linka)
    ImageView searchForLinka;
    @BindView(R.id.user_email)
    TextView userEmail;
    @BindView(R.id.log_out)
    TextView logOut;

    private Unbinder unbinder;

    public static SetupLinka1 newInstance() {
        Bundle bundle = new Bundle();
        SetupLinka1 fragment = new SetupLinka1();
        fragment.setArguments(bundle);
        return fragment;
    }


    public SetupLinka1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setup_linka_intro_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
        }
        userEmail.setText(getString(R.string.you_re_logged_is_as) + " " + LinkaAPIServiceImpl.getUserEmail());
        List<Linka> linkas = Linka.getLinkas();
        for (int i = 0;i<linkas.size();i++){
            if(linkas.get(i).isLocked){
                logOut.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick(R.id.log_out)
    void onLogOutClicked(){
        ((AppMainActivity) getActivity()).tryLogout();
    }


    @OnClick(R.id.search_for_linka)
    void onSearchForLinka() {

        //First check that location services are enabled. If not, popup
        LocationManager lm = (LocationManager)getAppMainActivity().getSystemService(getAppMainActivity().LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if(!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(getAppMainActivity())
                    .setTitle(R.string.location_unavailable)
                    .setMessage(R.string.enable_location)
                    .setNegativeButton(R.string.ok, null)
                    .show();
        }else {
            getAppMainActivity().pushFragment(SetupLinka2.newInstance());
        }
    }
}
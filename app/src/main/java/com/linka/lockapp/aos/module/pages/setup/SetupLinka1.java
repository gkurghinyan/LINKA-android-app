package com.linka.lockapp.aos.module.pages.setup;

import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetupLinka1 extends CoreFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    public static final int REQUEST_CHECK_SETTINGS = 134;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ThreeDotsDialogFragment threeDotsDialogFragment;

    @BindView(R.id.search_for_linka)
    ImageView searchForLinka;
    @BindView(R.id.user_email)
    TextView userEmail;
    @BindView(R.id.log_out)
    TextView logOut;
    @BindView(R.id.line)
    TextView line;
    @BindView(R.id.root)
    ConstraintLayout root;
    @BindView(R.id.email_root)
    LinearLayout emailRoot;

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
        setEmail();
        List<Linka> linkas = Linka.getLinkas();
        for (int i = 0; i < linkas.size(); i++) {
            if (linkas.get(i).isLocked) {
                logOut.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void setEmail(){
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int screenWidth = metrics.widthPixels;
        userEmail.setText(getString(R.string.you_re_logged_is_as) + " " + LinkaAPIServiceImpl.getUserEmail());
        userEmail.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(userEmail.getWidth() > screenWidth - (logOut.getWidth() + line.getWidth() + emailRoot.getPaddingStart() * 5)){
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.weight = 1;
                    userEmail.setLayoutParams(params);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }


    @OnClick(R.id.log_out)
    void onLogOutClicked() {
        ((AppMainActivity) getActivity()).tryLogout();
    }


    @OnClick(R.id.search_for_linka)
    void onSearchForLinka() {

        //First check that location services are enabled. If not, popup
        LocationManager lm = (LocationManager) getAppMainActivity().getSystemService(getAppMainActivity().LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            Blurry.with(getActivity()).radius(25).sampling(2).onto(root);
            threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance();
            threeDotsDialogFragment.show(getFragmentManager(), null);
            initGoogleApi();
        } else {
            getAppMainActivity().pushFragment(SetupLinka2.newInstance());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient,
                        builder.build()
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {
                    if (threeDotsDialogFragment != null) {
                        Blurry.delete(root);
                        threeDotsDialogFragment.dismiss();
                        threeDotsDialogFragment = null;
                    }
                    //failed to show dialog
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.d("test", "loc");
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    @Subscribe
    public void onEvent(Object object) {
        if (object instanceof String && object.equals("GPSConnected")) {
            if (threeDotsDialogFragment != null) {
                Blurry.delete(root);
                threeDotsDialogFragment.dismiss();
                threeDotsDialogFragment = null;
            }
            onSearchForLinka();
        }else if(object instanceof String && object.equals("GPSNotConnected")){
            if (threeDotsDialogFragment != null) {
                Blurry.delete(root);
                threeDotsDialogFragment.dismiss();
                threeDotsDialogFragment = null;
            }
        }
    }
}
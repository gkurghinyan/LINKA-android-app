package com.linka.lockapp.aos.module.pages.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.LocationAddress;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaAddress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 30/3/16.
 */
public class LocationPageFragment extends CoreFragment implements OnMapReadyCallback {

    Linka linka;
    @BindView(R.id.btn_mapview)
    Button btnMapview;
    boolean isActive;
    Unbinder unbinder;

    public static LocationPageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        LocationPageFragment fragment = new LocationPageFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public LocationPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_page, container, false);
        ButterKnife.bind(this, rootView);
        isActive = true;

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            if (bundle.get("linka") != null) {
                linka = (Linka) bundle.getSerializable("linka");
            }
            init(savedInstanceState);
            updateLinkaLocation();
        }
    }

    @Override
    public void onDestroyView() {
        isActive = false;
        SupportMapFragment f = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (f != null) {
            getChildFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    void init(Bundle savedInstanceState) {
        implementMap();
    }


    void implementMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);


        if (AppDelegate.shouldShowCustomOpenInMapsButton) {
            btnMapview.setVisibility(View.VISIBLE);
        } else {
            btnMapview.setVisibility(View.GONE);
        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        updateLinkaLocation();


        if (AppDelegate.shouldShowCustomOpenInMapsButton) {
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(false);
        } else {
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(true);
        }


        if (ActivityCompat.checkSelfPermission(getAppMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getAppMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
    }


    GoogleMap map;


    void updateLinkaLocation() {
        if (!isAdded()) return;
        if (!isActive) return;
        if (map == null) return;

        btnMapview.setVisibility(View.INVISIBLE);

        Linka _linka = Linka.getLinkaFromLockController(linka);
        if (_linka == null) return;
        linka = _linka;

        if (_linka.latitude != null && !_linka.latitude.equals("")) {
            if (_linka.longitude != null && !_linka.longitude.equals("")) {
                Double lat = Double.parseDouble(_linka.latitude);
                Double lng = Double.parseDouble(_linka.longitude);

                LatLng latlng = new LatLng(lat, lng);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));

                updateAddress(lat, lng);

                if (marker != null) {
                    marker.setTitle(address != null ? address : linka.getName());
                    marker.setPosition(latlng);
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_pin));

//                    Toast.makeText(getAppMainActivity(), "Lock Location Updated - " + lat + " " + lng, Toast.LENGTH_LONG).show();

                } else {
                    marker = map.addMarker(new MarkerOptions().position(latlng)
                            .title(address != null ? address : linka.getName()));
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_pin));

//                    Toast.makeText(getAppMainActivity(), "Lock Location Set - " + lat + " " + lng, Toast.LENGTH_LONG).show();

                }

                btnMapview.setVisibility(View.VISIBLE);
            }
        } else {
            Location location = AppDelegate.getInstance().getCurLocation();
            if (location != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            }
        }
    }


    void updateAddress(double lat, double lng) {
        LinkaAddress linkaAddress = LinkaAddress.getAddressForLatLng(lat + "", lng + "");
        if (linkaAddress != null) {
            address = linkaAddress.address;
        } else {
            address = null;
        }

        if (address == null) {
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(lat, lng,
                    getAppMainActivity().getApplicationContext(), new GeocoderHandler());
        }
    }


    class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            address = locationAddress;
            updateLinkaLocation();
        }
    }

    String address = null;
    Marker marker = null;

    @OnClick(R.id.btn_mapview)
    void onClickBtnMapView() {

        Location location = AppDelegate.getInstance().getCurLocation();
        if (location != null
                && linka.latitude != null && !linka.latitude.equals("")
                && linka.longitude != null && !linka.longitude.equals("")) {
            double slat = location.getLatitude();
            double slng = location.getLongitude();

            double dlat = Double.parseDouble(linka.latitude);
            double dlng = Double.parseDouble(linka.longitude);

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q="+dlat+","+dlng+""));
            startActivity(intent);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(Object object) {
        if (!isAdded()) return;

        if (object != null && object.equals(LinkaActivity.LINKA_ACTIVITY_ON_CHANGE)) {
            linka = Linka.getLinkaFromLockController(linka);
            address = null;
            updateLinkaLocation();
        }
    }
}
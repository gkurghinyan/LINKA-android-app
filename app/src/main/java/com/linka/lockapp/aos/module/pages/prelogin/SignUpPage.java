package com.linka.lockapp.aos.module.pages.prelogin;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceJSON;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.eventbus.WrongCredentialsBusEventMessage;
import com.linka.lockapp.aos.module.gcm.MyFirebaseInstanceIdService;
import com.linka.lockapp.aos.module.helpers.FontHelpers;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.other.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 11/7/2016.
 */
public class SignUpPage extends CoreFragment {


    @BindView(R.id.first_name)
    EditText firstName;
    @BindView(R.id.last_name)
    EditText lastName;
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.reenter_password)
    EditText reenterPassword;
    @BindView(R.id.create_account)
    Button createAccount;
    @BindView(R.id.root)
    ConstraintLayout root;

    private Unbinder unbinder;

    public static SignUpPage newInstance() {
        Bundle bundle = new Bundle();
        SignUpPage fragment = new SignUpPage();
        fragment.setArguments(bundle);
        return fragment;
    }


    public SignUpPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_prelogin_v2_signup_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        Log.d("ActivityNameLog",getActivity().getLocalClassName());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FontHelpers.setFontFace(getContext(), firstName);
        FontHelpers.setFontFace(getContext(), lastName);
        FontHelpers.setFontFace(getContext(), username);
        FontHelpers.setFontFace(getContext(), password);
        FontHelpers.setFontFace(getContext(), reenterPassword);
        FontHelpers.setFontFace(getContext(), createAccount);
        firstName.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        lastName.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        username.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        password.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        reenterPassword.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            init();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void init() {


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

    @OnClick(R.id.create_account)
    void onCreateAccount() {
        getAppMainActivity().hideKeyboard();

        if (!Utils.isEmailValid(username.getText().toString())) {
            Toast.makeText(getActivity(), getString(R.string.invalid_email_massage), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.getText().toString().equals(reenterPassword.getText().toString())) {
            Toast.makeText(getActivity(), getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkaAPIServiceJSON.Register body = new LinkaAPIServiceJSON.Register();
        body.email = username.getText().toString();
        body.password = password.getText().toString();
        body.profile.first_name = firstName.getText().toString();
        body.profile.last_name = lastName.getText().toString();
        body.profile.name = firstName.getText().toString() + " " + lastName.getText().toString();

        body.device_token = Helpers.device_token;
        body.platform = Helpers.platform;
        body.device_name = Helpers.device_name;
        body.os_version = Helpers.os_version;


        showLoading(root);

        LinkaAPIServiceImpl.register(getAppMainActivity(),
                body,
                new Callback<LinkaAPIServiceResponse.RegisterResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse.RegisterResponse> call, Response<LinkaAPIServiceResponse.RegisterResponse> response) {
                        cancelLoading();
                        if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                            if(response.body() != null && response.body().message != null) {
                                if (response.body().message.equals("Verification Email Sent")) {
                                    new AlertDialog.Builder(getActivity())
                                            .setMessage(response.body().message)
                                            .setPositiveButton(R.string.ok, null)
                                            .setCancelable(true)
                                            .create().show();
                                } else {
                                    login();
                                }
                            }else {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("This is an invalid email")
                                        .setPositiveButton(R.string.ok, null)
                                        .setCancelable(true)
                                        .create().show();
                            }
                        }

                        //Now that we've signed in, we should send the push token immediately
                        MyFirebaseInstanceIdService.getFcmToken();
                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse.RegisterResponse> call, Throwable t) {
                        cancelLoading();
                    }
                }
        );
    }


    @Subscribe
    public void onWrongCredentialsDialog(WrongCredentialsBusEventMessage eventMessage){
        if(eventMessage.getAction() == WrongCredentialsBusEventMessage.SHOW) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage("Wrong username or password.")
                    .setNegativeButton(R.string.ok, null)
                    .show();
        }
    }


    void login() {
        getAppMainActivity().hideKeyboard();
        showLoading(root);

        LinkaAPIServiceImpl.login(getAppMainActivity(), username.getText().toString(), password.getText().toString(), new Callback<LinkaAPIServiceResponse.LoginResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.LoginResponse> call, Response<LinkaAPIServiceResponse.LoginResponse> response) {
                cancelLoading();
                if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                    getAppMainActivity().didSignIn();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.LoginResponse> call, Throwable t) {
                cancelLoading();
            }
        });
    }

}

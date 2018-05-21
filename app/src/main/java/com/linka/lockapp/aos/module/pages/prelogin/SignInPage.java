package com.linka.lockapp.aos.module.pages.prelogin;

/**
 * Created by kyle on 3/7/18.
 */

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.eventbus.WrongCredentialsBusEventMessage;
import com.linka.lockapp.aos.module.gcm.MyFirebaseInstanceIdService;
import com.linka.lockapp.aos.module.helpers.FontHelpers;
import com.linka.lockapp.aos.module.pages.dialogs.WrongCredentialsDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.blurry.Blurry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SignInPage extends CoreFragment {

    @BindView(R.id.background)
    FrameLayout frameLayout;
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.sign_in)
    Button signIn;
    @BindView(R.id.forgot_password)
    TextView forgotPassword;

    private Unbinder unbinder;

    public static SignInPage newInstance() {
        Bundle bundle = new Bundle();
        SignInPage fragment = new SignInPage();
        fragment.setArguments(bundle);
        return fragment;
    }


    public SignInPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FontHelpers.setFontFace(getContext(), username);
        FontHelpers.setFontFace(getContext(), password);
        FontHelpers.setFontFace(getContext(), forgotPassword);
        FontHelpers.setFontFace(getContext(), signIn);
        username.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        password.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
//        FontHelpers.setFontFaceLight(getContext(), signInFb);

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

    @OnClick(R.id.sign_in)
    void onSignIn() {
        Blurry.with(getActivity()).radius(25).sampling(2).onto(frameLayout);

        getAppMainActivity().hideKeyboard();
        showLoading(getString(R.string.logging_in));

        LinkaAPIServiceImpl.login(getAppMainActivity(), username.getText().toString(), password.getText().toString(), new Callback<LinkaAPIServiceResponse.LoginResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.LoginResponse> call, Response<LinkaAPIServiceResponse.LoginResponse> response) {
                hideLoading();
                if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                    getAppMainActivity().didSignIn();
                }

                Blurry.delete(frameLayout);
                //Now that we've signed in, we should send the push token immediately
                MyFirebaseInstanceIdService.getFcmToken();
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.LoginResponse> call, Throwable t) {
                Blurry.delete(frameLayout);
                hideLoading();
            }
        });
    }

    @Subscribe
    public void onWrongCredentialsDialog(WrongCredentialsBusEventMessage eventMessage){
        if(eventMessage.getAction() == WrongCredentialsBusEventMessage.SHOW) {
            Blurry.with(getActivity()).radius(25).sampling(2).onto(frameLayout);
            WrongCredentialsDialogFragment.newInstance().show(getActivity().getFragmentManager(), null);
        }else if(eventMessage.getAction() == WrongCredentialsBusEventMessage.CLOSE){
            Blurry.delete(frameLayout);
        }
    }


    @OnClick(R.id.forgot_password)
    void onForgotPassword() {
        ForgotPasswordPage1 fragment = ForgotPasswordPage1.newInstance();
        getAppMainActivity().pushFragment(fragment);
    }
}
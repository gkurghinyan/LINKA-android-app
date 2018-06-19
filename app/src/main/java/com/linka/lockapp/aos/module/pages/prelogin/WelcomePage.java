package com.linka.lockapp.aos.module.pages.prelogin;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.gcm.MyFirebaseInstanceIdService;
import com.linka.lockapp.aos.module.helpers.socialhelpers.FacebookHelper;

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
public class WelcomePage extends CoreFragment {

    @BindView(R.id.sign_in)
    TextView signIn;

    @BindView(R.id.sign_up)
    TextView signUp;

    @BindView(R.id.login_facebook)
    Button signInFb;

    @BindView(R.id.root)
    ConstraintLayout root;

    private Unbinder unbinder;

    public static WelcomePage newInstance() {
        Bundle bundle = new Bundle();
        WelcomePage fragment = new WelcomePage();
        fragment.setArguments(bundle);
        return fragment;
    }


    public WelcomePage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_prelogin_v2_signin_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        Log.d("ActivityNameLog",getActivity().getPackageName());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @OnClick(R.id.sign_up)
    void onSignUp() {
        getAppMainActivity().hideKeyboard();
        getAppMainActivity().pushFragment(SignUpPage.newInstance());
    }

    
    @OnClick(R.id.sign_in)
    void onSignIn() {
        SignInPage fragment = SignInPage.newInstance();
        getAppMainActivity().pushFragment(fragment);
    }

    @OnClick(R.id.login_facebook)
    void onSignInFb(){
        getAppMainActivity().hideKeyboard();
        FacebookHelper.authenticate(new FacebookHelper.FacebookHelperCallback() {
            @Override
            public void OnSuccess(String facebook_account, String facebook_email, String facebook_display_name, AccessToken access_token) {
                showLoading(root);

                LinkaAPIServiceImpl.login_facebook(getAppMainActivity(), access_token.getToken(), new Callback<LinkaAPIServiceResponse.LoginResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse.LoginResponse> call, Response<LinkaAPIServiceResponse.LoginResponse> response) {
                        hideLoading();
                        if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                            getAppMainActivity().didSignIn();
                        }

                        //Now that we've signed in, we should send the push token immediately
                        MyFirebaseInstanceIdService.getFcmToken();
                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse.LoginResponse> call, Throwable t) {
                        hideLoading();
                    }
                });
            }
        });
    }
}
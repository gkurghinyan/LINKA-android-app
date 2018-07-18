package com.linka.lockapp.aos.module.pages.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.pages.prelogin.ForgotPasswordPage1;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AppSettingsFragment extends CoreFragment {

    @BindView(R.id.user_first_name)
    TextView firstName;

    @BindView(R.id.user_last_name)
    TextView lastName;

    @BindView(R.id.reset_password)
    LinearLayout resetPassword;

    private Unbinder unbinder;

    public static AppSettingsFragment newInstance() {
        Bundle args = new Bundle();
        AppSettingsFragment fragment = new AppSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);
        getAppMainActivity().setTitle(getString(R.string.account_settings));
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setTitle("");
        unbinder.unbind();
    }

    private void init(){
        if (LinkaAPIServiceImpl.isLoggedIn()) {
            firstName.setText(Prefs.getString("user-first-name",""));
            lastName.setText(Prefs.getString("user-last-name",""));
        }
    }

    @OnClick(R.id.reset_password)
    void onResetPasswordClicked(){
        getAppMainActivity().pushFragment(ForgotPasswordPage1.newInstance());
    }

}

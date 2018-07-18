package com.linka.lockapp.aos.module.pages.prelogin;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.FontHelpers;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 5/10/2016.
 */
public class ForgotPasswordPage1 extends CoreFragment {

    @BindView(R.id.please_enter_email)
    TextView pleaseEnterEmail;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.ok)
    TextView ok;
    @BindView(R.id.proceed_with_code)
    TextView proceedWithCode;
    @BindView(R.id.root)
    ConstraintLayout root;

    private Unbinder unbinder;

    public static ForgotPasswordPage1 newInstance() {
        Bundle bundle = new Bundle();
        ForgotPasswordPage1 fragment = new ForgotPasswordPage1();
        fragment.setArguments(bundle);
        return fragment;
    }


    public ForgotPasswordPage1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgotpassword_1, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FontHelpers.setFontFace(getContext(), pleaseEnterEmail);
        FontHelpers.setFontFace(getContext(), email);
        FontHelpers.setFontFace(getContext(), ok);
        FontHelpers.setFontFace(getContext(), proceedWithCode);
        email.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);

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


    @OnClick(R.id.ok)
    void onOk() {
        getAppMainActivity().hideKeyboard();
        showLoading(root);

        final String _email = email.getText().toString();

        LinkaAPIServiceImpl.request_reset_password_code(
                getAppMainActivity(),
                _email,
                new Callback<LinkaAPIServiceResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                        cancelLoading();
                        if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                            getAppMainActivity().pushFragment(ForgotPasswordPage2.newInstance());
                        }
                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                        cancelLoading();
                    }
                }
        );
    }


    @OnClick(R.id.proceed_with_code)
    void onProceedWithCode()
    {
        ForgotPasswordPage2 fragment = ForgotPasswordPage2.newInstance();
        getAppMainActivity().pushFragment(fragment);
    }
}


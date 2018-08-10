package com.linka.lockapp.aos.module.pages.prelogin;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.other.Utils;

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
public class ForgotPasswordPage2 extends CoreFragment {

    @BindView(R.id.code)
    EditText code;
    @BindView(R.id.password)
    EditText newPassword;
    @BindView(R.id.confirm_password)
    EditText confirmNewPassword;
    @BindView(R.id.ok)
    TextView ok;
    @BindView(R.id.root)
    ConstraintLayout root;

    private Unbinder unbinder;

    public static ForgotPasswordPage2 newInstance() {
        Bundle bundle = new Bundle();
        ForgotPasswordPage2 fragment = new ForgotPasswordPage2();
        fragment.setArguments(bundle);
        return fragment;
    }


    public ForgotPasswordPage2() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgotpassword_2, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FontHelpers.setFontFace(getContext(), code);
        FontHelpers.setFontFace(getContext(), newPassword);
        FontHelpers.setFontFace(getContext(), confirmNewPassword);
        FontHelpers.setFontFace(getContext(), ok);
        code.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        newPassword.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
        confirmNewPassword.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);

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

        if (!newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
            new AlertDialog.Builder(getAppMainActivity())
                    .setMessage(R.string.password_not_match)
                    .setNegativeButton(R.string.ok, null)
                    .show();
            return;
        }

        showLoading(root);

        LinkaAPIServiceImpl.reset_password(
                getAppMainActivity(),
                code.getText().toString(),
                newPassword.getText().toString(),
                confirmNewPassword.getText().toString(),
                new Callback<LinkaAPIServiceResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                        cancelLoading();
                        if (LinkaAPIServiceImpl.check(response, false, getAppMainActivity())) {
                            new AlertDialog.Builder(getAppMainActivity())
                                    .setTitle(_.i(R.string.success))
                                    .setMessage(_.i(R.string.password_changed_success))
                                    .setNegativeButton(_.i(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(LinkaAPIServiceImpl.isLoggedIn()){
                                                Utils.showLoading(getActivity(),root);
                                                LinkaNotificationSettings.disconnect_all_linka();
                                                LinkaAPIServiceImpl.logout(getActivity(), new Callback<LinkaAPIServiceResponse>() {
                                                    @Override
                                                    public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                                                        Utils.cancelLoading();
                                                        getAppMainActivity().logout();
                                                    }

                                                    @Override
                                                    public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                                                        Utils.cancelLoading();
                                                    }
                                                });
                                            }else {
                                                getAppMainActivity().resetActivity();
                                            }
                                        }
                                    })
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                        cancelLoading();
                    }
                }
        );
    }
}

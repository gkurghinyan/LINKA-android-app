package com.linka.lockapp.aos.module.pages.pac;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by kyle on 3/8/18.
 */

public class SetPac1 extends CoreFragment {


    @BindView(R.id.pac_button1)
    Button changePac;
    @BindView(R.id.pac_button2)
    Button learnPac;

    Unbinder unbinder;

    public static SetPac1 newInstance() {
        Bundle bundle = new Bundle();
        SetPac1 fragment = new SetPac1();
        fragment.setArguments(bundle);
        return fragment;
    }


    public SetPac1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_pac1, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }





    @OnClick(R.id.pac_button1)
    void changePac() {
        getAppMainActivity().popFragment();
        getAppMainActivity().pushFragment(SetPac3.newInstance(LinkaNotificationSettings.get_latest_linka()));
    }

    @OnClick(R.id.pac_button2)
    void learnPac() {
        getAppMainActivity().pushFragment(SetPac2.class);
    }
}

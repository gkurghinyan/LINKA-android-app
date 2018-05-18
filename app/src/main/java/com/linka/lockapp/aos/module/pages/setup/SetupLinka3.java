package com.linka.lockapp.aos.module.pages.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetupLinka3 extends CoreFragment {


    @BindView(R.id.setup3_next)
    Button searchForLinka;

    @BindView(R.id.setup3_name)
    EditText name;

    private Unbinder unbinder;

    public static SetupLinka3 newInstance() {
        Bundle bundle = new Bundle();
        SetupLinka3 fragment = new SetupLinka3();
        fragment.setArguments(bundle);
        return fragment;
    }


    public SetupLinka3() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setup_name_linka, container, false);
        unbinder = ButterKnife.bind(this, rootView);

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

    @OnClick(R.id.setup3_next)
    void onSearchForLinka() {

        String linkaName = name.getText().toString();

        getAppMainActivity().resetActivity();
    }
}
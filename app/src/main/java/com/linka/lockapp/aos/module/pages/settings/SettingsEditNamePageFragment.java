package com.linka.lockapp.aos.module.pages.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LinkaButton;
import com.linka.lockapp.aos.module.widget.LinkaEditTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 30/3/16.
 */
public class SettingsEditNamePageFragment extends CoreFragment {


    @BindView(R.id.name)
    LinkaEditTextView name;
    @BindView(R.id.save)
    LinkaButton save;

    private Unbinder unbinder;

    public static SettingsEditNamePageFragment newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SettingsEditNamePageFragment fragment = new SettingsEditNamePageFragment();
        bundle.putSerializable("linka", linka);
        fragment.setArguments(bundle);
        return fragment;
    }


    public SettingsEditNamePageFragment() {
    }

    Linka linka;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_edit_name_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);

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
            init();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void init() {
        name.setText(linka.getName());
    }



    @OnClick(R.id.save)
    void onSave() {
        String lock_name = name.getText().toString();
        linka.saveName(lock_name);
        getAppMainActivity().hideKeyboard();
        getAppMainActivity().popFragment();
    }

}

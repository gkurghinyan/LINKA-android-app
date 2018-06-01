package com.linka.lockapp.aos.module.pages.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetupLinka3 extends CoreFragment {


    @BindView(R.id.next_button)
    Button next;
    @BindView(R.id.edit_name)
    EditText editName;

    private Unbinder unbinder;

    public static SetupLinka3 newInstance() {
        Bundle bundle = new Bundle();
        SetupLinka3 fragment = new SetupLinka3();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setup_name_linka, container, false);
        getAppMainActivity().setBackAviable(false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setBackAviable(true);
        unbinder.unbind();
    }

    @OnClick(R.id.next_button)
    void onSearchForLinka() {
        String linkaName = editName.getText().toString();
        if (!linkaName.equals("")) {
            Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).saveName(linkaName);
           startActivity(new Intent(getActivity(), WalkthroughActivity.class));
        } else {
            Toast.makeText(getActivity(), "No valid name", Toast.LENGTH_SHORT).show();
        }

    }
}
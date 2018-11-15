package com.linka.lockapp.aos.module.pages.setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaNotificationSettings;
import com.linka.lockapp.aos.module.model.User;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetupLinka3 extends CoreFragment {
    private static final String CURRENT_FRAGMENT = "CurrentFragment";
    public static final int WALKTHROUGH = 0;
    public static final int SETTINGS = 1;

    @BindView(R.id.next_button)
    Button next;
    @BindView(R.id.edit_name)
    EditText editName;

    private Unbinder unbinder;

    public static SetupLinka3 newInstance(int currentFragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(CURRENT_FRAGMENT,currentFragment);
        SetupLinka3 fragment = new SetupLinka3();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setup_name_linka, container, false);
        if(getArguments().getInt(CURRENT_FRAGMENT) == WALKTHROUGH) {
            getAppMainActivity().setBackAvailable(false);
            SharedPreferences.Editor editor = Prefs.edit();
            editor.putInt(Constants.SHOWING_FRAGMENT, Constants.SET_NAME_FRAGMENT);
            editor.apply();
        }
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String name = Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).getName();
        editName.setText(name);
        editName.getBackground().setColorFilter(getResources().getColor(R.color.linka_white), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getAppMainActivity().setBackAvailable(true);
        unbinder.unbind();
    }

    @OnClick(R.id.next_button)
    void onSearchForLinka() {
        String linkaName = editName.getText().toString();
        if (!linkaName.equals("")) {
            Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).saveName(linkaName);
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(getArguments().getInt(CURRENT_FRAGMENT) == SETTINGS){
                getAppMainActivity().popFragment();
                return;
            }
            if (!Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).pacIsSet && Linka.getLinkaById(LinkaNotificationSettings.get_latest_linka_id()).pac == 0) {
                getActivity().finish();
                startActivity(new Intent(getActivity(), WalkthroughActivity.class));
            }else {
                SharedPreferences.Editor editor = Prefs.edit();
                if (Prefs.getBoolean("show-walkthrough", false) || Prefs.getBoolean(Constants.SHOW_TUTORIAL_WALKTHROUGH,false)) {
                    editor.putInt(Constants.SHOWING_FRAGMENT, Constants.TUTORIAL_FRAGMENT);
                } else {
                    editor.putInt(Constants.SHOWING_FRAGMENT, Constants.DONE_FRAGMENT);
                }
                editor.apply();
                getActivity().finish();
                startActivity(new Intent(getActivity(), WalkthroughActivity.class));
            }
        } else {
            Toast.makeText(getActivity(), "No valid name", Toast.LENGTH_SHORT).show();
        }

    }
}
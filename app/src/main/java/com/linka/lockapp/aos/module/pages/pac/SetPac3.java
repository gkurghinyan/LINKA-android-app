package com.linka.lockapp.aos.module.pages.pac;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Constants;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;
import com.linka.lockapp.aos.module.pages.home.MainTabBarPageFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialDoneFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.TutorialsPagerFragment;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;
import com.linka.lockapp.aos.module.widget.LinkaTextView;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetPac3 extends CoreFragment {
    private static final String NEXT_FRAGMENT = "NextFragment";
    public static final int WALKTHROUGH = 1;
    public static final int SETTINGS = 2;
    private ThreeDotsDialogFragment threeDotsDialogFragment;

    @BindView(R.id.pin_value_1)
    protected ImageView pinVal1;
    @BindView(R.id.pin_value_2)
    protected ImageView pinVal2;
    @BindView(R.id.pin_value_3)
    protected ImageView pinVal3;
    @BindView(R.id.pin_value_4)
    protected ImageView pinVal4;
    @BindView(R.id.pin_value_1_filled)
    protected ImageView pinVal1_filled;
    @BindView(R.id.pin_value_2_filled)
    protected ImageView pinVal2_filled;
    @BindView(R.id.pin_value_3_filled)
    protected ImageView pinVal3_filled;
    @BindView(R.id.pin_value_4_filled)
    protected ImageView pinVal4_filled;
    @BindView(R.id.title_text)
    TextView caption;
    @BindView(R.id.view)
    View view;
    @BindView(R.id.back_button)
    ImageView back;
    @BindView(R.id.root)
    ConstraintLayout root;

    Unbinder unbinder;

    int nextPinToEdit = 1;
    String pinValue_enter = "";
    String pinValue_reenter = "";

    boolean isEnteredPinValue;
    boolean isReEnteredPinValue;

    public static SetPac3 newInstance(Linka linka, int nextFragment) {
        Bundle bundle = new Bundle();
        SetPac3 fragment = new SetPac3();
        bundle.putSerializable("linka", linka);
        bundle.putInt(NEXT_FRAGMENT, nextFragment);
        fragment.setArguments(bundle);
        return fragment;
    }

    Linka linka;

    public SetPac3() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_passcode_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() instanceof AppMainActivity){
            ((AppMainActivity) getActivity()).setTitle(getString(R.string.set_pac));
        }

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
        pinValue_enter = "";
        pinValue_reenter = "";
        isEnteredPinValue = false;
        isReEnteredPinValue = false;
        nextPinToEdit = 1;
        pinVal1.setVisibility(View.VISIBLE);
        pinVal2.setVisibility(View.VISIBLE);
        pinVal3.setVisibility(View.VISIBLE);
        pinVal4.setVisibility(View.VISIBLE);
        pinVal1_filled.setVisibility(View.GONE);
        pinVal2_filled.setVisibility(View.GONE);
        pinVal3_filled.setVisibility(View.GONE);
        pinVal4_filled.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);


        caption.setText(R.string.enter_pin_text);
    }

    void initReenter() {
        isEnteredPinValue = true;
        nextPinToEdit = 1;
        pinVal1.setVisibility(View.VISIBLE);
        pinVal2.setVisibility(View.VISIBLE);
        pinVal3.setVisibility(View.VISIBLE);
        pinVal4.setVisibility(View.VISIBLE);
        pinVal1_filled.setVisibility(View.GONE);
        pinVal2_filled.setVisibility(View.GONE);
        pinVal3_filled.setVisibility(View.GONE);
        pinVal4_filled.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);

        caption.setText(R.string.reenter_pin_text);
    }


    public void setPinBackgroundColor(LinkaTextView view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setBackgroundColor(Color.parseColor("#efefef"));
                break;
            case MotionEvent.ACTION_UP:
                view.setBackgroundColor(Color.parseColor("#ffffff"));
                break;
        }
    }

    @OnTouch(R.id.pin_key_0)
    public boolean pin0clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_1)
    public boolean pin1clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_2)
    public boolean pin2clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_3)
    public boolean pin3clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_4)
    public boolean pin4clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_5)
    public boolean pin5clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_6)
    public boolean pin6clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_7)
    public boolean pin7clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_8)
    public boolean pin8clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnTouch(R.id.pin_key_9)
    public boolean pin9clicked(LinkaTextView view, MotionEvent event) {
        setPinBackgroundColor(view, event);
        return false;
    }

    @OnClick(R.id.pin_key_0)
    void onPin0Click(){
        setLastPinValue("0");
    }

    @OnClick(R.id.pin_key_1)
    void onPin1Click() {
        setLastPinValue("1");
    }

    @OnClick(R.id.pin_key_2)
    void onPin2Click() {
        setLastPinValue("2");
    }

    @OnClick(R.id.pin_key_3)
    void onPin3Click() {
        setLastPinValue("3");
    }

    @OnClick(R.id.pin_key_4)
    void onPin4Click() {
        setLastPinValue("4");
    }

    @OnClick(R.id.pin_key_5)
    void onPin5Click() {
        setLastPinValue("5");
    }

    @OnClick(R.id.pin_key_6)
    void onPin6Click() {
        setLastPinValue("6");
    }

    @OnClick(R.id.pin_key_7)
    void onPin7Click() {
        setLastPinValue("7");
    }

    @OnClick(R.id.pin_key_8)
    void onPin8Click() {
        setLastPinValue("8");
    }

    @OnClick(R.id.pin_key_9)
    void onPin9Click() {
        setLastPinValue("9");
    }

    @OnClick(R.id.pin_key_delete)
    void onDeleteClick() {
        removeLastPinValue();
    }

    @OnClick(R.id.back_button)
    void onBackClick(){
        init();
    }

    public void setLastPinValue(String value) {
        if (nextPinToEdit == 1) {
            pinVal1.setVisibility(View.GONE);
            pinVal1_filled.setVisibility(View.VISIBLE);

        } else if (nextPinToEdit == 2) {
            pinVal2.setVisibility(View.GONE);
            pinVal2_filled.setVisibility(View.VISIBLE);
        } else if (nextPinToEdit == 3) {
            pinVal3.setVisibility(View.GONE);
            pinVal3_filled.setVisibility(View.VISIBLE);

        } else if (nextPinToEdit == 4) {
            pinVal4.setVisibility(View.GONE);
            pinVal4_filled.setVisibility(View.VISIBLE);


        }

        enterPinValue(value);
        if (refreshSaveButton()) {
            if (nextPinToEdit < 4) {
                nextPinToEdit++;
            }
        }
    }

    public void removeLastPinValue() {
        if (nextPinToEdit == 1) {
            return;

        } else if (nextPinToEdit == 2) {
            pinVal1.setVisibility(View.VISIBLE);
            pinVal1_filled.setVisibility(View.GONE);

        } else if (nextPinToEdit == 3) {
            pinVal2.setVisibility(View.VISIBLE);
            pinVal2_filled.setVisibility(View.GONE);


        } else if (nextPinToEdit == 4) {
            pinVal3.setVisibility(View.VISIBLE);
            pinVal3_filled.setVisibility(View.GONE);

        }

        deletePinValue();
        nextPinToEdit--;
        refreshSaveButton();
    }


    public void enterPinValue(String value) {
        if (!isEnteredPinValue) {
            pinValue_enter = pinValue_enter + value;
        } else if (!isReEnteredPinValue) {
            pinValue_reenter = pinValue_reenter + value;
        }
    }

    public void deletePinValue() {
        if (!isEnteredPinValue) {
            int len = (pinValue_enter.length() > 0) ? pinValue_enter.length() - 1 : 0;
            pinValue_enter = pinValue_enter.substring(0, len);
        } else if (!isReEnteredPinValue) {
            int len = (pinValue_reenter.length() > 0) ? pinValue_reenter.length() - 1 : 0;
            pinValue_reenter = pinValue_reenter.substring(0, len);
        }
    }

    public boolean refreshSaveButton() {
        if (nextPinToEdit == 4) {
//            save.setVisibility(View.VISIBLE);

            if (!isEnteredPinValue) {
                initReenter();
                return false;
            } else {
                onSave();
                return false;
            }

        }
        return true;
    }

    void onSave() {
        if (!isEnteredPinValue) {
            initReenter();
        } else if (!isReEnteredPinValue) {
            isReEnteredPinValue = true;
        }

        if (isEnteredPinValue && isReEnteredPinValue) {
            // compare
            if (pinValue_enter.equals(pinValue_reenter)) {

                // check if passcode not valid
                if (pinValue_enter.equals("1234")
                        || pinValue_enter.equals("1111")
                        || pinValue_enter.equals("2222")
                        || pinValue_enter.equals("3333")
                        || pinValue_enter.equals("4444")
                        || pinValue_enter.equals("5555")
                        || pinValue_enter.equals("6666")
                        || pinValue_enter.equals("7777")
                        || pinValue_enter.equals("8888")
                        || pinValue_enter.equals("9999")
                        ) {
                    init();
                    Toast.makeText(getActivity(), getString(R.string.passcode_format), Toast.LENGTH_SHORT).show();

                    return;
                }

                // set lock settings
                LockController lockController = LocksController.getInstance().getLockController();
                if (lockController == null) {
                    return;
                }
                if (lockController.doSetPasscode(pinValue_enter)) {

                    linka.pac = Integer.parseInt(pinValue_enter);
                    linka.pacIsSet = true;
                    linka.saveSettings();

                    Toast.makeText(getActivity(), "Success Pac", Toast.LENGTH_SHORT).show();
                    if (getArguments().getInt(NEXT_FRAGMENT) == WALKTHROUGH) {
                        if (Prefs.getBoolean("show-walkthrough", false) || Prefs.getBoolean(Constants.SHOW_TUTORIAL_WALKTHROUGH,false)) {
                            ((WalkthroughActivity) getActivity()).nextTutorial(TutorialsPagerFragment.newInstance());
                        } else {
                            ((WalkthroughActivity) getActivity()).nextTutorial(TutorialDoneFragment.newInstance());
                        }
                    } else {
                        getAppMainActivity().setFragment(MainTabBarPageFragment.newInstance(linka));
                    }

                } else {
                    Toast.makeText(getActivity(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }

            } else {
                init();
                Snackbar snackbar = Snackbar
                        .make(view, "Re-entered PAC doesn't match", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }
}

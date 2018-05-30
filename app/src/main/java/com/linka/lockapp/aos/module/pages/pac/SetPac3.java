package com.linka.lockapp.aos.module.pages.pac;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LinkaButton;
import com.linka.lockapp.aos.module.widget.LinkaTextView;
import com.linka.lockapp.aos.module.widget.LockController;
import com.linka.lockapp.aos.module.widget.LocksController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

/**
 * Created by Vanson on 17/2/16.
 */
public class SetPac3 extends CoreFragment {

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
    @BindView(R.id.save)
    LinkaButton save;
    @BindView(R.id.enter_pac_caption)
    TextView caption;

    @BindView(R.id.set_pac_success)
    RelativeLayout setPacSuccess;
    Unbinder unbinder;

    int nextPinToEdit = 1;
    String pinValue_enter = "";
    String pinValue_reenter = "";

    boolean isEnteredPinValue;
    boolean isReEnteredPinValue;

    public static SetPac3 newInstance(Linka linka) {
        Bundle bundle = new Bundle();
        SetPac3 fragment = new SetPac3();
        bundle.putSerializable("linka", linka);
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

        setPacSuccess.setVisibility(View.GONE);

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

        caption.setText(R.string.reenter_pin_text);
    }


    public void setPinBackgroundColor(LinkaTextView view, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setBackgroundColor(Color.parseColor("#efefef"));
                break;
            case MotionEvent.ACTION_UP:
                view.setBackgroundColor(Color.parseColor("#ffffff"));
                break;
        }
    }
    @OnTouch(R.id.pin_key_1)
    public boolean pin1clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_2)
    public boolean pin2clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_3)
    public boolean pin3clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_4)
    public boolean pin4clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_5)
    public boolean pin5clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_6)
    public boolean pin6clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_7)
    public boolean pin7clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_8)
    public boolean pin8clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
    }
    @OnTouch(R.id.pin_key_9)
    public boolean pin9clicked(LinkaTextView view, MotionEvent event){
        setPinBackgroundColor(view, event);
        return false;
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
        save.setVisibility(View.INVISIBLE);  // Not sure what this button is even for, disable it
        if (nextPinToEdit == 4) {
//            save.setVisibility(View.VISIBLE);

            if (!isEnteredPinValue) {
                initReenter();
                return false;
            }

            else {
                onSave();
                return false;
            }

        } else {
            save.setVisibility(View.INVISIBLE);
        }
        return true;
    }


    @OnClick(R.id.save)
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
                        )
                {
                    init();
                    new AlertDialog.Builder(getAppMainActivity())
                            .setTitle(_.i(R.string.invalid_passcode))
                            .setMessage(_.i(R.string.passcode_format))
                            .setNegativeButton(_.i(R.string.ok), null)
                            .show();

                    return;
                }

                // set lock settings
                LockController lockController = LocksController.getInstance().getLockController();
                if(lockController == null){
                    return;
                }
                if (lockController.doSetPasscode(pinValue_enter)) {

                    linka.pac= Integer.parseInt(pinValue_enter);
                    linka.pacIsSet = true;
                    linka.saveSettings();

                    setPacSuccess.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAppMainActivity().setFragment(AppMainActivity.WalkthroughOrder.PAC);
                        }
                    }, 2000);

                } else {
                    new AlertDialog.Builder(getAppMainActivity())
                            .setTitle(_.i(R.string.fail_to_communicate))
                            .setMessage(_.i(R.string.check_connection))
                            .setNegativeButton(_.i(R.string.ok), null)
                            .show();
                }

            } else {
                init();
                new AlertDialog.Builder(getAppMainActivity())
                        .setTitle(_.i(R.string.wrong_access_code))
                        .setMessage(_.i(R.string.access_code_pair_not_match))
                        .setNegativeButton(_.i(R.string.ok), null)
                        .show();
            }
        }
    }
}

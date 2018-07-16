package com.linka.lockapp.aos.module.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.i18n._;


/**
 * Created by van on 13/7/15.
 */
public class CoreActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    public int curFragmentCount = 0;

    public boolean disableBackButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curFragmentCount", curFragmentCount);
        outState.putBoolean("disableBackButton", disableBackButton);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            curFragmentCount = savedInstanceState.getInt("curFragmentCount", curFragmentCount);
            disableBackButton = savedInstanceState.getBoolean("disableBackButton", disableBackButton);
        }
    }



    @Override
    public void onBackStackChanged() {
        onChangeFragment(null);
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }


    public void onFragmentInteraction(Uri uri) {

    }


    public void showDialogFragment(DialogFragment dialogFragment)
    {
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }



    public Fragment getFragment(Class fragment)
    {
        boolean state = false;
        Fragment instance = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        try {
            instance = (Fragment) fragment.getMethod("newInstance", null).invoke(null);
            state = true;
        } catch (Exception e) {
        }
        if (state == false) try {
            instance = (Fragment) fragment.newInstance();
            state = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public boolean pushFragment(Class fragment) {
        boolean state = false;
        Fragment instance = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        Fragment newInstance = getFragment(fragment);
        if (newInstance != null)
        {
            instance = newInstance;
            state = true;
        }
        pushFragment(instance);
        return state;
    }

    public void pushFragment(Fragment instance)
    {
        if (instance == null) { Log.e("warning", "fragment instance is null!"); return; }
        curFragmentCount += 1;
        onChangeFragment(instance);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_fade_right_in, R.anim.anim_fade_left_out, R.anim.anim_fade_left_in, R.anim.anim_right_out)
                .replace(R.id.fragment_container, instance).addToBackStack(null).commit();
    }

    public void pushFragmentWithoutAnimation(Fragment instance) {
        if (instance == null) { Log.e("warning", "fragment instance is null!"); return; }
        curFragmentCount += 1;
        onChangeFragment(instance);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, instance).addToBackStack(null).commit();
    }

    public void pushFragmentShowHide(Fragment instance)
    {
        if (instance == null) { Log.e("warning", "fragment instance is null!"); return; }
        Fragment _instance = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        curFragmentCount += 1;
        onChangeFragment(instance);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_fade_right_in, R.anim.anim_fade_left_out, R.anim.anim_fade_left_in, R.anim.anim_right_out)
                .hide(_instance)
                .add(R.id.fragment_container, instance).addToBackStack(null).commit();
    }

    public boolean setFragment(Class fragment) {
        boolean state = false;
        Fragment instance = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        Fragment newInstance = getFragment(fragment);
        if (newInstance != null)
        {
            instance = newInstance;
            state = true;
        }
        setFragment(instance);
        return state;
    }

    public void setFragment(Fragment instance)
    {
        if (instance == null) { Log.e("warning", "fragment instance is null!"); return; }
//        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        curFragmentCount = 0;
        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out, R.anim.anim_fade_in, R.anim.anim_fade_out)
                .replace(R.id.fragment_container, instance).commit();
        onChangeFragment(instance);
    }

    public void popFragment() {
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        getSupportFragmentManager().popBackStack();
        curFragmentCount -= 1;
    }

    public void replaceFragment(Fragment instance) {
        if (instance == null) { Log.e("warning", "fragment instance is null!"); return; }
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out, R.anim.anim_fade_in, R.anim.anim_fade_out)
                .replace(R.id.fragment_container, instance).addToBackStack(null).commit();
        onChangeFragment(instance);
    }

    @Override
    public void onBackPressed() {
        if (curFragmentCount > 0)
        {
            popFragment();
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(_.i(R.string.exit_app))
                    .setPositiveButton(_.i(R.string.yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            CoreActivity.this.finish();
                        }

                    })
                    .setNegativeButton(_.i(R.string.no), null)
                    .show();
        }
    }

    public void onChangeFragment(Fragment fragment)
    {
        if (fragment == null)
        {
            fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }

        if (fragment != null && fragment instanceof CoreFragment)
        {
            ((CoreFragment)fragment).onChangeFragment();
        }
    }


    public Fragment getCurFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }









    public void hideKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }





    public static CoreActivity instance;
    public static AppMainActivity getInstance() {
        return (AppMainActivity) instance;
    }

    public static Activity getInstanceActivity(){
        return instance;
    }
    @Override
    protected void onResume() {
        super.onResume();
        instance = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instance == this) {
            instance = null;
        }
    }



    public void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.ok, null)
                .show();
    }


}

package com.linka.lockapp.aos.module.other;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;

import jp.wasabeef.blurry.Blurry;

public class Utils {
    private static Utils instance;
    private Context context;
    private ViewGroup viewGroup;
    private ThreeDotsDialogFragment threeDotsDialogFragment;

    private Utils(Context context){
        this.context = context;
    }

    public static Utils getInstance(Context context){
        if(instance == null){
            instance = new Utils(context);
        }
        return instance;
    }

    public void showLoading(ViewGroup viewGroup){
        this.viewGroup = viewGroup;
        Blurry.with(context).radius(25).sampling(2).onto(viewGroup);
        threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance();
        if(context instanceof AppCompatActivity) {
            threeDotsDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(),null);
        }
    }

    public void cancelLoading(){
        if(viewGroup != null){
            Blurry.delete(viewGroup);
            viewGroup = null;
        }
        if(threeDotsDialogFragment != null){
            threeDotsDialogFragment.dismiss();
            threeDotsDialogFragment = null;
        }
    }
}

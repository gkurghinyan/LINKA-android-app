package com.linka.lockapp.aos.module.other;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.linka.lockapp.aos.module.pages.dialogs.ThreeDotsDialogFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.blurry.Blurry;

public class Utils {
    private static Context context;
    private static ViewGroup viewGroup;
    private static ThreeDotsDialogFragment threeDotsDialogFragment;
    private static boolean isOpen = true;
    private static boolean inQueue = false;
    private static boolean isShowed = false;

    public static void showLoading(Context ct, ViewGroup vg) {
        if (isOpen && !isShowed) {
            isOpen = false;
            context = ct;
            viewGroup = vg;
            if(viewGroup.getWidth() == 0 && viewGroup.getHeight() == 0) {
                viewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        setBlur();
                    }
                });
            }else {
                setBlur();
            }
        }
    }

    private static void setBlur(){
        Blurry.with(context).radius(25).sampling(2).onto(viewGroup);
        threeDotsDialogFragment = ThreeDotsDialogFragment.newInstance();
        if (context instanceof AppCompatActivity) {
            threeDotsDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), null);
        } else if (context instanceof FragmentActivity) {
            threeDotsDialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), null);
        }
        isOpen = true;
        isShowed = true;
        if (inQueue) {
            cancelLoading();
        }
    }

    public static void cancelLoading() {
        if (isOpen) {
            if(isShowed) {
                if (viewGroup != null) {
                    Blurry.delete(viewGroup);
                    viewGroup = null;
                }
                if (threeDotsDialogFragment != null) {
                    threeDotsDialogFragment.dismiss();
                    threeDotsDialogFragment = null;
                }
                context = null;
                isShowed = false;
            }
        }else {
            inQueue = true;
        }
    }

    public static boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }
}

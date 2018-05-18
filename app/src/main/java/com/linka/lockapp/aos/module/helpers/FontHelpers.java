package com.linka.lockapp.aos.module.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by Vanson on 17/2/16.
 */
public class FontHelpers {
    private static final String CUSTOM_FONT_NAME_LIGHT = "Lato-Light.ttf";
    private static final String CUSTOM_FONT_NAME = "Lato-Regular.ttf";
    private static final String CUSTOM_FONT_NAME_BOLD = "Lato-Bold.ttf";
    private static Typeface TYPEFACE_LIGHT;
    private static Typeface TYPEFACE;
    private static Typeface TYPEFACE_BOLD;

    public static void setFontFaceLight(Context context, TextView tv) {
        if (TYPEFACE_LIGHT == null) {
            TYPEFACE_LIGHT = Typeface.createFromAsset(context.getAssets(), "fonts/" + CUSTOM_FONT_NAME_LIGHT);
        }
        tv.setTypeface(TYPEFACE_LIGHT);
    }

    public static void setFontFace(Context context, TextView tv) {
        if (TYPEFACE == null) {
            TYPEFACE = Typeface.createFromAsset(context.getAssets(), "fonts/" + CUSTOM_FONT_NAME);
        }
        tv.setTypeface(TYPEFACE);
    }

    public static void setFontFaceBold(Context context, TextView tv) {
        if (TYPEFACE_BOLD == null) {
            TYPEFACE_BOLD = Typeface.createFromAsset(context.getAssets(), "fonts/" + CUSTOM_FONT_NAME_BOLD);
        }
        tv.setTypeface(TYPEFACE_BOLD);
    }
}

package com.linka.lockapp.aos.module.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.AppMainActivity;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.UUID;

/**
 * Created by van on 15/7/15.
 */
public class Helpers {

    public static String device_token = "";
    public static String device_name = "";
    public static String os_version = "";
    public static String platform = "Android";


    public static void load_device_token(){
        device_token = Prefs.getString("device_token", "");

        //If it doesn't exist, then load the saved gcm_registration_id (used pre-elise)
        if(device_token.isEmpty()){
            device_token = Prefs.getString("gcm_registration_id", "");
        }

        //If it is still empty, then generate a random uuid
        if(device_token.isEmpty()){
            device_token = Settings.Secure.getString(AppDelegate.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
            LogHelper.e("Device Token", "Generated token = " + device_token);
        }

        //Save off the device token
        Prefs.putString("device_token", device_token);
    }

    public static void loadDeviceInfo(){
        device_name = getDeviceName();
        os_version = Build.VERSION.RELEASE;
    }


    //   https://stackoverflow.com/questions/7071281/get-android-device-name
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    public static String getEllipsizedString(String content)
    {
        int maxLength = content.length();
        boolean trim = false;
        if (maxLength > 120)
        {
            maxLength = 120;
            trim = true;
        }
        if (trim)
        {
            content = content.substring(0, maxLength).trim() + "...";
        }
        return content;
    }





    public static void sendEmail(Activity activity) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "info@aimpact.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
//        activity.startActivity(Intent.createChooser(emailIntent, _.i(R.string.email_us)));
    }




    public static void sendEmail(Activity activity, String email, String question) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        activity.startActivity(Intent.createChooser(emailIntent, question));
    }


    public static void sendEmailWithText(Activity activity, String email, String question, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        activity.startActivity(Intent.createChooser(emailIntent, question));
    }





    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    public static void dial(Context context, String tel) {
        Intent intent = new Intent(Intent.ACTION_DIAL);

        intent.setData(Uri.parse("tel:" + tel));
        context.startActivity(intent);
    }


    //THIS FUNCTION IS NOT USED
    public static Spanned parseHTML(String html) {
        return Html.fromHtml(html, null, new HTMLTagHandler());
    }
}

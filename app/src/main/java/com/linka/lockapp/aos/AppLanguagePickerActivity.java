package com.linka.lockapp.aos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.linka.lockapp.aos.module.i18n._;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by Vanson on 25/9/2016.
 */
public class AppLanguagePickerActivity extends Activity {

    public final static int SET_LANGUAGE = 1010;
    public final static int LANGUAGE_IS_SET = 1011;

    public static boolean shouldStartLanguageSelect()
    {
        return !isLanguageSet();
    }

    public static void createNewInstance(Activity context, boolean isForce)
    {
        Intent intent = new Intent(context, AppLanguagePickerActivity.class);
        intent.putExtra("isForce", isForce);
        context.startActivityForResult(intent, SET_LANGUAGE);
    }

    public static void forceSelectLanguageEnglish(Context context)
    {
        String language = "en";

        Locale locale;
        if (language.equals("de")) {
            locale = Locale.GERMAN;
        } else if (language.equals("fr")) {
            locale = Locale.FRANCE;
        } else if (language.equals("es")) {
            locale = new Locale("es", "ES");
        } else {
            locale = Locale.ENGLISH;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

        Prefs.edit().putBoolean("is_language_set", true).commit();
    }

    boolean isForce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_language);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        isForce = extras.getBoolean("isForce");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(_.i(R.string.select_language));

        List<String> buttons = new ArrayList<>();
        buttons.add("English");
        buttons.add("Deutsche");
        buttons.add("Français");
        buttons.add("Español");


        if (!isForce)
        {
            buttons.add(_.i(R.string.cancel));
        }

        String[] strs = buttons.toArray(new String[buttons.size()]);
        builder.setItems(strs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    setLanguageAndGotoTitlePage("en");
                }
                if (which == 1) {
                    setLanguageAndGotoTitlePage("de");
                }
                if (which == 2) {
                    setLanguageAndGotoTitlePage("fr");
                }
                if (which == 3) {
                    setLanguageAndGotoTitlePage("es");
                }
                if (which == 4) {
                    cancel();
                }
            }
        });


        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }





    public String getLanguage() {
        return Locale.getDefault().toString();
    }

    public void setLanguage(String language) {
        if (language == null) {
            language = "en";
        }

        Locale locale;
        if (language.equals("de")) {
            locale = Locale.GERMAN;
        } else if (language.equals("fr")) {
            locale = new Locale("fr", "FR");
        } else if (language.equals("es")) {
            locale = new Locale("es", "ES");
        } else {
            locale = Locale.ENGLISH;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    public static boolean isLanguageSet() {
        boolean is_language_set = Prefs.getBoolean("is_language_set", false);
        if (!is_language_set) return false;
        return true;
    }

    public void setLanguageSet() {
        Prefs.edit().putBoolean("is_language_set", true).commit();
    }






    void setLanguageAndGotoTitlePage(String lang) {
        setLanguage(lang);
        setLanguageSet();

        setResult(LANGUAGE_IS_SET);
        finish();
    }


    void cancel() {
        finish();
    }
}

package com.example.hustlefix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

public class LanguageManager {
    private static final String PREF_NAME = "HustleFixPrefs";
    private static final String KEY_LANGUAGE = "app_language";

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_AFRIKAANS = "af";
    public static final String LANGUAGE_ZULU = "zu";
    public static final String LANGUAGE_XHOSA = "xh";

    public static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }

    public static void applyLanguage(Activity activity) {
        String languageCode = getLanguage(activity);
        setLocale(activity, languageCode);
    }

    // THIS METHOD MUST RETURN Context, NOT void
    public static Context applyLanguageToContext(Context context) {
        String languageCode = getLanguage(context);
        return setLocaleOnContext(context, languageCode);
    }

    private static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // This method returns a Context with the new locale
    private static Context setLocaleOnContext(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }

        return context.createConfigurationContext(config);
    }

    public static void changeLanguage(Activity activity, String languageCode) {
        saveLanguage(activity, languageCode);
        setLocale(activity, languageCode);
        Intent intent = new Intent(activity, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_AFRIKAANS:
                return "Afrikaans";
            case LANGUAGE_ZULU:
                return "isiZulu";
            case LANGUAGE_XHOSA:
                return "isiXhosa";
            default:
                return "English";
        }
    }
}
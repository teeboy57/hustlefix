package com.example.hustlefix;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

public class LanguageManager {
    public static final String LANGUAGE_ENGLISH = "en";

    public static void saveLanguage(Context context, String languageCode) {
        // No-op as we only support English now
    }

    public static String getLanguage(Context context) {
        return LANGUAGE_ENGLISH;
    }

    public static void applyLanguage(Activity activity) {
        setLocale(activity, LANGUAGE_ENGLISH);
    }

    public static Context applyLanguageToContext(Context context) {
        return setLocaleOnContext(context, LANGUAGE_ENGLISH);
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
        // No-op
    }

    public static String getLanguageDisplayName(String languageCode) {
        return "English";
    }
}
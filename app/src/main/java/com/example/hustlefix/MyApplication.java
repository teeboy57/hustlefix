package com.example.hustlefix;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        Context context = LanguageManager.applyLanguageToContext(base);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        // Facebook SDK removed - will be added later if needed
    }
}
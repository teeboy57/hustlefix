package com.example.hustlefix;

import android.app.Application;
import android.content.Context;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        if (instance == null) {
            // This case should be rare but helps debugging
            return null;
        }
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        Context context = LanguageManager.applyLanguageToContext(base);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        // Initialize Cloudinary
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "tbst3u90");
        MediaManager.init(this, config);
    }
}
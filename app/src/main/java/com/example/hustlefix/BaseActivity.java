package com.example.hustlefix;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply language to context
        Context context = LanguageManager.applyLanguageToContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply language to activity
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
    }
}
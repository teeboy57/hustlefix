package com.example.hustlefix;

import android.content.Context;
import android.widget.Toast;

public class ErrorUtils {
    
    public static void showError(Context context, String message) {
        Toast.makeText(context, "❌ " + message, Toast.LENGTH_LONG).show();
    }
    
    public static void showError(Context context, Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "An error occurred. Please try again.";
        }
        Toast.makeText(context, "❌ " + message, Toast.LENGTH_LONG).show();
    }
    
    public static void showSuccess(Context context, String message) {
        Toast.makeText(context, "✅ " + message, Toast.LENGTH_LONG).show();
    }
    
    public static void showInfo(Context context, String message) {
        Toast.makeText(context, "ℹ️ " + message, Toast.LENGTH_LONG).show();
    }
}
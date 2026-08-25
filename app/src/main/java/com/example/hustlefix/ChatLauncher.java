package com.example.hustlefix;

import android.content.Context;
import android.content.Intent;

/**
 * Helper to launch chat screens.
 * All launches now redirect to MainActivity for Compose Navigation.
 */
public final class ChatLauncher {
    public static final String EXTRA_OTHER_USER_ID = "otherUserId";
    public static final String EXTRA_OTHER_USER_NAME = "otherUserName";

    private ChatLauncher() {}

    public static void openChatList(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("NAV_DESTINATION", "chat_list");
        context.startActivity(intent);
    }

    public static void openChat(Context context, String partnerId, String partnerName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("NAV_DESTINATION", "chat");
        intent.putExtra(EXTRA_OTHER_USER_ID, partnerId);
        intent.putExtra(EXTRA_OTHER_USER_NAME, partnerName);
        context.startActivity(intent);
    }

    public static String resolveOtherUserId(Intent intent) {
        if (intent == null) return null;
        String id = intent.getStringExtra(EXTRA_OTHER_USER_ID);
        if (id == null) id = intent.getStringExtra("partnerId");
        if (id == null) id = intent.getStringExtra("workerId");
        return id;
    }
}

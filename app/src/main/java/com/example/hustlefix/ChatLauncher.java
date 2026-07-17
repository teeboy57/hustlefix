package com.example.hustlefix;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
/**
 * Centralizes opening chats and building consistent Firebase chat room IDs.
 */
public final class ChatLauncher {
    public static final String EXTRA_OTHER_USER_ID = "otherUserId";
    public static final String EXTRA_OTHER_USER_NAME = "otherUserName";
    private ChatLauncher() {}
    public static void openChatList(Context context) {
        context.startActivity(new Intent(context, ChatListActivity.class));
    }
    public static void openChat(Context context, String otherUserId, String otherUserName) {
        if (TextUtils.isEmpty(otherUserId)) {
            openChatList(context);
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && otherUserId.equals(user.getUid())) {
            openChatList(context);
            return;
        }
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_OTHER_USER_ID, otherUserId);
        intent.putExtra(EXTRA_OTHER_USER_NAME, otherUserName != null ? otherUserName : "User");
        context.startActivity(intent);
    }
    public static String resolveOtherUserId(Intent intent) {
        if (intent == null) return null;
        String id = intent.getStringExtra(EXTRA_OTHER_USER_ID);
        if (!TextUtils.isEmpty(id)) return id;
        id = intent.getStringExtra("other_user_id");
        if (!TextUtils.isEmpty(id)) return id;
        id = intent.getStringExtra("worker_id");
        if (!TextUtils.isEmpty(id)) return id;
        id = intent.getStringExtra("client_id");
        return id;
    }
    public static String resolveOtherUserName(Intent intent) {
        if (intent == null) return null;
        String name = intent.getStringExtra(EXTRA_OTHER_USER_NAME);
        if (!TextUtils.isEmpty(name)) return name;
        name = intent.getStringExtra("other_user_name");
        if (!TextUtils.isEmpty(name)) return name;
        name = intent.getStringExtra("worker_name");
        if (!TextUtils.isEmpty(name)) return name;
        return intent.getStringExtra("client_name");
    }
    public static String buildChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        }
        return userId2 + "_" + userId1;
    }
    public static void ensureChatRoomIndexed(String chatRoomId, String currentUserId, String currentUserName, String otherUserId, String otherUserName) {
        if (TextUtils.isEmpty(chatRoomId) || TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(otherUserId)) {
            return;
        }
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);
        chatRef.child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> chatInfo = new HashMap<>();
                Map<String, Boolean> participants = new HashMap<>();
                participants.put(currentUserId, true);
                participants.put(otherUserId, true);
                chatInfo.put("participants", participants);
                Map<String, String> participantNames = new HashMap<>();
                participantNames.put(currentUserId, currentUserName != null ? currentUserName : "User");
                participantNames.put(otherUserId, otherUserName != null ? otherUserName : "User");
                chatInfo.put("participantNames", participantNames);
                if (!snapshot.exists()) {
                    chatInfo.put("createdAt", ServerValue.TIMESTAMP);
                    chatInfo.put("lastMessage", "");
                    chatInfo.put("lastMessageTime", ServerValue.TIMESTAMP);
                    chatInfo.put("lastMessageSender", "");
                    chatRef.child("info").setValue(chatInfo);
                } else if (!snapshot.child("participantNames").exists()) {
                    chatRef.child("info").child("participantNames").setValue(participantNames);
                }
                FirebaseDatabase.getInstance().getReference("userChats")
                        .child(currentUserId).child(chatRoomId).setValue(true);
                FirebaseDatabase.getInstance().getReference("userChats")
                        .child(otherUserId).child(chatRoomId).setValue(true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    /** Opens chat with the other party on a job (client Ã¢â€ â€ assigned worker). */
    public static void openChatForJob(Context context, Job job) {
        if (job == null) {
            openChatList(context);
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            openChatList(context);
            return;
        }
        String myId = user.getUid();
        if (!TextUtils.isEmpty(job.getAssignedTo()) && !job.getAssignedTo().equals(myId)) {
            openChat(context, job.getAssignedTo(), job.getAssignedToName());
            return;
        }
        if (!TextUtils.isEmpty(job.getPostedBy()) && !job.getPostedBy().equals(myId)) {
            openChat(context, job.getPostedBy(), job.getPostedByName());
            return;
        }
        Toast.makeText(context,
                "No worker assigned yet. Find workers or accept a quote first.",
                Toast.LENGTH_LONG).show();
        openChatList(context);
    }
}

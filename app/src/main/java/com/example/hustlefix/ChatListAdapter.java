package com.example.hustlefix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<ChatSummary> chats;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatSummary chat);
    }

    public ChatListAdapter(List<ChatSummary> chats, OnChatClickListener listener) {
        this.chats = chats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_conversation, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSummary chat = chats.get(position);
        holder.tvPartnerName.setText(chat.getPartnerName());
        holder.tvLastMessage.setText(chat.getLastMessage());
        
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(chat.getLastTimestamp())));

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats != null ? chats.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvPartnerName, tvLastMessage, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPartnerName = itemView.findViewById(R.id.tvPartnerName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
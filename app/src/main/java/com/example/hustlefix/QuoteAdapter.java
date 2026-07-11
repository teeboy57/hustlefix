package com.example.hustlefix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {
    private List<Quote> quoteList;
    private String userRole;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onAcceptClick(int position);
        void onDeclineClick(int position);
        void onChatClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public QuoteAdapter(List<Quote> quoteList, String userRole) {
        this.quoteList = quoteList;
        this.userRole = userRole;
    }
    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        Quote quote = quoteList.get(position);
        holder.bind(quote, userRole, listener, position);
    }
    @Override
    public int getItemCount() {
        return quoteList.size();
    }
    public void updateList(List<Quote> newList) {
        this.quoteList = newList;
        notifyDataSetChanged();
    }
    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkerName, tvJobTitle, tvAmount, tvMessage, tvTimeline, tvStatus, tvReceivedDate;
        Button btnAccept, btnDecline, btnChat;
        LinearLayout actionButtons, acceptedMessage;
        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimeline = itemView.findViewById(R.id.tvTimeline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReceivedDate = itemView.findViewById(R.id.tvReceivedDate);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnChat = itemView.findViewById(R.id.btnChat);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            acceptedMessage = itemView.findViewById(R.id.acceptedMessage);
        }
        void bind(Quote quote, String userRole, OnItemClickListener listener, int position) {
            tvWorkerName.setText(quote.getWorkerName());
            tvJobTitle.setText(quote.getJobTitle());
            tvAmount.setText(quote.getFormattedAmount());
            tvMessage.setText(quote.getMessage());
            tvTimeline.setText("Timeline: " + quote.getTimeline());
            tvReceivedDate.setText("Received: " + quote.getTimeAgo());
            if ("pending".equals(quote.getStatus())) {
                tvStatus.setText("PENDING");
                tvStatus.setTextColor(0xFFF9B43A);
                actionButtons.setVisibility(View.VISIBLE);
                acceptedMessage.setVisibility(View.GONE);
                btnAccept.setOnClickListener(v -> {
                    if (listener != null) listener.onAcceptClick(position);
                });
                btnDecline.setOnClickListener(v -> {
                    if (listener != null) listener.onDeclineClick(position);
                });
                btnChat.setOnClickListener(v -> {
                    if (listener != null) listener.onChatClick(position);
                });
            } else if ("accepted".equals(quote.getStatus())) {
                tvStatus.setText("ACCEPTED");
                tvStatus.setTextColor(0xFF2ECC71);
                actionButtons.setVisibility(View.GONE);
                acceptedMessage.setVisibility(View.VISIBLE);
                btnChat.setOnClickListener(v -> {
                    if (listener != null) listener.onChatClick(position);
                });
            } else {
                tvStatus.setText("DECLINED");
                tvStatus.setTextColor(0xFFFF4444);
                actionButtons.setVisibility(View.GONE);
                acceptedMessage.setVisibility(View.GONE);
            }
        }
    }
}
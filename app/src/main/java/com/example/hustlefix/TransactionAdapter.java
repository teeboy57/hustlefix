package com.example.hustlefix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.tvType.setText(t.getType());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(t.getTimestamp())));

        if (t.getAmount() > 0) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "+R%.2f", t.getAmount()));
            holder.tvAmount.setTextColor(0xFF4CAF50);
            holder.ivIcon.setImageResource(R.drawable.ic_save);
        } else {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "-R%.2f", Math.abs(t.getAmount())));
            holder.tvAmount.setTextColor(0xFFF44336);
            holder.ivIcon.setImageResource(R.drawable.ic_logout);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvAmount;
        ImageView ivIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvTransType);
            tvDate = itemView.findViewById(R.id.tvTransDate);
            tvAmount = itemView.findViewById(R.id.tvTransAmount);
            ivIcon = itemView.findViewById(R.id.ivTransIcon);
        }
    }
}

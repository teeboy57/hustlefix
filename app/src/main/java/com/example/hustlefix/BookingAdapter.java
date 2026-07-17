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

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        String serviceName = booking.getServiceName();
        String clientName = booking.getClientName();
        String status = booking.getStatus();
        double price = booking.getPrice();
        long timestamp = booking.getTimestamp();
        
        holder.tvServiceName.setText(serviceName);
        holder.tvClientName.setText(clientName);
        holder.tvPrice.setText(String.format("$%.2f", price));
        
        if (status == null) status = "pending";
        holder.tvStatus.setText(status);
        
        if (status.equals("completed")) {
            holder.tvStatus.setTextColor(0xFF4CAF50);
        } else if (status.equals("pending")) {
            holder.tvStatus.setTextColor(0xFFFF9800);
        } else if (status.equals("cancelled")) {
            holder.tvStatus.setTextColor(0xFFF44336);
        } else {
            holder.tvStatus.setTextColor(0xFF2196F3);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(timestamp));
        holder.tvDate.setText(dateStr);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvClientName;
        TextView tvPrice;
        TextView tvStatus;
        TextView tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
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

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

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
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBudget, tvStatus, tvDate, tvClientName;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookingTitle);
            tvBudget = itemView.findViewById(R.id.tvBookingBudget);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvDate = itemView.findViewById(R.id.tvBookingDeadline);
            tvClientName = itemView.findViewById(R.id.tvClientName);
        }

        void bind(Booking booking, OnBookingClickListener listener) {
            if (tvTitle != null) {
                tvTitle.setText(booking.getServiceTitle() != null ? booking.getServiceTitle() : "No Title");
            }
            
            if (tvBudget != null) {
                tvBudget.setText("$" + String.format("%.2f", booking.getPrice()));
            }
            
            if (tvStatus != null) {
                String status = booking.getStatus() != null ? booking.getStatus() : "pending";
                tvStatus.setText(status.toUpperCase());
                // Set status color
                switch (status.toLowerCase()) {
                    case "pending":
                        tvStatus.setTextColor(0xFFFF9800);
                        break;
                    case "confirmed":
                        tvStatus.setTextColor(0xFF2196F3);
                        break;
                    case "in_progress":
                        tvStatus.setTextColor(0xFF9C27B0);
                        break;
                    case "completed":
                        tvStatus.setTextColor(0xFF4CAF50);
                        break;
                    case "cancelled":
                        tvStatus.setTextColor(0xFFF44336);
                        break;
                    default:
                        tvStatus.setTextColor(0xFF757575);
                        break;
                }
            }
            
            if (tvDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String date = sdf.format(new Date(booking.getBookingDate()));
                tvDate.setText(date);
            }
            
            if (tvClientName != null) {
                tvClientName.setText("Client: " + (booking.getClientName() != null ? booking.getClientName() : "Unknown"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }
    }
}
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
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBudget, tvStatus, tvDeadline, tvClientName;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookingTitle);
            tvBudget = itemView.findViewById(R.id.tvBookingBudget);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvDeadline = itemView.findViewById(R.id.tvBookingDeadline);
            tvClientName = itemView.findViewById(R.id.tvClientName);
        }

        void bind(Booking booking, OnBookingClickListener listener) {
            tvTitle.setText(booking.getServiceTitle());
            tvBudget.setText("$" + booking.getPrice());
            tvStatus.setText(booking.getStatus());
            
            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String date = sdf.format(new Date(booking.getBookingDate()));
            tvDeadline.setText("Booked: " + date);
            tvClientName.setText("Client: " + booking.getClientName());

            // Set status color
            switch (booking.getStatus()) {
                case "pending":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                    break;
                case "confirmed":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                    break;
                case "in_progress":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_purple));
                    break;
                case "completed":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
                case "cancelled":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.black));
                    break;
            }

            itemView.setOnClickListener(v -> listener.onBookingClick(booking));
        }
    }
}
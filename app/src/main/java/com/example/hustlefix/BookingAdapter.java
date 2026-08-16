package com.example.hustlefix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

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
        String providerName = booking.getServiceProviderName();
        String status = booking.getStatus();
        double price = booking.getPrice();
        long timestamp = booking.getTimestamp();
        String imageUrl = booking.getProviderProfileImageUrl();
        
        holder.tvServiceName.setText(serviceName);
        holder.tvClientName.setText(providerName); // Changed from Client Name to Provider Name for "My Bookings"
        holder.tvPrice.setText(String.format("R%.2f", price));
        
        if (status == null) status = "pending";
        holder.tvStatus.setText(status.toUpperCase());
        
        // Color coding based on status using background badges
        if (status.equalsIgnoreCase("completed")) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_accepted);
        } else if (status.equalsIgnoreCase("pending")) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_pending);
        } else if (status.equalsIgnoreCase("cancelled")) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_red);
        } else if (status.equalsIgnoreCase("accepted") || status.equalsIgnoreCase("confirmed")) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_accepted);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(timestamp));
        holder.tvDate.setText(dateStr);

        // Load profile image
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .into(holder.ivProviderProfile);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });

        // Add fade-in animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
        holder.itemView.startAnimation(animation);
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
        CircleImageView ivProviderProfile;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivProviderProfile = itemView.findViewById(R.id.ivProviderProfile);
        }
    }
}
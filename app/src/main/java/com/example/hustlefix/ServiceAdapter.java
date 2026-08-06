package com.example.hustlefix;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
        void onEditClick(Service service);
        void onDeleteClick(Service service);
    }

    public ServiceAdapter(List<Service> services, OnServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service, listener);
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvCategory, tvStatus, tvDate;
        Button btnEdit, btnDelete;
        ImageView ivServiceBanner;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvServiceTitle);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvStatus = itemView.findViewById(R.id.tvServiceStatus);
            tvDate = itemView.findViewById(R.id.tvServiceDate);
            btnEdit = itemView.findViewById(R.id.btnEditService);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);
            ivServiceBanner = itemView.findViewById(R.id.ivServiceBanner);
        }

        void bind(Service service, OnServiceClickListener listener) {
            tvTitle.setText(service.getTitle());
            tvPrice.setText("R" + String.format("%.2f", service.getPrice()));
            tvCategory.setText(service.getCategory() != null ? service.getCategory() : "General");

            if (service.getServiceImageUrl() != null && !service.getServiceImageUrl().isEmpty()) {
                ivServiceBanner.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(service.getServiceImageUrl())
                        .centerCrop()
                        .into(ivServiceBanner);
            } else {
                ivServiceBanner.setVisibility(View.GONE);
            }

            String status = service.getStatus() != null ? service.getStatus() : "active";
            tvStatus.setText(status.toUpperCase());
            if (status.equals("active")) {
                tvStatus.setTextColor(0xFF4CAF50);
            } else {
                tvStatus.setTextColor(0xFFF44336);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvDate.setText("Posted: " + sdf.format(new Date(service.getCreatedAt())));

            itemView.setOnClickListener(v -> listener.onServiceClick(service));
            btnEdit.setOnClickListener(v -> listener.onEditClick(service));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(service));
        }
    }
}
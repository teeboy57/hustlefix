package com.example.hustlefix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ServiceDiscoveryAdapter extends RecyclerView.Adapter<ServiceDiscoveryAdapter.ServiceViewHolder> {

    private List<Service> services;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServiceDiscoveryAdapter(List<Service> services, OnServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    public void updateList(List<Service> newList) {
        this.services = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_discovery, parent, false);
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
        TextView tvTitle, tvPrice, tvCategory, tvServiceProvider, tvDescription;
        CircleImageView ivProviderProfile;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvServiceTitle);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvServiceProvider = itemView.findViewById(R.id.tvServiceProviderName);
            tvDescription = itemView.findViewById(R.id.tvServiceDescription);
            ivProviderProfile = itemView.findViewById(R.id.ivProviderProfile);
        }

        void bind(Service service, OnServiceClickListener listener) {
            if (tvTitle != null) {
                tvTitle.setText(service.getTitle() != null ? service.getTitle() : "No Title");
            }
            if (tvPrice != null) {
                tvPrice.setText("$" + String.format("%.2f", service.getPrice()));
            }
            if (tvCategory != null) {
                tvCategory.setText(service.getCategory() != null ? service.getCategory() : "General");
            }
            if (tvServiceProvider != null) {
                tvServiceProvider.setText("By: " + (service.getserviceProviderName() != null ? service.getserviceProviderName() : "Unknown"));
            }
            if (tvDescription != null) {
                tvDescription.setText(service.getDescription() != null ? service.getDescription() : "");
            }

            if (ivProviderProfile != null) {
                Glide.with(itemView.getContext())
                        .load(service.getServiceProviderProfileImageUrl())
                        .placeholder(R.drawable.ic_profile_default)
                        .error(R.drawable.ic_profile_default)
                        .into(ivProviderProfile);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceClick(service);
                }
            });
        }
    }
}
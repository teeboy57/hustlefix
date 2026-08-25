package com.example.hustlefix;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        
        // Add fade-in animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvCategory, tvServiceProvider, tvDescription;
        CircleImageView ivProviderProfile;
        ImageView ivServiceBanner;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvServiceTitle);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvServiceProvider = itemView.findViewById(R.id.tvServiceProviderName);
            ivProviderProfile = itemView.findViewById(R.id.ivProviderProfile);
            ivServiceBanner = itemView.findViewById(R.id.ivServiceBanner);
        }

        void bind(Service service, OnServiceClickListener listener) {
            String imageUrl = null;
            if (service.getServiceImageUrls() != null && !service.getServiceImageUrls().isEmpty()) {
                imageUrl = service.getServiceImageUrls().get(0);
            } else if (service.getServiceImageUrl() != null) {
                imageUrl = service.getServiceImageUrl();
            }

            if (ivServiceBanner != null) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ivServiceBanner.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .centerCrop()
                            .into(ivServiceBanner);
                    
                    String finalImageUrl = imageUrl;
                    ivServiceBanner.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, finalImageUrl);
                        itemView.getContext().startActivity(intent);
                    });
                } else {
                    ivServiceBanner.setImageResource(R.drawable.ic_image_placeholder);
                    ivServiceBanner.setVisibility(View.VISIBLE);
                }
            }
            
            if (tvTitle != null) {
                tvTitle.setText(service.getTitle() != null ? service.getTitle() : "No Title");
            }
            if (tvPrice != null) {
                tvPrice.setText("R" + String.format(Locale.getDefault(), "%.2f", service.getPrice()));
            }
            if (tvCategory != null) {
                tvCategory.setText(service.getCategory() != null ? service.getCategory() : "General");
            }
            if (tvServiceProvider != null) {
                tvServiceProvider.setText(service.getserviceProviderName() != null ? service.getserviceProviderName() : "Unknown");
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
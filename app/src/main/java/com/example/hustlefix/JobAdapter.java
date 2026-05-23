package com.example.hustlefix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobs;
    private String currentUserId;
    private String userRole;
    private OnJobActionListener listener;

    public interface OnJobActionListener {
        void onViewDetails(Job job);
        void onApply(Job job);
        void onDelete(Job job);
        void onChat(Job job);
        void onViewApplications(Job job);
    }

    public JobAdapter(List<Job> jobs, String currentUserId, String userRole) {
        this.jobs = jobs != null ? jobs : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.userRole = userRole;
    }

    public void setOnJobActionListener(OnJobActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Job> newJobs) {
        this.jobs = newJobs != null ? newJobs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.bind(job);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    class JobViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvBudget, tvLocation, tvStatus, tvPostedBy;
        private MaterialButton btnAction1, btnAction2, btnChat;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvBudget = itemView.findViewById(R.id.tvJobBudget);
            tvLocation = itemView.findViewById(R.id.tvJobLocation);
            tvStatus = itemView.findViewById(R.id.tvJobStatus);
            tvPostedBy = itemView.findViewById(R.id.tvPostedBy);
            btnAction1 = itemView.findViewById(R.id.btnAction1);
            btnAction2 = itemView.findViewById(R.id.btnAction2);
            btnChat = itemView.findViewById(R.id.btnChat);
        }

        void bind(Job job) {
            tvTitle.setText(job.getTitle());
            tvBudget.setText(job.getFormattedBudget());
            tvLocation.setText(job.getLocation());
            tvStatus.setText(job.getStatus());

            if (job.getPostedByName() != null) {
                tvPostedBy.setText("Posted by: " + job.getPostedByName());
            } else {
                tvPostedBy.setVisibility(View.GONE);
            }

            // Set status color
            switch (job.getStatus()) {
                case "open":
                    tvStatus.setBackgroundColor(0xFF4CAF50);
                    break;
                case "in_progress":
                    tvStatus.setBackgroundColor(0xFFFF9800);
                    break;
                case "completed":
                    tvStatus.setBackgroundColor(0xFF2196F3);
                    break;
                default:
                    tvStatus.setBackgroundColor(0xFF9E9E9E);
            }

            // Configure buttons based on role
            if ("CLIENT".equals(userRole) && job.isOwner(currentUserId)) {
                // Client viewing their own job
                btnAction1.setText("Details");
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) listener.onViewDetails(job);
                });
                btnAction2.setText("Applications");
                btnAction2.setOnClickListener(v -> {
                    if (listener != null) listener.onViewApplications(job);
                });
                btnAction2.setVisibility(View.VISIBLE);
            } else if ("ENTREPRENEUR".equals(userRole)) {
                // Entrepreneur viewing jobs
                btnAction1.setText("Details");
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) listener.onViewDetails(job);
                });
                if (job.isOwner(currentUserId)) {
                    btnAction2.setText("Applications");
                    btnAction2.setOnClickListener(v -> {
                        if (listener != null) listener.onViewApplications(job);
                    });
                    btnAction2.setVisibility(View.VISIBLE);
                } else {
                    btnAction2.setText("Apply");
                    btnAction2.setOnClickListener(v -> {
                        if (listener != null) listener.onApply(job);
                    });
                    btnAction2.setVisibility(View.VISIBLE);
                }
            }

            btnChat.setOnClickListener(v -> {
                if (listener != null) listener.onChat(job);
            });
        }
    }
}
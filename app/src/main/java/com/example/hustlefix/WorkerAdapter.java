package com.example.hustlefix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;
public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {
    private List<Worker> workerList;
    private FindWorkersActivity activity;
    private OnWorkerActionListener listener;
    public interface OnWorkerActionListener {
        void onHireClick(Worker worker);
        void onViewProfileClick(Worker worker);
        void onChatClick(Worker worker);
        void onQuoteForJob(Worker worker);
    }
    public void setOnWorkerActionListener(OnWorkerActionListener listener) {
        this.listener = listener;
    }
    public WorkerAdapter(List<Worker> workerList, FindWorkersActivity activity) {
        this.workerList = workerList;
        this.activity = activity;
    }
    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_worker, parent, false);
        return new WorkerViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);
        holder.tvWorkerName.setText(worker.getName() != null ? worker.getName() : "Unknown");
        holder.tvSkill.setText(worker.getSkill() != null ? worker.getSkill() : "No skill specified");
        holder.tvLocation.setText(worker.getLocation() != null ? worker.getLocation() : "Location not specified");
        holder.tvRating.setText(worker.getFormattedRating());
        holder.tvExperience.setText(worker.getExperience() + " yrs");
        holder.tvCompletedJobs.setText(worker.getCompletedJobs() + " jobs");
        holder.tvHourlyRate.setText(worker.getFormattedHourlyRate());
        // Set availability indicator
        if (worker.isAvailable()) {
            holder.tvAvailable.setVisibility(View.VISIBLE);
            holder.tvAvailable.setText("● Available");
            holder.tvAvailable.setTextColor(0xFF4CAF50);
        } else {
            holder.tvAvailable.setVisibility(View.VISIBLE);
            holder.tvAvailable.setText("● Busy");
            holder.tvAvailable.setTextColor(0xFFFF4444);
        }
        // Set rating bar
        holder.rbRating.setRating(worker.getRating().floatValue());
        // Load profile image if available
        if (worker.getProfileImage() != null && !worker.getProfileImage().isEmpty()) {
            Glide.with(activity)
                    .load(worker.getProfileImage())
                    .placeholder(R.drawable.ic_profile_default)
                    .error(R.drawable.ic_profile_default)
                    .circleCrop()
                    .into(holder.ivProfileImage);
            holder.ivProfileImage.setVisibility(View.VISIBLE);
            holder.tvInitials.setVisibility(View.GONE);
        } else {
            holder.ivProfileImage.setVisibility(View.GONE);
            holder.tvInitials.setVisibility(View.VISIBLE);
            holder.tvInitials.setText(worker.getInitials());
        }
        // Set click listeners
        holder.btnHire.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHireClick(worker);
            } else {
                activity.hireWorker(worker);
            }
        });
        holder.btnViewProfile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfileClick(worker);
            } else {
                activity.viewWorkerProfile(worker);
            }
        });
        holder.btnChat.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(worker);
            } else {
                activity.chatWithWorker(worker);
            }
        });
        // Quote button - show available jobs for this worker to quote on
        holder.btnQuote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuoteForJob(worker);
            } else {
                showAvailableJobsForWorker(worker);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            activity.viewWorkerProfile(worker);
        });

        // Add fade-in animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
        holder.itemView.startAnimation(animation);
    }
    private void showAvailableJobsForWorker(Worker worker) {
        // Get available jobs from Firebase
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        jobsRef.orderByChild("status").equalTo("open").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                java.util.List<Job> jobList = new java.util.ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Job job = data.getValue(Job.class);
                    if (job != null) {
                        job.setJobId(data.getKey());
                        // Don't show worker's own jobs
                        if (!worker.getId().equals(job.getPostedBy())) {
                            jobList.add(job);
                        }
                    }
                }
                if (jobList.isEmpty()) {
                    android.widget.Toast.makeText(activity,
                            "No available jobs at the moment", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                showJobSelectionDialog(jobList, worker);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.widget.Toast.makeText(activity,
                        "Failed to load jobs", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showJobSelectionDialog(java.util.List<Job> jobList, Worker worker) {
        String[] jobTitles = new String[jobList.size()];
        for (int i = 0; i < jobList.size(); i++) {
            jobTitles[i] = jobList.get(i).getTitle() + " - " + jobList.get(i).getFormattedBudget();
        }
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Select Job to Quote")
                .setItems(jobTitles, (dialog, which) -> {
                    Job selectedJob = jobList.get(which);
                    // Call the activity's method to show job details and send quote
                    activity.showJobDetailsDialog(selectedJob);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public int getItemCount() {
        return workerList != null ? workerList.size() : 0;
    }
    public void updateList(List<Worker> newList) {
        this.workerList = newList;
        notifyDataSetChanged();
    }
    static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkerName, tvSkill, tvLocation, tvRating, tvExperience, tvCompletedJobs;
        TextView tvHourlyRate, tvAvailable, tvInitials;
        RatingBar rbRating;
        Button btnHire, btnViewProfile, btnChat, btnQuote;
        CircleImageView ivProfileImage;
        WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvSkill = itemView.findViewById(R.id.tvSkill);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvExperience = itemView.findViewById(R.id.tvExperience);
            tvCompletedJobs = itemView.findViewById(R.id.tvCompletedJobs);
            tvHourlyRate = itemView.findViewById(R.id.tvHourlyRate);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            rbRating = itemView.findViewById(R.id.rbRating);
            btnHire = itemView.findViewById(R.id.btnHire);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnQuote = itemView.findViewById(R.id.btnQuote);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
        }
    }
}
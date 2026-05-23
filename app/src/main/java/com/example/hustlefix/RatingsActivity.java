package com.example.hustlefix;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class RatingsActivity extends AppCompatActivity {

    // Header views
    private ImageView ivBack, ivHistory;

    // Average rating card views
    private TextView tvAverageRating, tvTotalRatings;
    private RatingBar rbAverageRating;
    private ProgressBar progress5, progress4, progress3, progress2, progress1;
    private TextView tvCount5, tvCount4, tvCount3, tvCount2, tvCount1;

    // Rating section views
    private RatingBar ratingBar;
    private TextView tvRatingLabel;
    private EditText etReview;
    private CheckBox cbAnonymous;
    private Button btnSubmitRating;
    private ProgressBar progressBar;

    // Recent reviews
    private RecyclerView recyclerReviews;
    private TextView tvViewAll;

    private DatabaseReference ratingsRef;
    private DatabaseReference jobsRef;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    private List<Rating> ratingList;
    private ReviewsAdapter reviewsAdapter;
    private List<Job> completedJobs;
    private String selectedJobId;
    private String selectedWorkerId;
    private String selectedWorkerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        initViews();
        setupClickListeners();
        setupFirebase();
        setupRatingListener();
        loadCompletedJobs();
        loadRecentReviews();
        loadUserRatingStats();
    }

    private void initViews() {
        // Header
        ivBack = findViewById(R.id.ivBack);
        ivHistory = findViewById(R.id.ivHistory);

        // Average rating card
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalRatings = findViewById(R.id.tvTotalRatings);
        rbAverageRating = findViewById(R.id.rbAverageRating);
        progress5 = findViewById(R.id.progress5);
        progress4 = findViewById(R.id.progress4);
        progress3 = findViewById(R.id.progress3);
        progress2 = findViewById(R.id.progress2);
        progress1 = findViewById(R.id.progress1);
        tvCount5 = findViewById(R.id.tvCount5);
        tvCount4 = findViewById(R.id.tvCount4);
        tvCount3 = findViewById(R.id.tvCount3);
        tvCount2 = findViewById(R.id.tvCount2);
        tvCount1 = findViewById(R.id.tvCount1);

        // Rating section
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingLabel = findViewById(R.id.tvRatingLabel);
        etReview = findViewById(R.id.etReview);
        cbAnonymous = findViewById(R.id.cbAnonymous);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
        progressBar = findViewById(R.id.progressBar);

        // Recent reviews
        recyclerReviews = findViewById(R.id.recyclerReviews);
        tvViewAll = findViewById(R.id.tvViewAll);

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        ratingList = new ArrayList<>();
        reviewsAdapter = new ReviewsAdapter(ratingList);
        recyclerReviews.setAdapter(reviewsAdapter);

        completedJobs = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.onOptionsItemSelected(this, item) || super.onOptionsItemSelected(item);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivBack.setOnLongClickListener(v -> {
            NavigationHelper.showNavigationDialog(RatingsActivity.this);
            return true;
        });

        ivHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Rating history coming soon", Toast.LENGTH_SHORT).show();
        });

        tvViewAll.setOnClickListener(v -> {
            Toast.makeText(this, "All reviews coming soon", Toast.LENGTH_SHORT).show();
        });

        btnSubmitRating.setOnClickListener(v -> submitRating());
    }

    private void setupFirebase() {
        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRatingListener() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            String label;
            if (rating == 5) {
                label = "Excellent! ⭐⭐⭐⭐⭐";
            } else if (rating >= 4) {
                label = "Very Good! ⭐⭐⭐⭐";
            } else if (rating >= 3) {
                label = "Good ⭐⭐⭐";
            } else if (rating >= 2) {
                label = "Average ⭐⭐";
            } else if (rating > 0) {
                label = "Poor ⭐";
            } else {
                label = "Tap to rate";
            }
            tvRatingLabel.setText(label);
        });
    }

    private void loadUserRatingStats() {
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float rating = snapshot.child("rating").getValue(Float.class);
                Integer totalRatings = snapshot.child("totalRatings").getValue(Integer.class);

                if (rating != null) {
                    tvAverageRating.setText(String.format("%.1f", rating));
                    rbAverageRating.setRating(rating);
                } else {
                    tvAverageRating.setText("0");
                    rbAverageRating.setRating(0);
                }

                if (totalRatings != null) {
                    tvTotalRatings.setText(totalRatings + " ratings");
                } else {
                    tvTotalRatings.setText("0 ratings");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCompletedJobs() {
        progressBar.setVisibility(View.VISIBLE);

        jobsRef.orderByChild("postedBy").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        completedJobs.clear();
                        for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                            String assignedTo = jobSnapshot.child("assignedTo").getValue(String.class);
                            if (assignedTo != null && !assignedTo.isEmpty()) {
                                Job job = new Job();
                                job.setJobId(jobSnapshot.getKey());
                                job.setTitle(jobSnapshot.child("title").getValue(String.class));
                                job.setAssignedTo(assignedTo);
                                completedJobs.add(job);
                                loadWorkerName(job);
                            }
                        }

                        if (completedJobs.isEmpty()) {
                            Toast.makeText(RatingsActivity.this,
                                    "No completed jobs to rate yet", Toast.LENGTH_LONG).show();
                            btnSubmitRating.setEnabled(false);
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RatingsActivity.this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadWorkerName(Job job) {
        String workerId = job.getAssignedTo();
        if (workerId != null) {
            usersRef.child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String workerName = snapshot.child("name").getValue(String.class);
                    if (workerName != null) {
                        job.setAssignedToName(workerName);
                        if (selectedJobId == null && !completedJobs.isEmpty()) {
                            selectJob(completedJobs.get(0));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void selectJob(Job job) {
        selectedJobId = job.getJobId();
        selectedWorkerId = job.getAssignedTo();
        selectedWorkerName = job.getAssignedToName();

        Toast.makeText(this, "Rating: " + job.getTitle(), Toast.LENGTH_SHORT).show();
        checkIfAlreadyRated();
    }

    private void checkIfAlreadyRated() {
        if (selectedJobId == null) return;

        ratingsRef.orderByChild("jobId").equalTo(selectedJobId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnSubmitRating.setEnabled(false);
                            btnSubmitRating.setText("ALREADY RATED");
                            btnSubmitRating.setAlpha(0.5f);
                        } else {
                            btnSubmitRating.setEnabled(true);
                            btnSubmitRating.setText("SUBMIT RATING");
                            btnSubmitRating.setAlpha(1f);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void submitRating() {
        if (selectedJobId == null) {
            Toast.makeText(this, "No job selected to rate", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String review = etReview.getText().toString().trim();
        boolean isAnonymous = cbAnonymous.isChecked();

        setLoading(true);

        String ratingId = ratingsRef.push().getKey();
        if (ratingId == null) {
            setLoading(false);
            Toast.makeText(this, "Failed to create rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String jobTitle = getJobTitle(selectedJobId);

        Rating ratingObj = new Rating(
                selectedJobId, jobTitle,
                currentUser.getUid(), currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
                selectedWorkerId, selectedWorkerName != null ? selectedWorkerName : "Worker",
                rating, review, isAnonymous
        );
        ratingObj.setId(ratingId);

        ratingsRef.child(ratingId).setValue(ratingObj)
                .addOnSuccessListener(aVoid -> {
                    updateWorkerAverageRating();
                    updateRatingDistribution();

                    Toast.makeText(RatingsActivity.this,
                            "Rating submitted! Thank you!", Toast.LENGTH_LONG).show();

                    ratingBar.setRating(0);
                    etReview.setText("");
                    cbAnonymous.setChecked(false);
                    btnSubmitRating.setEnabled(false);
                    btnSubmitRating.setText("RATED");
                    btnSubmitRating.setAlpha(0.5f);

                    loadRecentReviews();
                    loadUserRatingStats();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(RatingsActivity.this,
                            "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRatingDistribution() {
        ratingsRef.orderByChild("ratedId").equalTo(selectedWorkerId)
                .get().addOnSuccessListener(snapshot -> {
                    int count5 = 0, count4 = 0, count3 = 0, count2 = 0, count1 = 0;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Rating r = data.getValue(Rating.class);
                        if (r != null) {
                            int rating = (int) r.getRating();
                            switch (rating) {
                                case 5: count5++; break;
                                case 4: count4++; break;
                                case 3: count3++; break;
                                case 2: count2++; break;
                                case 1: count1++; break;
                            }
                        }
                    }

                    int total = count5 + count4 + count3 + count2 + count1;
                    if (total > 0) {
                        progress5.setProgress((count5 * 100) / total);
                        progress4.setProgress((count4 * 100) / total);
                        progress3.setProgress((count3 * 100) / total);
                        progress2.setProgress((count2 * 100) / total);
                        progress1.setProgress((count1 * 100) / total);

                        tvCount5.setText(String.valueOf(count5));
                        tvCount4.setText(String.valueOf(count4));
                        tvCount3.setText(String.valueOf(count3));
                        tvCount2.setText(String.valueOf(count2));
                        tvCount1.setText(String.valueOf(count1));
                    }
                });
    }

    private String getJobTitle(String jobId) {
        for (Job job : completedJobs) {
            if (job.getJobId() != null && job.getJobId().equals(jobId)) {
                return job.getTitle();
            }
        }
        return "";
    }

    private void updateWorkerAverageRating() {
        ratingsRef.orderByChild("ratedId").equalTo(selectedWorkerId)
                .get().addOnSuccessListener(snapshot -> {
                    float totalRating = 0;
                    int count = 0;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Rating r = data.getValue(Rating.class);
                        if (r != null) {
                            totalRating += r.getRating();
                            count++;
                        }
                    }

                    float averageRating = count > 0 ? totalRating / count : 0;
                    usersRef.child(selectedWorkerId).child("rating").setValue(averageRating);
                    usersRef.child(selectedWorkerId).child("totalRatings").setValue(count);
                    setLoading(false);
                });
    }

    private void loadRecentReviews() {
        ratingsRef.orderByChild("timestamp").limitToLast(20)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Rating rating = data.getValue(Rating.class);
                            if (rating != null) {
                                ratingList.add(0, rating);
                            }
                        }
                        reviewsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitRating.setEnabled(!isLoading);
        btnSubmitRating.setText(isLoading ? "SUBMITTING..." : "SUBMIT RATING");
    }

    // ReviewsAdapter inner class
    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
        private List<Rating> reviews;

        ReviewsAdapter(List<Rating> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Rating review = reviews.get(position);
            holder.tvReviewerName.setText(review.getDisplayName());
            holder.tvRating.setText(review.getFormattedRating());
            holder.rbRating.setRating(review.getRating());
            holder.tvReview.setText(review.getReview() != null && !review.getReview().isEmpty() ?
                    review.getReview() : "No review text provided");
            holder.tvDate.setText(review.getFormattedDate());
            holder.tvJobTitle.setText(review.getJobTitle());
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView tvReviewerName, tvRating, tvReview, tvDate, tvJobTitle;
            RatingBar rbRating;

            ReviewViewHolder(View itemView) {
                super(itemView);
                tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
                tvRating = itemView.findViewById(R.id.tvRating);
                rbRating = itemView.findViewById(R.id.rbRating);
                tvReview = itemView.findViewById(R.id.tvReview);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            }
        }
    }
}
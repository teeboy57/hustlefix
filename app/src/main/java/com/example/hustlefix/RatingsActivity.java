package com.example.hustlefix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RatingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvAverageRating, tvTotalReviews, tvEmpty;
    private RecyclerView rvReviews;

    private DatabaseReference bookingsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<Review> reviewList;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        initViews();
        setupToolbar();
        loadReviews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvReviews = findViewById(R.id.rvReviews);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ratings and Reviews");
        }
    }

    private void loadReviews() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("serviceProviderId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        setLoading(false);
                        reviewList.clear();

                        if (!snapshot.exists()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvReviews.setVisibility(View.GONE);
                            tvAverageRating.setText("0.0 Star");
                            tvTotalReviews.setText("0 reviews");
                            return;
                        }

                        double totalRating = 0;
                        int reviewCount = 0;

                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking != null && booking.getRating() > 0) {
                                Review review = new Review();
                                review.setRating(booking.getRating());
                                review.setClientName(booking.getClientName() != null ? booking.getClientName() : "Anonymous");
                                review.setServiceTitle(booking.getServiceTitle() != null ? booking.getServiceTitle() : "Service");
                                // reviewDate will be set later
                                review.setComment(booking.getNotes() != null ? booking.getNotes() : "No comment provided");
                                
                                reviewList.add(review);
                                totalRating += booking.getRating();
                                reviewCount++;
                            }
                        }

                        if (reviewList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvReviews.setVisibility(View.GONE);
                            tvAverageRating.setText("0.0 Star");
                            tvTotalReviews.setText("0 reviews");
                            return;
                        }

                        // Calculate average
                        double avgRating = totalRating / reviewCount;
                        tvAverageRating.setText(String.format("%.1f Star", avgRating));
                        tvTotalReviews.setText(reviewCount + " reviews");

                        // Sort by date (newest first)
                        reviewList.sort((r1, r2) -> Long.compare(r2.getReviewDate(), r1.getReviewDate()));
                        
                        reviewAdapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setLoading(false);
                        Toast.makeText(RatingsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Inner Review class
    private static class Review {
        private double rating;
        private String clientName;
        private String serviceTitle;
        private long reviewDate;
        private String comment;

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getServiceTitle() { return serviceTitle; }
        public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }
        public long getReviewDate() { return reviewDate; }
        public void setReviewDate(long reviewDate) { this.reviewDate = reviewDate; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    // Review Adapter
    private static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

        private List<Review> reviews;

        public ReviewAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.bind(review);
        }

        @Override
        public int getItemCount() {
            return reviews != null ? reviews.size() : 0;
        }

        static class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView tvClientName, tvServiceTitle, tvRating, tvComment, tvDate;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                tvClientName = itemView.findViewById(R.id.tvClientName);
                tvServiceTitle = itemView.findViewById(R.id.tvServiceTitle);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvComment = itemView.findViewById(R.id.tvComment);
                tvDate = itemView.findViewById(R.id.tvDate);
            }

            void bind(Review review) {
                tvClientName.setText(review.getClientName());
                tvServiceTitle.setText(review.getServiceTitle());
                tvRating.setText(String.format("%.1f Star", review.getRating()));
                tvComment.setText(review.getComment());
                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvDate.setText(sdf.format(new Date(review.getReviewDate())));
            }
        }
    }
}
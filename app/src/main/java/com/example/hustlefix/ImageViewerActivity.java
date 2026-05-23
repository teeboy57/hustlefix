package com.example.hustlefix;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";

    private ImageView ivFullImage;
    private ProgressBar progressBar;
    private TextView tvError;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        initViews();
        setupToolbar();
        loadImage();
    }

    private void initViews() {
        ivFullImage = findViewById(R.id.ivFullImage);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Image Preview");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadImage() {
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        if (imageUrl == null || imageUrl.isEmpty()) {
            showError("No image URL provided");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.ic_image_error)
                .into(ivFullImage);

        progressBar.setVisibility(View.GONE);
        ivFullImage.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }
}
package com.example.hustlefix;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL = "image_url";
    private ImageView ivFullImage;
    private ImageView btnDownload;
    private ProgressBar progressBar;
    private TextView tvError;
    private Toolbar toolbar;
    private String imageUrl;
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
        btnDownload = findViewById(R.id.btnDownload);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        toolbar = findViewById(R.id.toolbar);
        
        btnDownload.setOnClickListener(v -> downloadImage());
    }
    
    private void downloadImage() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Cannot download: URL missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle("HustleFix Download");
            request.setDescription("Downloading work photo...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "HustleFix_" + System.currentTimeMillis() + ".jpg");
            
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
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
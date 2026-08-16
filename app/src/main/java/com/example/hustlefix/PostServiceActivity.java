package com.example.hustlefix;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostServiceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextInputEditText etTitle, etDescription, etPrice, etLocation;
    private ChipGroup chipGroupCategory;
    private MaterialButton btnSubmitService;
    private ProgressBar progressBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    
    private RecyclerView rvPickedImages;
    private PickedImageAdapter pickedImageAdapter;
    private List<Uri> imageUris = new ArrayList<>();
    private Uri tempUri; // For camera
    private View layoutAddPhoto;
    
    private String selectedCategory = "";
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userRole = "";

    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        userRole = SessionHelper.getRole(this);
        
        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupFirebase();
        setupLaunchers();
        setupClickListeners();
        setupCategorySelection();
        updateNavHeader();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etBudget);
        etLocation = findViewById(R.id.etLocation);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        btnSubmitService = findViewById(R.id.btnSubmitJob);
        progressBar = findViewById(R.id.progressBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        
        rvPickedImages = findViewById(R.id.rvPickedImages);
        layoutAddPhoto = findViewById(R.id.layoutAddPhoto);
        
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        pickedImageAdapter = new PickedImageAdapter(imageUris, position -> {
            imageUris.remove(position);
            pickedImageAdapter.notifyItemRemoved(position);
            if (imageUris.isEmpty()) {
                rvPickedImages.setVisibility(View.GONE);
                layoutAddPhoto.setVisibility(View.VISIBLE);
            }
        });
        rvPickedImages.setAdapter(pickedImageAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Post a Service");
        }
    }

    private void setupNavigationDrawer() {
        NavigationHelper.setupDrawer(this, drawerLayout, toolbar, navigationView);
    }

    private void updateNavHeader() {
        if (navigationView != null && navigationView.getHeaderView(0) != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
            TextView tvNavUserEmail = headerView.findViewById(R.id.tvNavUserEmail);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String name = currentUser.getDisplayName();
                if (TextUtils.isEmpty(name)) name = "User";
                if (tvNavUserName != null) tvNavUserName.setText(name);
                if (tvNavUserEmail != null) tvNavUserEmail.setText(currentUser.getEmail());
            }
        }
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("services");
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(10),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        imageUris.addAll(uris);
                        pickedImageAdapter.notifyDataSetChanged();
                        rvPickedImages.setVisibility(View.VISIBLE);
                        layoutAddPhoto.setVisibility(View.GONE);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadPreviewImage(tempUri);
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void loadPreviewImage(Uri uri) {
        if (uri == null) return;
        imageUris.add(uri);
        pickedImageAdapter.notifyDataSetChanged();
        rvPickedImages.setVisibility(View.VISIBLE);
        layoutAddPhoto.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnSubmitService.setOnClickListener(v -> postService());
        findViewById(R.id.cardServiceImage).setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Service Portfolio Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission();
                    else if (which == 1) openGallery();
                    else removePhoto();
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Service Photo");
        Uri cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        // Note: We need a way to pass cameraImageUri to the result launcher. 
        // For simplicity, let's just define a tempUri field.
        this.tempUri = cameraImageUri;
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        galleryLauncher.launch(new androidx.activity.result.PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void removePhoto() {
        imageUris.clear();
        pickedImageAdapter.notifyDataSetChanged();
        rvPickedImages.setVisibility(View.GONE);
        layoutAddPhoto.setVisibility(View.VISIBLE);
    }

    private void setupCategorySelection() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipPlumber) selectedCategory = "Plumber";
                else if (checkedId == R.id.chipElectrician) selectedCategory = "Electrician";
                else selectedCategory = "Other";
            } else {
                selectedCategory = "";
            }
        });
    }

    private void postService() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Required"); return; }
        if (TextUtils.isEmpty(description)) { etDescription.setError("Required"); return; }
        if (TextUtils.isEmpty(price)) { etPrice.setError("Required"); return; }
        if (TextUtils.isEmpty(selectedCategory)) { Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Required"); return; }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        setLoading(true);

        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = snapshot.child("profileImage").getValue(String.class);
                        proceedWithPosting(currentUser, title, description, price, location, profileImage);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setLoading(false);
                    }
                });
    }

    private void proceedWithPosting(FirebaseUser currentUser, String title, String description, String price, String location, String profileImage) {
        String serviceId = databaseReference.push().getKey();
        if (serviceId == null) { setLoading(false); return; }

        if (!imageUris.isEmpty()) {
            uploadMultipleImagesAndSave(serviceId, currentUser, title, description, price, location, profileImage);
        } else {
            saveServiceToDatabase(serviceId, currentUser, title, description, price, location, profileImage, null);
        }
    }

    private void uploadMultipleImagesAndSave(String serviceId, FirebaseUser currentUser, String title, String description, String price, String location, String profileImage) {
        List<String> uploadedUrls = new ArrayList<>();
        uploadRecursive(0, serviceId, currentUser, title, description, price, location, profileImage, uploadedUrls);
    }

    private void uploadRecursive(int index, String serviceId, FirebaseUser currentUser, String title, String description, String price, String location, String profileImage, List<String> uploadedUrls) {
        if (index >= imageUris.size()) {
            saveServiceToDatabase(serviceId, currentUser, title, description, price, location, profileImage, uploadedUrls);
            return;
        }

        MediaManager.get().upload(imageUris.get(index))
                .unsigned("hustle_fix")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        uploadedUrls.add((String) resultData.get("secure_url"));
                        uploadRecursive(index + 1, serviceId, currentUser, title, description, price, location, profileImage, uploadedUrls);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        setLoading(false);
                        Toast.makeText(PostServiceActivity.this, "Upload failed at image " + (index + 1), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveServiceToDatabase(String serviceId, FirebaseUser currentUser, String title, String description, String price, String location, String profileImage, List<String> serviceImageUrls) {
        Map<String, Object> service = new HashMap<>();
        service.put("serviceId", serviceId);
        service.put("title", title);
        service.put("description", description);
        service.put("price", Double.parseDouble(price));
        service.put("location", location);
        service.put("category", selectedCategory);
        service.put("status", "active");
        service.put("serviceProviderId", currentUser.getUid());
        service.put("serviceProviderName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        service.put("serviceProviderEmail", currentUser.getEmail());
        service.put("serviceProviderProfileImageUrl", profileImage);
        service.put("serviceImageUrls", serviceImageUrls);
        service.put("createdAt", System.currentTimeMillis());
        service.put("bookingsCount", 0);
        service.put("averageRating", 0);

        databaseReference.child(serviceId).setValue(service)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(PostServiceActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> setLoading(false));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        return NavigationHelper.handleNavigationItem(this, item.getItemId());
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitService.setEnabled(!isLoading);
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }
}
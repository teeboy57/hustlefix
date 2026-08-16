package com.example.hustlefix;

import android.Manifest;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditServiceActivity extends AppCompatActivity {

    private static final String TAG = "EditServiceActivity";
    private TextInputEditText etTitle, etDescription, etPrice, etLocation;
    private MaterialButton btnUpdateService, btnDeleteService, btnRemovePhoto;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    
    private RecyclerView rvPickedImages;
    private PickedImageAdapter pickedImageAdapter;
    private List<Uri> imageUris = new ArrayList<>();
    private List<String> existingUrls = new ArrayList<>();
    private Uri tempUri;
    private View layoutAddPhoto;

    private DatabaseReference servicesRef;
    private FirebaseAuth mAuth;
    private String serviceId;
    private Service currentService;

    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        serviceId = getIntent().getStringExtra("serviceId");
        if (serviceId == null) {
            Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupFirebase();
        setupLaunchers();
        loadServiceData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etLocation = findViewById(R.id.etLocation);
        btnUpdateService = findViewById(R.id.btnUpdateService);
        btnDeleteService = findViewById(R.id.btnDeleteService);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        progressBar = findViewById(R.id.progressBar);
        
        rvPickedImages = findViewById(R.id.rvPickedImages);
        layoutAddPhoto = findViewById(R.id.layoutAddPhoto);
        
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        pickedImageAdapter = new PickedImageAdapter(imageUris, position -> {
            imageUris.remove(position);
            pickedImageAdapter.notifyItemRemoved(position);
            if (imageUris.isEmpty() && existingUrls.isEmpty()) {
                rvPickedImages.setVisibility(View.GONE);
                layoutAddPhoto.setVisibility(View.VISIBLE);
                btnRemovePhoto.setVisibility(View.GONE);
            }
        });
        rvPickedImages.setAdapter(pickedImageAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Service");
        }
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        servicesRef = FirebaseDatabase.getInstance().getReference("services");
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
                        btnRemovePhoto.setVisibility(View.VISIBLE);
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
                }
        );
    }

    private void loadPreviewImage(Uri uri) {
        if (uri == null) return;
        imageUris.add(uri);
        pickedImageAdapter.notifyDataSetChanged();
        rvPickedImages.setVisibility(View.VISIBLE);
        layoutAddPhoto.setVisibility(View.GONE);
        btnRemovePhoto.setVisibility(View.VISIBLE);
    }

    private void loadServiceData() {
        setLoading(true);
        servicesRef.child(serviceId).get()
                .addOnSuccessListener(snapshot -> {
                    setLoading(false);
                    if (snapshot.exists()) {
                        currentService = snapshot.getValue(Service.class);
                        if (currentService != null) {
                            displayServiceData();
                        }
                    } else {
                        finish();
                    }
                })
                .addOnFailureListener(e -> setLoading(false));
    }

    private void displayServiceData() {
        etTitle.setText(currentService.getTitle());
        etDescription.setText(currentService.getDescription());
        etPrice.setText(String.valueOf(currentService.getPrice()));
        etLocation.setText(currentService.getLocation());
        
        if (currentService.getServiceImageUrls() != null && !currentService.getServiceImageUrls().isEmpty()) {
            existingUrls.clear();
            existingUrls.addAll(currentService.getServiceImageUrls());
            
            // For now, let's just convert strings to URIs for display in the same adapter
            imageUris.clear();
            for (String url : existingUrls) {
                imageUris.add(Uri.parse(url));
            }
            pickedImageAdapter.notifyDataSetChanged();
            
            rvPickedImages.setVisibility(View.VISIBLE);
            layoutAddPhoto.setVisibility(View.GONE);
            btnRemovePhoto.setVisibility(View.VISIBLE);
        } else {
            layoutAddPhoto.setVisibility(View.VISIBLE);
            btnRemovePhoto.setVisibility(View.GONE);
            rvPickedImages.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnUpdateService.setOnClickListener(v -> updateService());
        btnDeleteService.setOnClickListener(v -> confirmDelete());
        btnRemovePhoto.setOnClickListener(v -> removePhoto());
        findViewById(R.id.cardServiceImage).setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Service Photo")
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
        values.put(MediaStore.Images.Media.TITLE, "Edit Service Photo");
        Uri cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
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
        existingUrls.clear();
        pickedImageAdapter.notifyDataSetChanged();
        rvPickedImages.setVisibility(View.GONE);
        layoutAddPhoto.setVisibility(View.VISIBLE);
        btnRemovePhoto.setVisibility(View.GONE);
    }

    private void updateService() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr)) return;

        setLoading(true);
        
        // Logic: 
        // 1. Keep existing URLs that are still in imageUris (this is tricky with parsing URIs)
        // 2. Upload new local URIs from imageUris
        
        // For simplicity in this iteration, if any new images are picked, we re-upload all or just append.
        // Let's implement a clean "re-upload everything" for now if changed, or just update text if not.
        
        // Filter local URIs (those starting with content:// or file://)
        List<Uri> localUris = new ArrayList<>();
        List<String> remainingRemoteUrls = new ArrayList<>();
        
        for (Uri uri : imageUris) {
            if (uri.getScheme() != null && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                localUris.add(uri);
            } else {
                remainingRemoteUrls.add(uri.toString());
            }
        }

        if (!localUris.isEmpty()) {
            uploadNewImagesAndSave(localUris, remainingRemoteUrls, title, description, priceStr, location);
        } else {
            performUpdate(title, description, priceStr, location, remainingRemoteUrls);
        }
    }

    private void uploadNewImagesAndSave(List<Uri> localUris, List<String> remoteUrls, String title, String desc, String price, String loc) {
        List<String> allUrls = new ArrayList<>(remoteUrls);
        uploadRecursive(0, localUris, allUrls, title, desc, price, loc);
    }

    private void uploadRecursive(int index, List<Uri> localUris, List<String> allUrls, String title, String desc, String price, String loc) {
        if (index >= localUris.size()) {
            performUpdate(title, desc, price, loc, allUrls);
            return;
        }

        MediaManager.get().upload(localUris.get(index))
                .unsigned("hustle_fix")
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        allUrls.add((String) resultData.get("secure_url"));
                        uploadRecursive(index + 1, localUris, allUrls, title, desc, price, loc);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        setLoading(false);
                        Toast.makeText(EditServiceActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void performUpdate(String title, String desc, String price, String loc, List<String> imageUrls) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", desc);
        updates.put("price", Double.parseDouble(price));
        updates.put("location", loc);
        updates.put("serviceImageUrls", imageUrls);

        servicesRef.child(serviceId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(EditServiceActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> setLoading(false));
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> deleteService())
                .setNegativeButton("Cancel", null).show();
    }

    private void deleteService() {
        setLoading(true);
        servicesRef.child(serviceId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    finish();
                })
                .addOnFailureListener(e -> setLoading(false));
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUpdateService.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
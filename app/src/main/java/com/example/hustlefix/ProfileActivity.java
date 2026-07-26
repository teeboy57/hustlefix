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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Robust Profile Activity that handles image uploads for any format (PNG, JPEG, HEIC, etc.)
 * by re-encoding to a standardized compressed JPEG format.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private Toolbar toolbar;
    private ImageView ivProfileImage;
    private View cardProfileImage;
    private TextView tvChangePhoto;
    private TextInputEditText etDisplayName, etEmail, etPhone, etLocation;
    private MaterialButton btnSave, btnChangePassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri imageUri;
    private String currentPhotoUrl;
    private String userRole = "client";

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable("imageUri");
        }

        initViews();
        setupToolbar();
        setupFirebase();
        setupLaunchers();
        loadUserData();
        setupClickListeners();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) {
            outState.putParcelable("imageUri", imageUri);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        cardProfileImage = findViewById(R.id.cardProfileImage);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        loadPreviewImage(imageUri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadPreviewImage(imageUri);
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
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_default)
                .into(ivProfileImage);
    }

    private void loadUserData() {
        if (currentUser != null) {
            etDisplayName.setText(currentUser.getDisplayName());
            etEmail.setText(currentUser.getEmail());
            etEmail.setEnabled(false);

            databaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String phone = snapshot.child("phone").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        String photoUrl = snapshot.child("profileImage").getValue(String.class);
                        userRole = snapshot.child("role").getValue(String.class);

                        if (phone != null) etPhone.setText(phone);
                        if (location != null) etLocation.setText(location);
                        
                        if (imageUri == null && photoUrl != null && !photoUrl.isEmpty()) {
                            currentPhotoUrl = photoUrl;
                            loadPreviewImage(Uri.parse(photoUrl));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        }
    }

    private void setupClickListeners() {
        View.OnClickListener listener = v -> showImagePickerDialog();
        tvChangePhoto.setOnClickListener(listener);
        if (cardProfileImage != null) cardProfileImage.setOnClickListener(listener);
        
        btnSave.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission();
                    else if (which == 1) openGallery();
                    else removeProfilePhoto();
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
        values.put(MediaStore.Images.Media.TITLE, "New Profile Picture");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void removeProfilePhoto() {
        ivProfileImage.setImageResource(R.drawable.ic_profile_default);
        imageUri = null;
        currentPhotoUrl = null;
        Toast.makeText(this, "Photo removed. Save to apply.", Toast.LENGTH_SHORT).show();
    }

    private void saveProfile() {
        String displayName = etDisplayName.getText().toString().trim();
        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Name is required");
            return;
        }

        setLoading(true);

        if (currentUser != null && !displayName.equals(currentUser.getDisplayName())) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            currentUser.updateProfile(profileUpdates);
        }

        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (imageUri != null) {
            uploadImageAndSave(displayName, phone, location);
        } else {
            saveUserDataToDatabase(displayName, phone, location, currentPhotoUrl);
        }
    }

    private void uploadImageAndSave(String name, String phone, String location) {
        // Robust Format Fix: Standardize all uploads to JPEG using Glide's bitmap transformation.
        // This solves issues with HEIC, large PNGs, and incorrect rotation.
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .override(1024, 1024) // Optimal resolution for high-quality profile pics
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                        byte[] data = baos.toByteArray();

                        // Fixed filename ensures we overwrite the old one to save storage space
                        final StorageReference fileReference = storageReference.child(currentUser.getUid() + ".jpg");
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setContentType("image/jpeg")
                                .build();

                        fileReference.putBytes(data, metadata)
                                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    saveUserDataToDatabase(name, phone, location, uri.toString());
                                }))
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Incompatible image format. Please try another.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataToDatabase(String name, String phone, String location, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("location", location);
        updates.put("profileImage", photoUrl);

        databaseReference.child(currentUser.getUid()).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sync new photo to services so customers see updated avatar in search results
                        if ("worker".equals(userRole) && photoUrl != null) {
                            syncProfileImageToServices(photoUrl);
                        }
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Database error: Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void syncProfileImageToServices(String newImageUrl) {
        DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
        servicesRef.orderByChild("serviceProviderId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Map<String, Object> syncUpdates = new HashMap<>();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                syncUpdates.put(ds.getKey() + "/serviceProviderProfileImageUrl", newImageUrl);
                            }
                            servicesRef.updateChildren(syncUpdates);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Sync error: " + error.getMessage());
                    }
                });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);
        
        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String current = etCurrent.getText().toString().trim();
                    String newPass = etNew.getText().toString().trim();
                    if (!newPass.equals(etConfirm.getText().toString().trim())) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    changePassword(current, newPass);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void changePassword(String current, String newPass) {
        setLoading(true);
        if (currentUser == null || currentUser.getEmail() == null) {
            setLoading(false);
            return;
        }
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(currentUser.getEmail(), current);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPass).addOnCompleteListener(t -> {
                    setLoading(false);
                    Toast.makeText(this, t.isSuccessful() ? "Password updated" : "Update failed", Toast.LENGTH_SHORT).show();
                });
            } else {
                setLoading(false);
                Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnChangePassword.setEnabled(!isLoading);
    }
}
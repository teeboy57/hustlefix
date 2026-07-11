package com.example.hustlefix;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;
public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    // Views
    private Toolbar toolbar;
    private ImageView ivProfileImage;
    private TextView tvChangePhoto;
    private TextInputEditText etDisplayName, etEmail, etPhone, etLocation;
    private MaterialButton btnSave, btnChangePassword;
    private ProgressBar progressBar;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    // Image URI
    private Uri imageUri;
    private String currentPhotoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        setupToolbar();
        setupFirebase();
        loadUserData();
        setupClickListeners();
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
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
    private void loadUserData() {
        if (currentUser != null) {
            // Display Name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                etDisplayName.setText(displayName);
            }
            // Email
            String email = currentUser.getEmail();
            if (email != null) {
                etEmail.setText(email);
                etEmail.setEnabled(false); // Email cannot be changed
            }
            // Load additional data from Realtime Database
            databaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    String phone = snapshot.child("phone").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String photoUrl = snapshot.child("profileImage").getValue(String.class);
                    if (phone != null) etPhone.setText(phone);
                    if (location != null) etLocation.setText(location);
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        currentPhotoUrl = photoUrl;
                        Glide.with(ProfileActivity.this)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile_default)
                                .into(ivProfileImage);
                    }
                }
                @Override
                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void setupClickListeners() {
        tvChangePhoto.setOnClickListener(v -> showImagePickerDialog());
        btnSave.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }
    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Take Photo
                            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takeIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takeIntent, 2);
                            } else {
                                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1:
                            // Choose from Gallery
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                            break;
                        case 2:
                            // Remove Photo
                            removeProfilePhoto();
                            break;
                    }
                })
                .show();
    }
    private void removeProfilePhoto() {
        ivProfileImage.setImageResource(R.drawable.ic_profile_default);
        imageUri = null;
        currentPhotoUrl = null;
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            // Camera photo
            imageUri = data.getData();
            if (imageUri == null) {
                // Handle camera photo saved to temp file
                Bundle extras = data.getExtras();
                android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                // Convert bitmap to URI (simplified - in production you'd save to file)
                ivProfileImage.setImageBitmap(imageBitmap);
                Toast.makeText(this, "Photo selected from camera", Toast.LENGTH_SHORT).show();
            } else {
                ivProfileImage.setImageURI(imageUri);
            }
        }
    }
    private void saveProfile() {
        String displayName = etDisplayName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Name is required");
            etDisplayName.requestFocus();
            return;
        }
        setLoading(true);
        // Update Firebase Auth profile
        if (currentUser != null && !displayName.equals(currentUser.getDisplayName())) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            currentUser.updateProfile(profileUpdates);
        }
        // Upload image if selected
        if (imageUri != null) {
            uploadImageAndSave(displayName, phone, location);
        } else {
            saveUserDataToDatabase(displayName, phone, location, currentPhotoUrl);
        }
    }
    private void uploadImageAndSave(String displayName, String phone, String location) {
        final StorageReference fileReference = storageReference.child(currentUser.getUid() + ".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                currentPhotoUrl = imageUrl;
                                saveUserDataToDatabase(displayName, phone, location, imageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserDataToDatabase(String displayName, String phone, String location, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", displayName);
        updates.put("phone", phone);
        updates.put("location", location);
        if (photoUrl != null && !photoUrl.isEmpty()) {
            updates.put("profileImage", photoUrl);
        }
        databaseReference.child(currentUser.getUid()).updateChildren(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String currentPwd = etCurrentPassword.getText().toString().trim();
                    String newPwd = etNewPassword.getText().toString().trim();
                    String confirmPwd = etConfirmPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(currentPwd)) {
                        Toast.makeText(this, "Enter current password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(newPwd) || newPwd.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPwd.equals(confirmPwd)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    changePassword(currentPwd, newPwd);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void changePassword(String currentPassword, String newPassword) {
        setLoading(true);
        // Re-authenticate user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                setLoading(false);
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ProfileActivity.this, "Password updated successfully", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(ProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                setLoading(false);
                                Toast.makeText(ProfileActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnChangePassword.setEnabled(!isLoading);
    }
}
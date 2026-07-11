package com.example.hustlefix;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
public class EmergencyRequestActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CALL_PERMISSION_REQUEST_CODE = 1002;
    private Toolbar toolbar;
    private ChipGroup chipGroupEmergencyType;
    private EditText etDescription;
    private TextView tvLocation;
    private ProgressBar progressLocation;
    private CheckBox cbConfirm;
    private Button btnSendEmergency;
    private ProgressBar progressBar;
    private View cardSuccess;
    private Button btnCallPolice, btnCallAmbulance, btnCallFire;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private DatabaseReference emergencyRef;
    private FirebaseUser currentUser;
    private String selectedEmergencyType = "";
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private String currentAddress = "";
    private boolean locationReceived = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request);
        initViews();
        setupToolbar();
        setupFirebase();
        setupLocationServices();
        setupEmergencyTypeSelection();
        setupEmergencyCalls();
        setupClickListeners();
        requestLocationPermissions();
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupEmergencyType = findViewById(R.id.chipGroupEmergencyType);
        etDescription = findViewById(R.id.etDescription);
        tvLocation = findViewById(R.id.tvLocation);
        progressLocation = findViewById(R.id.progressLocation);
        cbConfirm = findViewById(R.id.cbConfirm);
        btnSendEmergency = findViewById(R.id.btnSendEmergency);
        progressBar = findViewById(R.id.progressBar);
        cardSuccess = findViewById(R.id.cardSuccess);
        btnCallPolice = findViewById(R.id.btnCallPolice);
        btnCallAmbulance = findViewById(R.id.btnCallAmbulance);
        btnCallFire = findViewById(R.id.btnCallFire);
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.emergency);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_navigation, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationHelper.onOptionsItemSelected(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupFirebase() {
        emergencyRef = FirebaseDatabase.getInstance().getReference("emergency_requests");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to send emergency requests", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    applyLocation(location.getLatitude(), location.getLongitude());
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        };
    }
    private void applyLocation(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        locationReceived = true;
        getAddressFromLocation(latitude, longitude);
    }
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        progressLocation.setVisibility(View.VISIBLE);
        tvLocation.setText(R.string.loading);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        applyLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        fusedLocationClient.requestLocationUpdates(
                                locationRequest, locationCallback, Looper.getMainLooper());
                    }
                })
                .addOnFailureListener(e -> {
                    progressLocation.setVisibility(View.GONE);
                    tvLocation.setText("Tap refresh in settings or try again outdoors");
                });
    }
    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                currentAddress = addresses.get(0).getAddressLine(0);
                tvLocation.setText(currentAddress);
            } else {
                tvLocation.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", latitude, longitude));
            }
        } catch (IOException e) {
            tvLocation.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", latitude, longitude));
        }
        progressLocation.setVisibility(View.GONE);
    }
    private void setupEmergencyTypeSelection() {
        chipGroupEmergencyType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds != null && !checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipMedical) {
                    selectedEmergencyType = "Medical";
                } else if (checkedId == R.id.chipFire) {
                    selectedEmergencyType = "Fire";
                } else if (checkedId == R.id.chipSecurity) {
                    selectedEmergencyType = "Security";
                } else if (checkedId == R.id.chipAccident) {
                    selectedEmergencyType = "Accident";
                } else if (checkedId == R.id.chipOther) {
                    selectedEmergencyType = "Other";
                } else {
                    Chip chip = findViewById(checkedId);
                    selectedEmergencyType = chip != null ? chip.getText().toString() : "";
                }
            } else {
                selectedEmergencyType = "";
            }
        });
    }
    private void setupEmergencyCalls() {
        btnCallPolice.setOnClickListener(v -> makePhoneCall("10111"));
        btnCallAmbulance.setOnClickListener(v -> makePhoneCall("10177"));
        btnCallFire.setOnClickListener(v -> makePhoneCall("10177"));
    }
    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
    private void setupClickListeners() {
        btnSendEmergency.setOnClickListener(v -> sendEmergencyRequest());
    }
    private void sendEmergencyRequest() {
        if (selectedEmergencyType.isEmpty()) {
            Toast.makeText(this, "Please select an emergency type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!locationReceived) {
            Toast.makeText(this, "Waiting for location. Please enable GPS and try again.", Toast.LENGTH_LONG).show();
            getCurrentLocation();
            return;
        }
        if (!cbConfirm.isChecked()) {
            Toast.makeText(this, "Please confirm this is a genuine emergency", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        String description = etDescription.getText().toString().trim();
        String userName = currentUser.getDisplayName();
        if (userName == null || userName.isEmpty()) {
            String email = currentUser.getEmail();
            userName = email != null && email.contains("@") ? email.split("@")[0] : "User";
        }
        String emergencyId = emergencyRef.push().getKey();
        EmergencyRequest request = new EmergencyRequest(
                currentUser.getUid(), userName,
                currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "",
                selectedEmergencyType, description, currentLatitude, currentLongitude, currentAddress
        );
        request.setId(emergencyId);
        emergencyRef.child(emergencyId).setValue(request)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    showSuccess();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(EmergencyRequestActivity.this,
                            "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void showSuccess() {
        cardSuccess.setVisibility(View.VISIBLE);
        btnSendEmergency.setEnabled(false);
    }
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendEmergency.setEnabled(!isLoading);
        btnSendEmergency.setText(isLoading ? "SENDING..." : "SEND EMERGENCY REQUEST");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required for emergency requests",
                        Toast.LENGTH_LONG).show();
                tvLocation.setText("Location permission denied â€” enter area in description");
                progressLocation.setVisibility(View.GONE);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}

package com.example.gigsfinder.local;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.example.gigsfinder.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.gigsfinder.local.models.Job;
import com.example.gigsfinder.local.models.User;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PostJobActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etLocation, etPayRate, etDuration;
    private Spinner spinnerJobType, spinnerPayType;
    private Button btnPost, btnUseCurrentLocation;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude, longitude;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etPayRate = findViewById(R.id.etPayRate);
        etDuration = findViewById(R.id.etDuration);
        spinnerJobType = findViewById(R.id.spinnerJobType);
        spinnerPayType = findViewById(R.id.spinnerPayType);
        btnPost = findViewById(R.id.btnPost);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        progressBar = findViewById(R.id.progressBar);

        setupSpinners();
        loadUserData();

        btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postJob();
            }
        });
    }

    private void setupSpinners() {
        String[] jobTypes = {"Casual Work", "Tutoring", "Service", "Other"};
        ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jobTypes);
        jobTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJobType.setAdapter(jobTypeAdapter);

        String[] payTypes = {"Hourly", "Fixed", "Daily"};
        ArrayAdapter<String> payTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, payTypes);
        payTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayType.setAdapter(payTypeAdapter);
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUser = documentSnapshot.toObject(User.class);
                });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        getAddressFromLocation(location);
                    }
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getLocality() + ", " + address.getAdminArea();
                etLocation.setText(addressText);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Could not get address", Toast.LENGTH_SHORT).show();
        }
    }

    private void postJob() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String payRate = etPayRate.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String jobType = spinnerJobType.getSelectedItem().toString().toLowerCase().replace(" ", "_");
        String payType = spinnerPayType.getSelectedItem().toString().toLowerCase();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            return;
        }

        if (TextUtils.isEmpty(payRate)) {
            etPayRate.setError("Pay rate is required");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Job job = new Job(currentUser.getUserId(), currentUser.getName(), title, description);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setPayRate(payRate);
        job.setPayType(payType);
        job.setDuration(duration);
        job.setLatitude(latitude);
        job.setLongitude(longitude);

        db.collection("jobs")
                .add(job)
                .addOnSuccessListener(documentReference -> {
                    job.setJobId(documentReference.getId());
                    documentReference.set(job);

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Job posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error posting job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
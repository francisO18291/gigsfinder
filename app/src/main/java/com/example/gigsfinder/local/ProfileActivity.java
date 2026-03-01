package com.example.gigsfinder.local;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gigsfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.gigsfinder.local.models.User;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;

    private TextView tvEmail, tvUserType, tvRating;
    private EditText etName, etPhone, etLocation, etSkills;
    private Button btnUpdate, btnUploadResume;
    private TextView tvResumeStatus;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private User currentUser;
    private Uri resumeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Check if user is logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        tvEmail = findViewById(R.id.tvEmail);
        tvUserType = findViewById(R.id.tvUserType);
        tvRating = findViewById(R.id.tvRating);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        etSkills = findViewById(R.id.etSkills);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUploadResume = findViewById(R.id.btnUploadResume);
        tvResumeStatus = findViewById(R.id.tvResumeStatus);
        progressBar = findViewById(R.id.progressBar);

        loadUserProfile();

        btnUploadResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = firebaseUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        displayUserData();
                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayUserData() {
        tvEmail.setText(currentUser.getEmail());
        tvUserType.setText("Account Type: " + currentUser.getUserType().toUpperCase());
        tvRating.setText(String.format("Rating: %.1f (%d reviews)", currentUser.getRating(), currentUser.getTotalRatings()));

        etName.setText(currentUser.getName());

        if (currentUser.getPhone() != null) {
            etPhone.setText(currentUser.getPhone());
        }

        if (currentUser.getLocation() != null) {
            etLocation.setText(currentUser.getLocation());
        }

        if (currentUser.getSkills() != null && !currentUser.getSkills().isEmpty()) {
            etSkills.setText(String.join(", ", currentUser.getSkills()));
        }

        if (currentUser.getResumeUrl() != null && !currentUser.getResumeUrl().isEmpty()) {
            tvResumeStatus.setText("Resume uploaded ✓");
            tvResumeStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        if ("employer".equals(currentUser.getUserType())) {
            btnUploadResume.setVisibility(View.GONE);
            tvResumeStatus.setVisibility(View.GONE);
            etSkills.setVisibility(View.GONE);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            resumeUri = data.getData();
            uploadResume();
        }
    }

    private void uploadResume() {
        if (resumeUri == null || currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);
        StorageReference resumeRef = storage.getReference()
                .child("resumes/" + currentUser.getUserId() + ".pdf");

        resumeRef.putFile(resumeUri)
                .addOnSuccessListener(taskSnapshot -> {
                    resumeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        currentUser.setResumeUrl(uri.toString());
                        db.collection("users").document(currentUser.getUserId())
                                .update("resumeUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    tvResumeStatus.setText("Resume uploaded ✓");
                                    tvResumeStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                    Toast.makeText(this, "Resume uploaded successfully", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error uploading resume: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String skillsText = etSkills.getText().toString().trim();

        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setLocation(location);

        if (!skillsText.isEmpty()) {
            String[] skillsArray = skillsText.split(",");
            java.util.List<String> skillsList = new java.util.ArrayList<>();
            for (String skill : skillsArray) {
                skillsList.add(skill.trim());
            }
            currentUser.setSkills(skillsList);
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(currentUser.getUserId())
                .set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
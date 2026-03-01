package com.example.gigsfinder.local;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gigsfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.gigsfinder.local.models.Application;
import com.example.gigsfinder.local.models.Job;
import com.example.gigsfinder.local.models.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JobDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvEmployer, tvLocation, tvJobType, tvPayRate, tvDuration, tvDescription, tvPostedDate;
    private Button btnApply;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String jobId;
    private Job currentJob;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        jobId = getIntent().getStringExtra("jobId");

        tvTitle = findViewById(R.id.tvTitle);
        tvEmployer = findViewById(R.id.tvEmployer);
        tvLocation = findViewById(R.id.tvLocation);
        tvJobType = findViewById(R.id.tvJobType);
        tvPayRate = findViewById(R.id.tvPayRate);
        tvDuration = findViewById(R.id.tvDuration);
        tvDescription = findViewById(R.id.tvDescription);
        tvPostedDate = findViewById(R.id.tvPostedDate);
        btnApply = findViewById(R.id.btnApply);
        progressBar = findViewById(R.id.progressBar);

        loadJobDetails();
        loadUserData();

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showApplyDialog();
            }
        });
    }

    private void loadJobDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("jobs").document(jobId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    currentJob = documentSnapshot.toObject(Job.class);
                    if (currentJob != null) {
                        displayJobDetails();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading job details", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null && "employer".equals(currentUser.getUserType())) {
                        btnApply.setVisibility(View.GONE);
                    }
                });
    }

    private void displayJobDetails() {
        tvTitle.setText(currentJob.getTitle());
        tvEmployer.setText("Posted by: " + currentJob.getEmployerName());
        tvLocation.setText("Location: " + currentJob.getLocation());
        tvJobType.setText("Type: " + currentJob.getJobType().replace("_", " ").toUpperCase());
        tvPayRate.setText("Pay: $" + currentJob.getPayRate() + " (" + currentJob.getPayType() + ")");
        tvDuration.setText("Duration: " + currentJob.getDuration());
        tvDescription.setText(currentJob.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvPostedDate.setText("Posted: " + sdf.format(new Date(currentJob.getPostedAt())));
    }

    private void showApplyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_apply, null);
        builder.setView(dialogView);

        EditText etCoverLetter = dialogView.findViewById(R.id.etCoverLetter);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coverLetter = etCoverLetter.getText().toString().trim();
                submitApplication(coverLetter);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void submitApplication(String coverLetter) {
        progressBar.setVisibility(View.VISIBLE);

        Application application = new Application(jobId, currentJob.getTitle(),
                currentUser.getUserId(), currentUser.getName());
        application.setSeekerEmail(currentUser.getEmail());
        application.setSeekerPhone(currentUser.getPhone());
        application.setCoverLetter(coverLetter);
        application.setResumeUrl(currentUser.getResumeUrl());

        db.collection("applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    application.setApplicationId(documentReference.getId());
                    documentReference.set(application);

                    // Update applicant count
                    db.collection("jobs").document(jobId)
                            .update("applicantCount", currentJob.getApplicantCount() + 1);

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
                    btnApply.setEnabled(false);
                    btnApply.setText("Applied");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error submitting application", Toast.LENGTH_SHORT).show();
                });
    }
}
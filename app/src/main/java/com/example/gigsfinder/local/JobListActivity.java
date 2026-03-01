package com.example.gigsfinder.local;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gigsfinder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.gigsfinder.local.adapters.JobAdapter;
import com.example.gigsfinder.local.models.Job;
import com.example.gigsfinder.local.models.User;
import java.util.ArrayList;
import java.util.List;

public class JobListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FloatingActionButton fabPostJob;
    private Spinner spinnerJobType, spinnerPayType;
    private ProgressBar progressBar;
    private TextView tvUserTypeIndicator, tvEmptyState;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mAuth = FirebaseAuth.getInstance();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {

                Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(JobListActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }


            if (currentUser.isAnonymous()) {
                Toast.makeText(this, "Anonymous access not allowed", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                Intent intent = new Intent(JobListActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }


            if (!isNetworkAvailable()) {
                showNoInternetDialog();
                return;
            }

            setContentView(R.layout.activity_job_list);

            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            db = FirebaseFirestore.getInstance();

            recyclerView = findViewById(R.id.recyclerView);
            fabPostJob = findViewById(R.id.fabPostJob);
            spinnerJobType = findViewById(R.id.spinnerJobType);
            spinnerPayType = findViewById(R.id.spinnerPayType);
            progressBar = findViewById(R.id.progressBar);
            tvUserTypeIndicator = findViewById(R.id.tvUserTypeIndicator);
            tvEmptyState = findViewById(R.id.tvEmptyState);

            jobList = new ArrayList<>();
            jobAdapter = new JobAdapter(jobList, this);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(jobAdapter);

            setupSpinners();
            loadUserType();
            loadJobs(null, null);

            fabPostJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(JobListActivity.this, PostJobActivity.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();

            Intent intent = new Intent(JobListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(JobListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupSpinners() {
        String[] jobTypes = {"All Types", "Casual Work", "Tutoring", "Service", "Other"};
        ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jobTypes);
        jobTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJobType.setAdapter(jobTypeAdapter);

        String[] payTypes = {"All Pay Types", "Hourly", "Fixed", "Daily"};
        ArrayAdapter<String> payTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, payTypes);
        payTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayType.setAdapter(payTypeAdapter);

        spinnerJobType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFilters() {
        String jobType = spinnerJobType.getSelectedItem().toString();
        String payType = spinnerPayType.getSelectedItem().toString();

        String jobTypeFilter = jobType.equals("All Types") ? null : jobType.toLowerCase().replace(" ", "_");
        String payTypeFilter = payType.equals("All Pay Types") ? null : payType.toLowerCase();

        loadJobs(jobTypeFilter, payTypeFilter);
    }

    private void loadUserType() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        currentUserType = user.getUserType();


                        tvUserTypeIndicator.setVisibility(View.VISIBLE);

                        if ("employer".equals(currentUserType)) {
                            fabPostJob.setVisibility(View.VISIBLE);
                            tvUserTypeIndicator.setText("👤 Employer Mode - Post & Manage Jobs");
                            tvUserTypeIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            fabPostJob.setVisibility(View.GONE);
                            tvUserTypeIndicator.setText("🔍 Job Seeker Mode - Browse & Apply");
                            tvUserTypeIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user type: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadJobs(String jobType, String payType) {
        progressBar.setVisibility(View.VISIBLE);

        // Build query with Firestore (now uses indexes)
        Query query = db.collection("jobs")
                .whereEqualTo("status", "active")
                .orderBy("postedAt", Query.Direction.DESCENDING);

        if (jobType != null) {
            query = query.whereEqualTo("jobType", jobType);
        }

        if (payType != null) {
            query = query.whereEqualTo("payType", payType);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    jobList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        jobList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();

                    // Show empty state if no jobs
                    if (jobList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        if ("employer".equals(currentUserType)) {
                            tvEmptyState.setText("No jobs posted yet.\nTap the + button to post your first job!");
                        } else {
                            tvEmptyState.setText("No jobs available yet.\nCheck back soon!");
                        }
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);

                    // Check if it's an index error
                    if (e.getMessage() != null && e.getMessage().contains("index")) {
                        new AlertDialog.Builder(JobListActivity.this)
                                .setTitle("Database Setup Required")
                                .setMessage("The app needs to create database indexes. This is a one-time setup.\n\nPlease check the error message for a link to create the indexes automatically.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        Toast.makeText(JobListActivity.this, "Error loading jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_applications) {
            Intent intent = new Intent(this, ApplicationActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check authentication when returning to activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(JobListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Check network connectivity
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

        applyFilters();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("This app requires an internet connection to function. Please check your connection and try again.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (isNetworkAvailable()) {
                        recreate();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    finish();
                })
                .show();
    }
}
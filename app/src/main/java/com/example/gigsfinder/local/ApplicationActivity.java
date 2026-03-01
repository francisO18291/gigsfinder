package com.example.gigsfinder.local;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gigsfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.gigsfinder.local.adapters.ApplicationAdapter;
import com.example.gigsfinder.local.models.Application;
import com.example.gigsfinder.local.models.User;
import java.util.ArrayList;
import java.util.List;

public class ApplicationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ApplicationAdapter applicationAdapter;
    private List<Application> applicationList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        applicationList = new ArrayList<>();
        applicationAdapter = new ApplicationAdapter(applicationList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(applicationAdapter);

        loadUserTypeAndApplications();
    }

    private void loadUserTypeAndApplications() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = firebaseUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        userType = user.getUserType();
                        loadApplications();
                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadApplications() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = firebaseUser.getUid();

        if ("seeker".equals(userType)) {
            db.collection("applications")
                    .whereEqualTo("seekerId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        progressBar.setVisibility(View.GONE);
                        applicationList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Application application = document.toObject(Application.class);
                            applicationList.add(application);
                        }
                        applicationAdapter.notifyDataSetChanged();

                        if (applicationList.isEmpty()) {
                            Toast.makeText(this, "No applications yet", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("jobs")
                    .whereEqualTo("employerId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> jobIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            jobIds.add(document.getId());
                        }

                        if (jobIds.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "No jobs posted yet", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("applications")
                                .whereIn("jobId", jobIds)
                                .get()
                                .addOnSuccessListener(appSnapshots -> {
                                    progressBar.setVisibility(View.GONE);
                                    applicationList.clear();
                                    for (QueryDocumentSnapshot document : appSnapshots) {
                                        Application application = document.toObject(Application.class);
                                        applicationList.add(application);
                                    }
                                    applicationAdapter.notifyDataSetChanged();

                                    if (applicationList.isEmpty()) {
                                        Toast.makeText(this, "No applications received yet", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error loading jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
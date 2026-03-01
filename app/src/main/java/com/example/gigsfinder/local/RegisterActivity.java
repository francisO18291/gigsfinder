package com.example.gigsfinder.local;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gigsfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.gigsfinder.local.models.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etPhone;
    private RadioGroup rgUserType;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        rgUserType = findViewById(R.id.rgUserType);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        Login = findViewById(R.id.etfrancis);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser() {
        // Check internet first
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your connection.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        int selectedId = rgUserType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String userType = selectedRadio.getText().toString().toLowerCase();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = new User(userId, email, name, userType);
                            user.setPhone(phone);

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnRegister.setEnabled(true);

                                        new AlertDialog.Builder(RegisterActivity.this)
                                                .setTitle("Registration Successful!")
                                                .setMessage("Your account has been created successfully.")
                                                .setPositiveButton("Continue", (dialog, which) -> {
                                                    Intent intent = new Intent(RegisterActivity.this, JobListActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .setCancelable(false)
                                                .show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnRegister.setEnabled(true);

                                        // Delete the auth user if Firestore fails
                                        firebaseUser.delete();

                                        new AlertDialog.Builder(RegisterActivity.this)
                                                .setTitle("Registration Failed")
                                                .setMessage("Error creating user profile: " + e.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show();
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }

                        // Show user-friendly error messages
                        if (errorMessage.contains("already in use")) {
                            errorMessage = "This email is already registered. Please login instead.";
                        } else if (errorMessage.contains("network")) {
                            errorMessage = "Network error. Please check your connection.";
                        } else if (errorMessage.contains("weak password")) {
                            errorMessage = "Password is too weak. Please use a stronger password.";
                        }

                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle("Registration Failed")
                                .setMessage(errorMessage)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
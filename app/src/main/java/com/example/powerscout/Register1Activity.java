package com.example.powerscout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Register1Activity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextUsername;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        editTextEmail = findViewById(R.id.emailEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        editTextConfirmPassword = findViewById(R.id.confirmPasswordEditText);
        editTextUsername = findViewById(R.id.editUsername);

        findViewById(R.id.registerButton).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();

        // Input validation
        if (username.isEmpty()) {
            editTextUsername.setError("Username is required!");
            editTextUsername.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email!");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters!");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match!");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            try {
                                // Encrypt password before saving
                                String encryptedPassword = AESEncryption.encrypt(password);
                                saveUserToFirestore(user.getUid(), username, email, encryptedPassword);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(Register1Activity.this, "Encryption error!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(Register1Activity.this,
                                task.getException() != null ? task.getException().getMessage() : "Registration failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String username, String email, String encryptedPassword) {
        final DocumentReference counterRef = db.collection("counters").document("user_counter");

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef);

            int nextUserId;
            if (snapshot.exists() && snapshot.contains("current_id")) {
                nextUserId = snapshot.getLong("current_id").intValue() + 1;
                transaction.update(counterRef, "current_id", nextUserId);
            } else {
                nextUserId = 1; // Start user ID from 1
                transaction.set(counterRef, Collections.singletonMap("current_id", nextUserId));
            }

            return nextUserId;
        }).addOnSuccessListener(nextUserId -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", nextUserId);
            userData.put("username", username);
            userData.put("email", email);
            userData.put("uid", uid);
            userData.put("password", encryptedPassword);  // 🔹 Now encrypted
            userData.put("created_at", System.currentTimeMillis());

            db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Register1Activity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register1Activity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Register1Activity.this, "Account created but failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Firestore", "Failed to save user data", e);
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(Register1Activity.this, "Failed to generate user ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Transaction failed", e);
        });
    }
}

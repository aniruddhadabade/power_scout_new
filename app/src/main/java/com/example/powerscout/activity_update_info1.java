package com.example.powerscout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class activity_update_info1 extends BaseActivity {

    private EditText editEmail, editUsername, editPassword;
    private Button saveButton;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_info1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadUserData(currentUser);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigationDrawer();

        saveButton.setOnClickListener(v -> {
            updateUserInfo(() -> {
                Toast.makeText(this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(activity_update_info1.this, activity_update_info2.class));
            });
        });
    }

    private void loadUserData(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        if (firstName != null && lastName != null) {
                            TextView userNameTextView = findViewById(R.id.userNameTextView);
                            userNameTextView.setText(firstName + " " + lastName);
                        }

                        editUsername.setText(documentSnapshot.getString("username"));
                        editEmail.setText(documentSnapshot.getString("email"));

                        if (documentSnapshot.contains("password")) {
                            String encryptedPassword = documentSnapshot.getString("password");
                            try {
                                String decryptedPassword = AESEncryption.decrypt(encryptedPassword);
                                editPassword.setText(decryptedPassword);
                            } catch (Exception e) {
                                Log.e("Firestore", "Error decrypting password", e);
                                editPassword.setText("****");
                            }
                        } else {
                            Log.e("Firestore", "Password not found");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load user data", e));
    }

    private void updateUserInfo(Runnable onSuccess) {
        Log.d("Firestore", "updateUserInfo() called!");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = editEmail.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "⚠️ Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String encryptedPassword;
        try {
            encryptedPassword = AESEncryption.encrypt(password);
        } catch (Exception e) {
            Log.e("Encryption", "Error encrypting password", e);
            progressDialog.dismiss();
            Toast.makeText(this, "❌ Encryption error!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", encryptedPassword);

        Log.d("Firestore", "Updating user with: " + userData);

        db.collection("users").document(user.getUid()).set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Profile updated!");
                    progressDialog.dismiss();
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Update failed", e);
                    progressDialog.dismiss();
                    Toast.makeText(this, "❌ Profile update failed. Try again!", Toast.LENGTH_LONG).show();
                });
    }


}
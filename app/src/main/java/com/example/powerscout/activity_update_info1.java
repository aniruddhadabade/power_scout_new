package com.example.powerscout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
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
    private ImageView profileImage;
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_info1);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI elements
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);
        profileImage = findViewById(R.id.image1);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadUserData(currentUser);

        profileImage.setOnClickListener(v -> openFileChooser());

        saveButton.setOnClickListener(v -> updateUserInfo());

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize drawer and navigation view
        setupNavigationDrawer();

        // Handle Next button click
        Button nextButton = findViewById(R.id.saveButton);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity_update_info1.this, activity_update_info2.class);
            startActivity(intent);
        });
    }

    private void loadUserData(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editUsername.setText(documentSnapshot.getString("username"));
                        editEmail.setText(documentSnapshot.getString("email"));

                        // Fetch and display the password
                        if (documentSnapshot.contains("password")) {
                            editPassword.setText(documentSnapshot.getString("password"));
                            Log.d("Firestore", "Password retrieved: " + documentSnapshot.getString("password"));
                        } else {
                            Log.e("Firestore", "Password not found");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load user data", e));
    }



    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void updateUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = editEmail.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", password); // Store plain text password (⚠️ Insecure!)

        db.collection("users").document(user.getUid()).set(userData, SetOptions.merge()) // Merge to prevent overwriting
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Password stored successfully!");
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to store password", e);
                    progressDialog.dismiss();
                });
    }


    private void uploadProfileImage(String userId, Map<String, Object> userData) {
        StorageReference imageRef = storageRef.child("profile_images/" + userId + ".jpg");
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userData.put("profileImageUrl", uri.toString());
                    updateFirestoreAndAuth(mAuth.getCurrentUser(), userData, editEmail.getText().toString().trim(), editPassword.getText().toString().trim());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirestoreAndAuth(FirebaseUser user, Map<String, Object> userData, String newEmail, String newPassword) {
        db.collection("users").document(user.getUid()).update(userData)
                .addOnSuccessListener(aVoid -> {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userData.get("username").toString())
                            .build();
                    user.updateProfile(profileUpdates);

                    if (!newEmail.equals(user.getEmail())) {
                        updateEmail(user, newEmail, newPassword);
                    } else if (!newPassword.isEmpty()) {
                        updatePassword(user, newPassword);
                    } else {
                        finishUpdate();
                    }
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private void updateEmail(FirebaseUser user, String newEmail, String newPassword) {
        user.updateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful() && !newPassword.isEmpty()) {
                updatePassword(user, newPassword);
            } else {
                finishUpdate();
            }
        });
    }

    private void updatePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword).addOnCompleteListener(task -> finishUpdate());
    }

    private void finishUpdate() {
        progressDialog.dismiss();
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }
}
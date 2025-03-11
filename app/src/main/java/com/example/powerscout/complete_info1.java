package com.example.powerscout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class complete_info1 extends BaseActivity {

    private EditText editFirstName, editLastName, editPhone;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_info1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupNavigationDrawer();
        fetchUserName();

        // Initialize UI elements
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editPhone = findViewById(R.id.editPhone);
        saveButton = findViewById(R.id.saveButton);

        // Handle Next button click
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void saveUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String firstName = editFirstName.getText().toString().trim();
            String lastName = editLastName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reference to the logged-in user's document
            DocumentReference userRef = db.collection("users").document(userId);

            // Store data without deleting previous fields
            Map<String, Object> userData = new HashMap<>();
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("phone", phone);

            userRef.update(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(complete_info1.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(complete_info1.this, complete_info2.class));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(complete_info1.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");

                    if (username != null) {
                        updateUserName(username);
                    }
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(complete_info1.this, "Failed to fetch username", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // Update the TextView with the user's name
    private void updateUserName(String username) {
        TextView userNameTextView = findViewById(R.id.tvUserName);
        userNameTextView.setText("Hi, " + username);
    }

}

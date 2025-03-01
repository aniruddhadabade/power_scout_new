package com.example.powerscout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class complete_info2 extends BaseActivity {

    private EditText editAge, editAddress, editNoDevice;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_info2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        editAge = findViewById(R.id.editAge);
        editAddress = findViewById(R.id.editUsername);  // Assuming this is for Address
        editNoDevice = findViewById(R.id.editNoDevice);
        saveButton = findViewById(R.id.saveButton);

        // Handle Save button click
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void saveUserData() {
        // Get current logged-in user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String age = editAge.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String noOfDevices = editNoDevice.getText().toString().trim();

        if (age.isEmpty() || address.isEmpty() || noOfDevices.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a user data object (only new fields will be added/updated)
        Map<String, Object> userData = new HashMap<>();
        userData.put("age", age);
        userData.put("address", address);
        userData.put("no_of_devices", noOfDevices);

        // Use update() to add new fields without deleting existing ones
        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(complete_info2.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(complete_info2.this, complete_info3.class));
                })
                .addOnFailureListener(e -> {
                    // If document does not exist, use set() with merge()
                    db.collection("users").document(userId)
                            .set(userData, SetOptions.merge()) // Merges data instead of overwriting
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(complete_info2.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(complete_info2.this, complete_info3.class));
                            })
                            .addOnFailureListener(ex -> Toast.makeText(complete_info2.this, "Failed to update profile!", Toast.LENGTH_SHORT).show());
                });
    }
}


package com.example.powerscout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

public class activity_update_info3 extends BaseActivity {

    private EditText editAge, editAddress, editDevices;
    private Button saveButton;
    private TextView userNameTextView; // To show first and last name
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_info3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigationDrawer();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editAge = findViewById(R.id.editAge);
        editAddress = findViewById(R.id.editAddress);
        editDevices = findViewById(R.id.editedevice);
        saveButton = findViewById(R.id.saveButton);
        userNameTextView = findViewById(R.id.userName); // Reference to name TextView

        // Load existing user data
        loadUserData();

        // Update Firestore on button click
        saveButton.setOnClickListener(v -> updateUserData());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editAge.setText(documentSnapshot.getString("age"));
                        editAddress.setText(documentSnapshot.getString("address"));
                        editDevices.setText(documentSnapshot.getString("devices"));

                        // Fetch first name and last name
                        String firstName = documentSnapshot.getString("first_name");
                        String lastName = documentSnapshot.getString("last_name");

                        if (firstName != null && lastName != null) {
                            userNameTextView.setText(firstName + " " + lastName);
                        } else {
                            userNameTextView.setText("User Name");
                        }
                    } else {
                        Log.e("Firestore", "No user data found");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch data", e));
    }

    private void updateUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String age = editAge.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String devices = editDevices.getText().toString().trim();

        if (age.isEmpty() || address.isEmpty() || devices.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("age", age);
        userData.put("address", address);
        userData.put("devices", devices);

        db.collection("users").document(user.getUid()).set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e("Firestore", "Update failed", e));
    }
}
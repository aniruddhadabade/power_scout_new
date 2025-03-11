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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class activity_update_info2 extends BaseActivity {

    private static final String TAG = "activity_update_info2";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton buttonDrawerToggle;
    private TextView userNameTextView;
    private EditText editFirstName, editLastName, editPhone;
    private Button saveButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_info2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        // Initialize UI elements
        userNameTextView = findViewById(R.id.userNameTextView);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editPhone = findViewById(R.id.editPhone);
        saveButton = findViewById(R.id.saveButton);

        // Fetch and display user data
        if (user != null) {
            fetchUserData(user.getUid());
        } else {
            showToast("⚠️ User not logged in", Toast.LENGTH_LONG);
        }

        // Save button click event with callback to move to next activity
        saveButton.setOnClickListener(v -> updateUserProfile(() -> {
            showToast("✅ Profile updated successfully!", Toast.LENGTH_SHORT);
            startActivity(new Intent(activity_update_info2.this, activity_update_info3.class));
        }));

        setupNavigationDrawer();
    }

    private void fetchUserData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");
                    String phone = document.getString("phone");

                    editFirstName.setText(firstName != null ? firstName : "");
                    editLastName.setText(lastName != null ? lastName : "");
                    editPhone.setText(phone != null ? phone : "");

                    userNameTextView.setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                } else {
                    Log.d(TAG, "No user data found");
                    showToast("ℹ️ No user data available", Toast.LENGTH_SHORT);
                }
            } else {
                Log.e(TAG, "Error fetching user data", task.getException());
                showToast("❌ Failed to fetch data. Try again later.", Toast.LENGTH_LONG);
            }
        });
    }

    private void updateUserProfile(Runnable onSuccess) {
        if (user == null) {
            showToast("⚠️ No user detected!", Toast.LENGTH_SHORT);
            return;
        }

        String userId = user.getUid();
        String newFirstName = editFirstName.getText().toString().trim();
        String newLastName = editLastName.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();

        if (newFirstName.isEmpty() && newLastName.isEmpty() && newPhone.isEmpty()) {
            showToast("⚠️ Please enter at least one field to update.", Toast.LENGTH_SHORT);
            return;
        }

        // Create a map with only the updated fields
        Map<String, Object> updates = new HashMap<>();
        if (!newFirstName.isEmpty()) updates.put("firstName", newFirstName);
        if (!newLastName.isEmpty()) updates.put("lastName", newLastName);
        if (!newPhone.isEmpty()) updates.put("phone", newPhone);

        // Update Firestore document (merge to keep old fields)
        db.collection("users").document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    userNameTextView.setText(newFirstName + " " + newLastName);
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating profile", e);
                    showToast("❌ Profile update failed. Please try again!", Toast.LENGTH_LONG);
                });
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }
}
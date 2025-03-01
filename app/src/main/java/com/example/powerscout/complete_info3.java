package com.example.powerscout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class complete_info3 extends BaseActivity {

    private EditText editDevice, editDeviceCapacity, editBrandName;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_info3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore and Firebase Auth
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI elements
        editDevice = findViewById(R.id.editDevice);
        editDeviceCapacity = findViewById(R.id.editDeviceCapacity);
        editBrandName = findViewById(R.id.editBrandName);
        saveButton = findViewById(R.id.saveButton);

        // Setup navigation drawer
        setupNavigationDrawer();

        // Handle Save button click
        saveButton.setOnClickListener(v -> saveDataToFirestore());
    }

    private void saveDataToFirestore() {
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Create a map with user inputs
        Map<String, Object> newData = new HashMap<>();
        newData.put("device_name", editDevice.getText().toString().trim());
        newData.put("device_capacity", editDeviceCapacity.getText().toString().trim());
        newData.put("brand_name", editBrandName.getText().toString().trim());

        // Update Firestore document without overwriting previous data
        db.collection("users").document(userId)
                .set(newData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> showSuccessPopup())
                .addOnFailureListener(e ->
                        Toast.makeText(complete_info3.this, "Failed to save data!", Toast.LENGTH_SHORT).show()
                );
    }

    private void showSuccessPopup() {
        // Inflate the custom layout for the popup
        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        // Set up button inside the dialog
        Button okButton = popupView.findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            // Navigate to HomeActivity
            Intent intent = new Intent(complete_info3.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Show the dialog
        dialog.show();
    }
}

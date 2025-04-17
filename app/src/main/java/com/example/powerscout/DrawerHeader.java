package com.example.powerscout;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DrawerHeader extends AppCompatActivity {

    private TextView textUserName, textUserEmail;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_header);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Initialize UI elements
        textUserName  = findViewById(R.id.textUserName);
        textUserEmail = findViewById(R.id.textUserEmail);

        // Fetch user details from Firestore
        loadUserInfo();
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get fields (ensure these keys match your Firestore docs)
                    String username = documentSnapshot.getString("username");
                    String email    = documentSnapshot.getString("email");

                    textUserName.setText(
                            username != null && !username.isEmpty()
                                    ? username
                                    : "No Name"
                    );
                    textUserEmail.setText(
                            email != null && !email.isEmpty()
                                    ? email
                                    : "No Email"
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}


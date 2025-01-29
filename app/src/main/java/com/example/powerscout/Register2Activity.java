package com.example.powerscout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register2Activity extends AppCompatActivity {

    private EditText editPhone, editAddress, editPassword, editConfirmPassword;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String username, email, name;
    private int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Retrieve data from Register1Activity
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");
        name = intent.getStringExtra("name");
        age = intent.getIntExtra("age", 0);

        // Initialize UI elements with corrected IDs
        editPhone = findViewById(R.id.textView3); // Phone Number
        editAddress = findViewById(R.id.textView5); // Address
        editPassword = findViewById(R.id.textView7); // Password
        editConfirmPassword = findViewById(R.id.editTextPassword); // Confirm Password
        registerButton = findViewById(R.id.button2); // Register Button

        registerButton.setOnClickListener(v -> {
            String password = editPassword.getText().toString().trim(); // Get password from input

            if (password.length() < 6) {
                editPassword.setError("Password must be at least 6 characters!");
                editPassword.requestFocus();
                return;
            }

            // Create user in Firebase Authentication with the email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Store user data in Firestore
                            String phone = editPhone.getText().toString().trim();
                            String address = editAddress.getText().toString().trim();

                            if (phone.isEmpty()) {
                                editPhone.setError("Phone number is required!");
                                editPhone.requestFocus();
                                return;
                            }

                            if (address.isEmpty()) {
                                editAddress.setError("Address is required!");
                                editAddress.requestFocus();
                                return;
                            }

                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("email", email);
                            user.put("name", name);
                            user.put("age", age);
                            user.put("phone", phone);
                            user.put("address", address);

                            db.collection("users")
                                    .document(mAuth.getCurrentUser().getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Register2Activity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register2Activity.this, MainActivity.class));
                                        finish(); // Close the activity
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(Register2Activity.this, "Error saving data!", Toast.LENGTH_SHORT).show()
                                    );
                        } else {
                            Toast.makeText(Register2Activity.this, "Registration failed! Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

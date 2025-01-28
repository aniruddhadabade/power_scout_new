package com.example.powerscout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register2Activity extends AppCompatActivity {

    private EditText editTextPhone, editTextAddress, editTextPassword, editTextConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String username, email, name, age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Retrieve data passed from Register1Activity
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        age = getIntent().getStringExtra("age");

        editTextPhone = findViewById(R.id.textView3);
        editTextAddress = findViewById(R.id.textView5);
        editTextPassword = findViewById(R.id.textView7);
        editTextConfirmPassword = findViewById(R.id.editTextPassword);

        findViewById(R.id.button2).setOnClickListener(v -> registerUser()); // Using lambda for click listener
    }

    private void registerUser() {
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Input validation using TextUtils
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required!");
            editTextPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            editTextAddress.setError("Address is required!");
            editTextAddress.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters!");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match!");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Complete user registration with password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Create a user document in the "users" collection with all user data
                        CollectionReference usersRef = db.collection("users");
                        usersRef.document(userId).set(new User(username, email, name, age, phone, address)) // Corrected User usage
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // User registration complete, navigate to LoginActivity
                                            Intent intent = new Intent(Register2Activity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(Register2Activity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(Register2Activity.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Define the User class within Register2Activity (or in a separate file)
    public static class User {
        public String username;
        public String email;
        public String name;
        public String age;
        public String phone;
        public String address;

        public User(String username, String email, String name, String age, String phone, String address) {
            this.username = username;
            this.email = email;
            this.name = name;
            this.age = age;
            this.phone = phone;
            this.address = address;
        }
    }
}
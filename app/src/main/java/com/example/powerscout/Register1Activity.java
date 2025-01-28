package com.example.powerscout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register1Activity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextName, editAge;
    private FirebaseAuth mAuth;
    private int userAge; // Declare userAge outside registerUser()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        editTextUsername = findViewById(R.id.textView3);
        editTextEmail = findViewById(R.id.textView5);
        editTextName = findViewById(R.id.textView7);
        editAge = findViewById(R.id.editAge);

        findViewById(R.id.button2).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();

        if (username.isEmpty()) {
            editTextUsername.setError("Username is required!");
            editTextUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address!");
            editTextEmail.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            editTextName.setError("Name is required!");
            editTextName.requestFocus();
            return;
        }

        if (ageStr.isEmpty()) {
            editAge.setError("Age is required!");
            editAge.requestFocus();
            return;
        }

        try {
            userAge = Integer.parseInt(ageStr.replaceAll("\\D", ""));

            mAuth.createUserWithEmailAndPassword(email, "")
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // User registration successful, proceed to Register2Activity
                                Intent intent = new Intent(Register1Activity.this, Register2Activity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("email", email);
                                intent.putExtra("name", name);
                                intent.putExtra("age", userAge);
                                startActivity(intent);
                                finish();
                            } else {
                                // Handle registration failure
                                Toast.makeText(Register1Activity.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (NumberFormatException e) {
            editAge.setError("Invalid age format. Please enter a valid number.");
            editAge.requestFocus();
        }
    }
}
package com.example.powerscout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register1Activity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Ensure correct XML file name

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.emailEditText); // Updated ID
        editTextPassword = findViewById(R.id.passwordEditText); // Updated ID
        editTextConfirmPassword = findViewById(R.id.confirmPasswordEditText); // Updated ID


        findViewById(R.id.registerButton).setOnClickListener(v -> registerUser()); // Updated ID
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email!");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) { // Example minimum password length
            editTextPassword.setError("Password should be at least 6 characters!");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match!");
            editTextConfirmPassword.requestFocus();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration success
                            Toast.makeText(Register1Activity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to Login Activity
                            Intent intent = new Intent(Register1Activity.this, LoginActivity.class); // Replace LoginActivity
                            startActivity(intent);
                            finish();
                        } else {
                            // Registration failed
                            String errorMessage = task.getException().getMessage(); // Get specific error
                            Toast.makeText(Register1Activity.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
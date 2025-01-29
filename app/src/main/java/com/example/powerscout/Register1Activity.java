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
    private int userAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Ensure correct XML file

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

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email!");
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

            mAuth.createUserWithEmailAndPassword(email, "DefaultPassword123") // Change password logic later
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Proceed to Register2Activity with user data
                                Intent intent = new Intent(Register1Activity.this, Register2Activity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("email", email);
                                intent.putExtra("name", name);
                                intent.putExtra("age", userAge);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Register1Activity.this, "Registration failed! Try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (NumberFormatException e) {
            editAge.setError("Enter a valid age.");
            editAge.requestFocus();
        }
    }
}

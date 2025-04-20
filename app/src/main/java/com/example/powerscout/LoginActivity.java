package com.example.powerscout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException; // Add this import
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential; // Add this import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private TextView recoverPasswordText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;  // Request code for Google Sign-In

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        editTextEmail = findViewById(R.id.editTextUserName);
        editTextPassword = findViewById(R.id.editTextPassword);
        recoverPasswordText = findViewById(R.id.textView6);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Get your Web Client ID from Firebase
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set click listeners
        recoverPasswordText.setOnClickListener(v -> {
            // Navigate to ResetPasswordActivity
            Intent resetPasswordIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(resetPasswordIntent);
        });

        findViewById(R.id.button2).setOnClickListener(v -> userLogin());

        // Google Sign-In Button Click
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> signInWithGoogle());
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }

        findViewById(R.id.button2).setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // ✅ Show success immediately
                            showLoginSuccessSnackbar();

                            // 🚀 Send token in background
                            user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String idToken = tokenTask.getResult().getToken();
                                    sendTokenToCloudRun(idToken); // Send silently
                                } else {
                                    Log.e("Token Error", "Failed to retrieve token");
                                }
                            });
                        }
                    } else {
                        findViewById(R.id.button2).setEnabled(true);
                        Exception exception = task.getException();
                        Toast.makeText(LoginActivity.this, "Login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void showLoginSuccessSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Login Successful!", Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue)); // Customize background color
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.white)); // Customize text color
        snackbar.show();

        // Immediately redirect to HomeActivity (don't wait for ESP)
        Intent intent = new Intent(LoginActivity.this, complete_info1.class);
        startActivity(intent);
        finish();
    }


    private void sendTokenToCloudRun(String token) {
        String cloudRunUrl = "https://verifytoken-z724mvhueq-uc.a.run.app";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, cloudRunUrl,
                response -> {
                    // Cloud Run response, you can process but don't block login
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        if (message.equals("Token is valid")) {
                            String uid = jsonResponse.getString("uid");
                            // Pass UID to ESP8266 for further processing
                            passUidToESP8266(uid);  // This is a background task, won't affect login
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle error in Cloud Run response, but no need to block login
                    }
                },
                error -> {
                    // Handle error, but login still continues
                    Log.e("Volley Error", error.getMessage());
                    Toast.makeText(LoginActivity.this, "Error sending token to Cloud Run", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", token);  // Sending Firebase token
                return params;
            }
        };

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }



    private void passUidToESP8266(String uid) {
        String esp8266Url = "http://192.168.0.106/sendUid";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, esp8266Url,
                response -> {
                    Log.d("ESP8266 Response", response);
                    // Handle successful response from ESP8266, but login already completed
                    // No need to wait for this to complete before showing dashboard
                },
                error -> {
                    Log.e("ESP8266 Error", error.toString());
                    // If the ESP connection fails, we just log the error, but do not block login
                    Toast.makeText(LoginActivity.this, "Failed to connect with ESP", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uid", uid);  // Sending UID to ESP8266
                return params;
            }
        };

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }





    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class); // Use ApiException here
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null); // AuthCredential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserInFirestore(user.getEmail());
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore(String email) {
        // Assuming the user exists and moving to MainActivity
        Intent intent = new Intent(LoginActivity.this, activity_update_info1.class);
        startActivity(intent);
        finish();
    }
}

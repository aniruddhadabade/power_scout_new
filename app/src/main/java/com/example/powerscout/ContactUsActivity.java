package com.example.powerscout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContactUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_us);

        // Initialize UI elements
        TextView phoneText = findViewById(R.id.tvPhone);
        TextView emailText = findViewById(R.id.tvEmail);
        TextView locationText = findViewById(R.id.tvLocation);

        // Handle system bars properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigationDrawer();

        // Call phone number
        phoneText.setOnClickListener(v -> callPhone(v));

        // Send email
        emailText.setOnClickListener(v -> sendEmail(v));

        // Open location in Google Maps
        locationText.setOnClickListener(v -> openMap(v));
    }

    // Call Phone Method
    public void callPhone(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+919876543210"));
        startActivity(intent);
    }

    // Send Email Method
    public void sendEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@example.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support Inquiry");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello, I need assistance with...");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

    // Open Map Method
    public void openMap(View view) {
        Uri gmmIntentUri = Uri.parse("geo:16.7050,74.2433?q=Mumbai, Maharashtra, India");
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
package com.example.powerscout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class BaseActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    protected ImageButton buttonDrawerToggle;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

    protected void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonDrawerToggle.setOnClickListener(v -> drawerLayout.open());

        View headerView = navigationView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.userImage);
        TextView textUserName = headerView.findViewById(R.id.textUserName);

        userImage.setOnClickListener(view ->
                Toast.makeText(BaseActivity.this, textUserName.getText(), Toast.LENGTH_SHORT).show()
        );

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navAccount) {
                    Toast.makeText(BaseActivity.this, "Account Clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(BaseActivity.this, activity_update_info1.class));
                } else if (itemId == R.id.navCompleteInfo) {
                    Toast.makeText(BaseActivity.this, "Complete Info Clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(BaseActivity.this, complete_info1.class));
                } else if (itemId == R.id.navDasboard) {
                    Toast.makeText(BaseActivity.this, "Dashboard Clicked", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navHistory) {
                    Toast.makeText(BaseActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navContact) {
                    Toast.makeText(BaseActivity.this, "Contact Clicked", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navFeedback) {
                    Toast.makeText(BaseActivity.this, "Feedback Clicked", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navShare) {
                    Toast.makeText(BaseActivity.this, "Share Clicked", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navTerms) {
                    Toast.makeText(BaseActivity.this, "Terms Clicked", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navTips) {
                    Toast.makeText(BaseActivity.this, "Tips Clicked", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.close();
                return true;
            }
        });
    }
}

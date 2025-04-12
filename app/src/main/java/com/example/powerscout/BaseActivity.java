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

        if (drawerLayout == null || buttonDrawerToggle == null || navigationView == null) {
            // These views don't exist in this activity's layout, so skip setting up navigation drawer
            return;
        }

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

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navAccount) {
                startActivity(new Intent(BaseActivity.this, activity_update_info1.class));
            } else if (itemId == R.id.navCompleteInfo) {
                startActivity(new Intent(BaseActivity.this, complete_info1.class));
            } else if (itemId == R.id.navDasboard) {
                startActivity(new Intent(BaseActivity.this, Dashboard.class));
            } else if (itemId == R.id.navHistory) {
                Toast.makeText(BaseActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.navContact) {
                startActivity(new Intent(BaseActivity.this, ContactUsActivity.class));
            } else if (itemId == R.id.navFeedback) {
                startActivity(new Intent(BaseActivity.this, FeedbackActivity.class));
            } else if (itemId == R.id.navShare) {
                Toast.makeText(BaseActivity.this, "Share Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.navTerms) {
                startActivity(new Intent(BaseActivity.this, TermsConditionActivity.class));
            } else if (itemId == R.id.navTips) {
                startActivity(new Intent(BaseActivity.this, EnergySavingActivity.class));
            } else if (itemId == R.id.logout) {
                startActivity(new Intent(BaseActivity.this, MainActivity.class));
            }

            drawerLayout.close();
            return true;
        });
    }

}

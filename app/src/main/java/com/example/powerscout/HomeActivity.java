package com.example.powerscout;

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

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        View headerView = navigationView.getHeaderView(0);
        ImageView useImage = headerView.findViewById(R.id.userImage);
        TextView textUserName = headerView.findViewById(R.id.textUserName);

        useImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this, textUserName.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId == R.id.navAccount){
                    Toast.makeText(HomeActivity.this, "Account Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navDasboard){
                    Toast.makeText(HomeActivity.this, "DashBoard Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navHistory){
                    Toast.makeText(HomeActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navContact){
                    Toast.makeText(HomeActivity.this, "Contact Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navFeedback){
                    Toast.makeText(HomeActivity.this, "Feedback Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navShare){
                    Toast.makeText(HomeActivity.this, "Share Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navTerms){
                    Toast.makeText(HomeActivity.this, "Terms Clicked", Toast.LENGTH_SHORT).show();
                }

                if(itemId == R.id.navTips){
                    Toast.makeText(HomeActivity.this, "Tips  Clicked", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.close();

                return false;
            }
        });


    }
}
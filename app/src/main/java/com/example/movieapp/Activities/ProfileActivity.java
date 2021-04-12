package com.example.movieapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.movieapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                        break;
                    case R.id.nav_profile :
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        break;
                    case R.id.nav_watchList:
                        startActivity(new Intent(getApplicationContext(),WatchListActivity.class));
                        break;
                }
                return true;
            }
        });
    }
}
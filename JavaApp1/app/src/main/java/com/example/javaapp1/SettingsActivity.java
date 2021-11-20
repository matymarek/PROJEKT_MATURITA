package com.example.javaapp1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.example.javaapp1.databinding.ActivitySettingsBinding;
import com.example.javaapp1.db.AppDatabase;
import com.example.javaapp1.db.RouteDAO;
import com.google.android.material.navigation.NavigationView;

public class SettingsActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    RouteDAO routeDAO;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.javaapp1.databinding.ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setNavigationViewListener();
        initDB();
    }

    public void initDB() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "Routes").allowMainThreadQueries().build();
        routeDAO = db.routeDAO();
    }

    public void setNavigationViewListener() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navbar_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("item", "" + item.getItemId());
        if (item.getItemId() == R.id.nav_track) {
            Intent intent = new Intent(SettingsActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_my_saves) {
            Intent intent = new Intent(SettingsActivity.this, SavesActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return false;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
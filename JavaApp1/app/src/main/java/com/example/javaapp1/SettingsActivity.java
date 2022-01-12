package com.example.javaapp1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.javaapp1.MessageBoxes.MessageBoxNewColor;
import com.example.javaapp1.databinding.ActivitySettingsBinding;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class SettingsActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Button btn;
    Switch swtch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.javaapp1.databinding.ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setNavigationViewListener();
        init();
    }

    public void init(){
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(view->changeColor());
        swtch = findViewById(R.id.swtch);
        swtch.setChecked(true);
        swtch.setOnClickListener(view->autosave());
        swtch.setEnabled(false); //autosave nefunguje
    }

    public void autosave(){
        if(swtch.isChecked()){
            Toast.makeText(getApplicationContext(), "Automatické ukládání vypnuto", Toast.LENGTH_LONG).show();
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write("\nautosaveoff", 10, 50);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Automatické ukládání zapnuto", Toast.LENGTH_LONG).show();
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write("\nautosaveon", 10, 50);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    public void changeColor(){
        MessageBoxNewColor box = new MessageBoxNewColor();
        box.show(getFragmentManager(), "Změna barvy");
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
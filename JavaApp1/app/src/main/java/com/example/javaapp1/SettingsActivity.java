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
import com.example.javaapp1.MessageBoxes.MessageBoxNewMapType;
import com.example.javaapp1.databinding.ActivitySettingsBinding;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SettingsActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Button btn;
    Button btn2;
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
        btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(view -> changeMapType());
        swtch = findViewById(R.id.swtch);
        initSwtch();
        swtch.setOnClickListener(view->autosave());
    }

    public void initSwtch(){
        try{
            InputStream inputStream = getApplicationContext().openFileInput("config.txt");
            String json;
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }
                inputStream.close();
                json = stringBuilder.toString();
            }
            else{
                json = new JSONObject("{\"barva\":\"červená\",\"autosave\":\"1\",\"mapType\":\"turistická\"}").toString();
            }
            JSONObject reader = new JSONObject(json);
            if(reader.getInt("autosave") == 1) { swtch.setChecked(true); }
            else { swtch.setChecked(false); }
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void autosave() {
        try {
            InputStream inputStream = getApplicationContext().openFileInput("config.txt");
            String json;
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }
                inputStream.close();
                json = stringBuilder.toString();
            }
            else{
                json = new JSONObject("{\"barva\":\"červená\",\"autosave\":\"1\",\"mapType\":\"turistická\"}").toString();
            }
            JSONObject reader = new JSONObject(json);
            if(reader.getInt("autosave") == 1){
                swtch.setChecked(false);
                reader.put("autosave", 0);
                Toast.makeText(getApplicationContext(), "Automatické ukládání vypnuto", Toast.LENGTH_LONG).show();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(reader.toString());
                outputStreamWriter.close();
            }
            else {
                reader.put("autosave", 1);
                swtch.setChecked(true);
                Toast.makeText(getApplicationContext(), "Automatické ukládání zapnuto", Toast.LENGTH_LONG).show();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(reader.toString());
                outputStreamWriter.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeColor(){
        MessageBoxNewColor box = new MessageBoxNewColor();
        box.show(getSupportFragmentManager(), "Změna barvy");
    }

    public void changeMapType(){
        MessageBoxNewMapType box = new MessageBoxNewMapType();
        box.show(getSupportFragmentManager(), "Autosave change");
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
        if (item.getItemId() == R.id.nav_track) {
            Intent intent = new Intent(SettingsActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_my_saves) {
            Intent intent = new Intent(SettingsActivity.this, SavesActivity.class);
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
package com.example.javaapp1;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ScrollCaptureCallback;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.example.javaapp1.databinding.ActivitySavesBinding;
import com.example.javaapp1.db.AppDatabase;
import com.example.javaapp1.db.Route;
import com.example.javaapp1.db.RouteDAO;
import com.google.android.material.navigation.NavigationView;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SavesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RouteDAO routeDAO;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    LinearLayout dataContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.javaapp1.databinding.ActivitySavesBinding binding = ActivitySavesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setNavigationViewListener();
        initDB();
        listDB();
    }

    public void initDB() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "Routes").allowMainThreadQueries().build();
        routeDAO = db.routeDAO();
        dataContainer = findViewById(R.id.data_container);
    }

    public void listDB() {
        List<Route> routes = routeDAO.getAll();
        for(int i = 0; i < routes.size(); i++) {
            TableRow row = new TableRow(SavesActivity.this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    WRAP_CONTENT);
            row.setWeightSum(5f);
            int finalI = i;
            row.setOnClickListener(view -> showMap(finalI));
            params.topMargin = 25;
            row.setLayoutParams(params);
            row.setOrientation(TableRow.VERTICAL);

            params = new TableRow.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1.75f);
            TextView order = new TextView(SavesActivity.this);
            order.setText(new Integer(i+1).toString());
            order.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            order.setTextSize(15);
            order.setLayoutParams(params);

            params = new TableRow.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1.15f);
            TextView date = new TextView(SavesActivity.this);
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            date.setText(df.format(routes.get(i).date));
            date.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            date.setTextSize(15);
            date.setLayoutParams(params);

            params = new TableRow.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1.05f);
            TextView length = new TextView(SavesActivity.this);
            if(routes.get(i).length > 1000) {
                double kmLength = routes.get(i).length/1000;
                length.setText(kmLength+"km");
            }
            else{
                length.setText(routes.get(i).length+"m");
            }
            length.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            length.setTextSize(15);
            length.setLayoutParams(params);

            params = new TableRow.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 0.95f);
            TextView time = new TextView(SavesActivity.this);
            df = new SimpleDateFormat("HH:mm:ss,s");
            time.setText(df.format(routes.get(i).timeLength));
            time.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            time.setTextSize(15);
            time.setLayoutParams(params);

            row.addView(order);
            row.addView(date);
            row.addView(length);
            row.addView(time);
            dataContainer.addView(row);
        }
    }

    private void showMap(int i) {
        Intent intent = new Intent(SavesActivity.this, MapsActivity.class);
        intent.putExtra("id", i);
        startActivity(intent);
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
            Intent intent = new Intent(SavesActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_my_saves) {
            Intent intent = new Intent(SavesActivity.this, SavesActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(SavesActivity.this, SettingsActivity.class);
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
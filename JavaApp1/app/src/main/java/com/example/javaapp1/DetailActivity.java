package com.example.javaapp1;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.javaapp1.MessageBoxes.MessageBoxDelete;
import com.example.javaapp1.db.AppDatabase;
import com.example.javaapp1.db.Route;
import com.example.javaapp1.db.RouteDAO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    Button ButtonBack;
    Button ButtonDelete;
    GoogleMap mMap;
    FusedLocationProviderClient mLocationClient;
    RouteDAO routeDAO;
    List<Route> dbRoute;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.javaapp1.databinding.ActivityDetailBinding binding = com.example.javaapp1.databinding.ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setNavigationViewListener();
        initDB();
        initmap();
        initBttn();
    }



    public void showDetail(int id) {
        Route route = dbRoute.get(id);
        ArrayList<LatLng> routeList = new ArrayList<>();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.latPoints.get(route.latPoints.size()/2), route.longPoints.get(route.longPoints.size()/2)), 18));
        for(int i = 0; i < route.latPoints.size(); i++){
            routeList.add(new LatLng(route.latPoints.get(i), route.longPoints.get(i)));
        }
        TextView date = findViewById(R.id.datePopup);
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        date.setText("Datum: "+df.format(route.date));

        TextView length = findViewById(R.id.lengthPopup);
        if(route.length > 1000) { double kmLength = route.length/1000; length.setText("Vzdálenost: "+kmLength+"km"); }
        else{ length.setText("Vzdálenost: "+route.length+"m"); }

        TextView time = findViewById(R.id.timePopup);
        if (route.timeLength.getTime() > 3600000) { df = new SimpleDateFormat("HH:mm:ss,s"); }
        else { df = new SimpleDateFormat("mm:ss,s"); }
        time.setText("Čas: "+df.format(route.timeLength));

        TextView elevation = findViewById(R.id.elevationPopup);
        if(route.elevation > 1000) { double kmElevation = route.elevation/1000; elevation.setText("Převýšení: "+kmElevation+"km"); }
        else{ elevation.setText("Převýšení: "+route.elevation+"m"); }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routeList)
                .color(Color.RED)
                .startCap(new RoundCap())
                .endCap(new RoundCap());
        Polyline polyline = mMap.addPolyline(polylineOptions);
    }

    public void initmap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.popupMap);
        mapFragment.getMapAsync(this);
    }

    public void initDB() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "Routes").allowMainThreadQueries().build();
        routeDAO = db.routeDAO();
        dbRoute = routeDAO.getAll();
    }

    public void initBttn(){
        ButtonBack = findViewById(R.id.ButtonBack);
        ButtonDelete = findViewById(R.id.ButtonDelete);
        ButtonBack.setOnClickListener(view -> goBack());
        ButtonDelete.setOnClickListener(view -> delete());
        id = getIntent().getExtras().getInt("id");
    }

    private void goBack() {
        Intent intent = new Intent(DetailActivity.this, SavesActivity.class);
        startActivity(intent);
    }

    private void delete() {
        MessageBoxDelete box = new MessageBoxDelete(id, routeDAO, dbRoute);
        box.show(getSupportFragmentManager(), "Delete record");
    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        checkPermission();
        mMap = googleMap;
        mLocationClient = new FusedLocationProviderClient(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        showDetail(id);
    }

    public void checkPermission() {
        if (getApplicationContext().getApplicationInfo().targetSdkVersion >= 29) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                requestPermissions(permissions, PERMISSION_GRANTED);
                while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                requestPermissions(permissions, PERMISSION_GRANTED);
                while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
            Intent intent = new Intent(DetailActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_my_saves) {
            Intent intent = new Intent(DetailActivity.this, SavesActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(DetailActivity.this, SettingsActivity.class);
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
package com.example.javaapp1;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.example.javaapp1.databinding.ActivityMapsBinding;
import com.example.javaapp1.db.AppDatabase;
import com.example.javaapp1.db.Route;
import com.example.javaapp1.db.RouteDAO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.navigation.NavigationView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener {

    GoogleMap mMap;
    ArrayList<LatLng> route;
    ArrayList<Double> latRoute;
    ArrayList<Double> longRoute;
    Button Button1;
    Button Button2;
    Button Button3;
    FusedLocationProviderClient mLocationClient;
    boolean tracking;
    double longitude;
    double latitude;
    float[] results;
    int count;
    Timer timer;
    TimeZone tz;

    double length;
    Long timeLength;
    RouteDAO routeDAO;
    List<Route> dbRoute;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    PowerManager.WakeLock wl;
    PowerManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setNavigationViewListener();
        initDB();
        initmap();
        initButtons();
    }

    public void startTracking() {
        if (!tracking) {
            tracking = true;
            length = 0;
            tz = TimeZone.getDefault();
            timeLength = System.currentTimeMillis() + tz.getOffset(System.currentTimeMillis());
            Log.i("TAG", "" + tz.getOffset(System.currentTimeMillis()));
            gotoLocation(getCurrentLocation());
            route = new ArrayList<>();
            route.add(getCurrentLocation());
            latRoute = new ArrayList<>();
            longRoute = new ArrayList<>();
            latRoute.add(getCurrentLocation().latitude);
            longRoute.add(getCurrentLocation().longitude);
            timer = new Timer();
            count = 0;
            initPM();
            wl.acquire();
            Toast.makeText(this, "Trasování zapnuto", Toast.LENGTH_LONG).show();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        LatLng current = getCurrentLocation();
                        if(count == 5) {gotoLocation(current); count = 0;}
                        route.add(current);
                        latRoute.add(current.latitude);
                        longRoute.add(current.longitude);
                        results = new float[3];
                        Location.distanceBetween(current.latitude, current.longitude, route.get(route.size() - 2).latitude, route.get(route.size() - 2).longitude, results);
                        length += results[0];
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(route)
                                .color(Color.RED)
                                .startCap(new RoundCap())
                                .endCap(new RoundCap());
                        Polyline polyline = mMap.addPolyline(polylineOptions);
                    });
                }
            }, 500, 500);
        }
    }

    public void stopTracking() {
        if (tracking) {
            timer.cancel();
            Toast.makeText(this, "Trasování ukončeno", Toast.LENGTH_LONG).show();
            tracking = false;
            dbRoute = routeDAO.getAll();
            Route route = new Route();
            route.id = dbRoute.size() + 1;
            route.date = new Date(System.currentTimeMillis());
            route.length = Math.floor(length * 100) / 100;
            route.timeLength = new Date(TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis() - timeLength));
            route.latPoints = latRoute;
            route.longPoints = longRoute;
            routeDAO.insertRoute(route);
            wl.release();
            Toast.makeText(this, "Trasa uložena", Toast.LENGTH_LONG).show();
        }
    }

    public LatLng getCurrentLocation() {
        checkPermission();
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
        return new LatLng(latitude, longitude);
    }

    public void gotoLocation(LatLng current) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 18);
        mMap.animateCamera(cameraUpdate, 2000, null);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mLocationClient = new FusedLocationProviderClient(this);
        checkPermission();
        mMap.setMyLocationEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt("id");
            Route route = dbRoute.get(id);
            ArrayList<LatLng> routeList = new ArrayList<>();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.latPoints.get(route.latPoints.size()/2), route.longPoints.get(route.longPoints.size()/2)), 18));
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            for(int i = 0; i < route.latPoints.size(); i++){
                routeList.add(new LatLng(route.latPoints.get(i), route.longPoints.get(i)));
            }
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routeList)
                    .color(Color.RED)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap());
            Polyline polyline = mMap.addPolyline(polylineOptions);
        }
        else {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50, 15.5), 6));
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        LatLng first = getCurrentLocation();
    }

    public void initmap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void initButtons(){
        Button1 = findViewById(R.id.Button1);
        Button2 = findViewById(R.id.Button2);
        Button3 = findViewById(R.id.Button3);
        Button1.setOnClickListener(view -> mMap.clear());
        Button2.setOnClickListener(view -> startTracking());
        Button3.setOnClickListener(view -> stopTracking());
        tracking = false;
    }

    public void initDB() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "Routes").allowMainThreadQueries().build();
        routeDAO = db.routeDAO();
        dbRoute = routeDAO.getAll();
    }

    public void initPM() {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:MyTag");
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
        Log.i("item", "" + item.getItemId());
        if (item.getItemId() == R.id.nav_track) {
            Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_my_saves) {
            Intent intent = new Intent(MapsActivity.this, SavesActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
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

    public void onConnected(@Nullable Bundle bundle) {

    }

    public void onConnectionSuspended(int i) {

    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
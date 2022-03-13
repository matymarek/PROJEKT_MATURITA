package com.example.javaapp1;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.example.javaapp1.MessageBoxes.MessageBoxBackgroundLocationAPI30;
import com.example.javaapp1.MessageBoxes.MessageBoxNoLocation;
import com.example.javaapp1.MessageBoxes.MessageBoxSaveRoute;
import com.example.javaapp1.databinding.ActivityMapsBinding;
import com.example.javaapp1.db.AppDatabase;
import com.example.javaapp1.db.Route;
import com.example.javaapp1.db.RouteDAO;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    GoogleMap mMap;
    ArrayList<LatLng> route;
    ArrayList<Double> latRoute;
    ArrayList<Double> longRoute;
    Button Button1;
    Button Button2;
    Button Button3;
    FusedLocationProviderClient mLocationClient;
    boolean tracking;
    boolean network;
    double netSpeed;
    double longitude;
    double latitude;
    double elevation;
    double elevationMin;
    double elevationMax;
    float[] results;
    int count;
    int color;
    int autosave;
    int mapType;
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
        readFromFile(getApplicationContext());
        initNetwork(getApplicationContext());
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
            gotoLocation(getCurrentLocation());
            route = new ArrayList<>();
            route.add(getCurrentLocation());
            elevationMax = elevationMin = requestElevation(route.get(0).latitude, route.get(0).longitude);
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
                        if(count == 10) {
                            gotoLocation(current);
                            elevation = requestElevation(current.latitude, current.longitude);
                            if(elevation >elevationMax) elevationMax = elevation;
                            else if(elevation < elevationMin) elevationMin = elevation;
                            count = 0;
                        }
                        count++;
                        route.add(current);
                        results = new float[3];
                        Location.distanceBetween(current.latitude, current.longitude, route.get(route.size() - 2).latitude, route.get(route.size() - 2).longitude, results);
                        length += results[0];
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(route)
                                .color(color)
                                .startCap(new RoundCap())
                                .endCap(new RoundCap());
                        mMap.clear();
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
            latRoute = new ArrayList<>();
            longRoute = new ArrayList<>();
            for(int i = 0; i < route.size(); i++){
                latRoute.add(route.get(i).latitude);
                longRoute.add(route.get(i).longitude);
            }
            tracking = false;
            dbRoute = routeDAO.getAll();
            Route route = new Route();
            route.id = dbRoute.size() + 1;
            route.date = new Date(System.currentTimeMillis());
            route.length = Math.round(length*100)/100.00;
            route.timeLength = new Date(TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis() - timeLength));
            route.elevation = Math.round((elevationMax - elevationMin) * 100)/100.00;
            route.latPoints = latRoute;
            route.longPoints = longRoute;
            if(autosave == 1) {
                routeDAO.insertRoute(route);
                Toast.makeText(this, "Trasa uložena", Toast.LENGTH_LONG).show();
            }
            else {
                MessageBoxSaveRoute box = new MessageBoxSaveRoute(routeDAO, route);
                box.show(getSupportFragmentManager(), "Save Route");
            }
            wl.release();
        }
    }

    public LatLng getCurrentLocation() {
        checkPermission();
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                if(location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                else {
                    MessageBoxNoLocation box = new MessageBoxNoLocation();
                    box.show(getSupportFragmentManager(), "Location is null");
                    Button2.setOnClickListener(null);
                    Button3.setOnClickListener(null);
                }
            }
        });
        return new LatLng(latitude, longitude);
    }

    public double requestElevation(double latitude, double longitude){
        final double[] res = {0.0};
        final boolean[] ready = {false};
        if (network && netSpeed > 1000) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/elevation/json?locations=" + latitude + "%2C" + longitude + "&key=" + getString(R.string.Api_key))
                    .method("GET", null)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    ready[0] = true;
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String json = response.body().string();
                    try {
                        JSONObject reader = new JSONObject(json);
                        JSONArray results = reader.getJSONArray("results");
                        JSONObject result = results.getJSONObject(0);
                        res[0] = result.getDouble("elevation");
                        ready[0] = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ready[0] = true;
                    }
                }
            });
            while (!ready[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return res[0];
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50, 15.5), 6));
        mMap.setMapType(mapType);
        LatLng first = getCurrentLocation();
    }

    private void readFromFile(Context context) {
        try {
            InputStream inputStream = null;
            try{
                inputStream = context.openFileInput("config.txt");
            }
            catch (FileNotFoundException e){
                String newJson = new JSONObject("{\"barva\":\"červená\",\"autosave\":\"1\",\"mapType\":\"turistická\"}").toString();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(newJson);
                outputStreamWriter.close();
            } finally {
                if(inputStream == null) {inputStream = context.openFileInput("config.txt");}
            }
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
            else {
                json = new JSONObject("{\"barva\":\"červená\",\"autosave\":\"1\",\"mapType\":\"turistická\"}").toString();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(json);
                outputStreamWriter.close();
            }
            JSONObject reader = new JSONObject(json);
            switch (reader.getString("barva")) {
                case "červená": {
                    color = Color.RED;
                    break;
                }
                case "zelená": {
                    color = Color.GREEN;
                    break;
                }
                case "modrá": {
                    color = Color.BLUE;
                    break;
                }
                case "černá": {
                    color = Color.BLACK;
                    break;
                }
                default: { color = Color.RED; }
            }
            switch (reader.getString("mapType")) {
                case "satelitní": {
                    mapType = GoogleMap.MAP_TYPE_SATELLITE;
                    break;
                }
                case "hybridní": {
                    mapType = GoogleMap.MAP_TYPE_HYBRID;
                    break;
                }
                case "turistická": {
                    mapType = GoogleMap.MAP_TYPE_TERRAIN;
                    break;
                }
                case "normální": {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                    break;
                }
                default: { mapType = GoogleMap.MAP_TYPE_TERRAIN; }
            }
            autosave = reader.getInt("autosave");
        }
        catch (FileNotFoundException e) { Log.e("login activity", "File not found: " + e.toString()); color = Color.RED;}
        catch (IOException e) { Log.e("login activity", "Can not read file: " + e.toString()); }
        catch (JSONException e) { e.printStackTrace(); }
    }

    public void initNetwork(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        network = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        netSpeed = nc.getLinkDownstreamBandwidthKbps();
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
        if (Build.VERSION.SDK_INT >= 30) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_GRANTED);
                while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED ){
                MessageBoxBackgroundLocationAPI30 box = new MessageBoxBackgroundLocationAPI30();
                box.show(getSupportFragmentManager(), "Request background location");
            }
        }
        else if (Build.VERSION.SDK_INT == 29) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_GRANTED);
                while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_GRANTED);
                while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    try {
                        Thread.sleep(200);
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
        if (item.getItemId() == R.id.nav_my_saves) {
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
}
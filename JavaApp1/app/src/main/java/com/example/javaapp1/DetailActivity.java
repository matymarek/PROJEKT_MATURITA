package com.example.javaapp1;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    Button ButtonBack;
    Button ButtonDelete;
    Button ButtonExport;
    Bitmap fragmentBmp;
    GoogleMap mMap;
    FusedLocationProviderClient mLocationClient;
    RouteDAO routeDAO;
    List<Route> dbRoute;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    int mapType;
    int id;
    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.javaapp1.databinding.ActivityDetailBinding binding = com.example.javaapp1.databinding.ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setNavigationViewListener();
        readFromFile(getApplicationContext());
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
                .color(color)
                .startCap(new RoundCap())
                .endCap(new RoundCap());
        Polyline polyline = mMap.addPolyline(polylineOptions);
    }

    public void initmap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.popupMap);
        mapFragment.getMapAsync(this);
    }

    private void readFromFile(Context context) {
        try {
            InputStream inputStream = context.openFileInput("config.txt");
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
        }
        catch (FileNotFoundException e) { Log.e("login activity", "File not found: " + e.toString()); color = Color.RED;}
        catch (IOException e) { Log.e("login activity", "Can not read file: " + e.toString()); }
        catch (JSONException e) { e.printStackTrace(); }
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
        ButtonExport = findViewById(R.id.ButtonShare);
        ButtonBack.setOnClickListener(view -> goBack());
        ButtonDelete.setOnClickListener(view -> delete());
        ButtonExport.setOnClickListener(view -> screenshot());
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

    private void screenshot() {
        GoogleMap.SnapshotReadyCallback callback = bitmap -> {
            fragmentBmp = bitmap;
            ButtonBack.setVisibility(View.INVISIBLE);
            ButtonExport.setVisibility(View.INVISIBLE);
            ButtonDelete.setVisibility(View.INVISIBLE);

            LinearLayout layout = (LinearLayout) DetailActivity.this.findViewById(R.id.forScreen);
            Bitmap rest = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(rest);
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:{
                    canvas.drawColor(Color.BLACK);
                    break;
                }
                case Configuration.UI_MODE_NIGHT_NO:{
                    canvas.drawColor(Color.WHITE);
                    break;
                }
            }
            layout.draw(canvas);

            ButtonBack.setVisibility(View.VISIBLE);
            ButtonExport.setVisibility(View.VISIBLE);
            ButtonDelete.setVisibility(View.VISIBLE);

            Bitmap finalBmp = Bitmap.createBitmap(rest.getWidth(), rest.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvasFinal = new Canvas(finalBmp);
            canvasFinal.drawBitmap(rest, new Matrix(), null);
            float width = Math.round(rest.getWidth()/15.5);
            float height = Math.round(rest.getHeight()/6.8);
            canvasFinal.drawBitmap(fragmentBmp, width, height, null);
            rest.recycle();
            fragmentBmp.recycle();

            Date now = new Date(System.currentTimeMillis());
            SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentResolver resolver = DetailActivity.this.getApplicationContext().getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "route_" + df.format(now) + ".png");
                    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    contentValues.put(MediaStore.Images.Media.DATE_TAKEN, now.toString());
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + DetailActivity.this.getString(R.string.app_name));
                    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    if (uri != null) {
                        DetailActivity.this.saveImageToStream(finalBmp, resolver.openOutputStream(uri));
                        resolver.update(uri, contentValues, null, null);
                    }
                } else {
                    File dir = new File((DetailActivity.this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DetailActivity.this.getString(R.string.app_name)));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir.getAbsolutePath() + File.separator + "route_" + df.format(now) + ".png");
                    DetailActivity.this.saveImageToStream(finalBmp, new FileOutputStream(file));
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                    DetailActivity.this.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(DetailActivity.this, "Obrázek trasy uložen do galerie", Toast.LENGTH_LONG).show();
        };
        mMap.snapshot(callback);
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        checkPermission();
        mMap = googleMap;
        mLocationClient = new FusedLocationProviderClient(this);
        mMap.setMapType(mapType);
        showDetail(id);
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 29) {
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
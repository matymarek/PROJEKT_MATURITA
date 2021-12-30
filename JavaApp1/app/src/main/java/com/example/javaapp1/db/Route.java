package com.example.javaapp1.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;
import java.util.ArrayList;

@Entity
public class Route {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "track_date")
    public Date date;
    @ColumnInfo(name = "track_length")
    public double length;
    @ColumnInfo(name = "track_time")
    public Date timeLength;
    @ColumnInfo(name = "track_elevation")
    public double elevation;
    @ColumnInfo(name = "track_points_lat")
    public ArrayList<Double> latPoints;
    @ColumnInfo(name = "track_points_long")
    public ArrayList<Double> longPoints;
}


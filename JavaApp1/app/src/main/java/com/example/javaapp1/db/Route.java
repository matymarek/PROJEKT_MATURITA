package com.example.javaapp1.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

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
}


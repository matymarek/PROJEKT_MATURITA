package com.example.javaapp1.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.sql.Date;
import java.util.List;

@Dao
public interface RouteDAO {
    @Query("SELECT * FROM route")
    List<Route> getAll();

    @Insert
    void insertRoute(Route route);

    @Delete
    void delete(Route route);
}


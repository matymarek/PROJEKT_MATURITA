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

    @Query("SELECT * FROM route WHERE id IN (:routeId)")
    List<Route> loadById(int routeId);

    @Query("SELECT * FROM route WHERE track_date LIKE :date AND " +
            "track_length LIKE :length AND track_time LIKE :timeLength LIMIT 10")
    Route findByData(Date date, double length, Date timeLength);

    @Insert
    void insertAll(Route... route);

    @Insert
    void insertRoute(Route route);

    @Delete
    void delete(Route route);
}


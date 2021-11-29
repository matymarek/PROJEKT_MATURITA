package com.example.javaapp1.db;

import androidx.room.TypeConverter;

import java.sql.Date;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static Date toDate(Long value) { return value == null ? null : new Date(value); }

    @TypeConverter
    public static Long toLong(Date date) { return date == null ? null : date.getTime(); }

    @TypeConverter
    public static ArrayList<Double> toArrayList(String points) {
        ArrayList<Double> list = new ArrayList<>();
        String[] array = points.split("/");
        for (int i = 1; i < array.length; i++) {
            list.add(Double.parseDouble(array[i]));
        }
        return list;
    }
    @TypeConverter
    public static String toString(ArrayList<Double> list) {
        String points = "";
        for (int i = 0; i < list.size(); i++) {
            points += "/" + list.get(i);
        }
        return points;
    }
}

package com.sjtu.demoapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sjtu.demoapp.Location;

import java.util.HashSet;
import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    long insertLocation(Location location);

    @Query("SELECT MAX(id) FROM location WHERE floor = :floor")
    Long count(int floor);

    @Query("SELECT MAX(id) FROM location WHERE floor = 0")
    Long count();

    @Query("SELECT e.id FROM location e WHERE e.floor = 0 AND e.x = :x AND e.y = :y")
    Long queryLocId(float x, float y);

    @Query("SELECT e.id FROM location e WHERE e.floor = :floor AND e.x = :x AND e.y = :y")
    Long queryLocId(int floor, float x, float y);

    @Query("SELECT * FROM location AS e WHERE e.id = :id")
    Location queryLocation(long id);

    @Query("SELECT * FROM location")
    List<Location> loadAll();

    @Query("SELECT * FROM location WHERE floor = :floor")
    List<Location> loadAll(int floor);

}

package com.sjtu.demoapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sjtu.demoapp.StatEntity;

import java.util.List;

@Dao
public interface StatDao {
    @Insert
    void insert(StatEntity entity);

    @Query("SELECT * FROM stat WHERE stat.loc_id = :loc_id")
    StatEntity getStat(long loc_id);

    @Query("SELECT * FROM stat WHERE stat.loc_id = (SELECT l.id FROM location l WHERE l.floor = 0 AND l.x = :x AND l.y = :y)")
    List<StatEntity> getStat(float x, float y);

    @Query("SELECT * FROM stat WHERE stat.loc_id = (SELECT l.id FROM location l WHERE l.floor = :floor AND l.x = :x AND l.y = :y)")
    List<StatEntity> getStat(int floor, float x, float y);

    @Query("SELECT stat.* FROM stat INNER JOIN location l ON (l.id = stat.loc_id AND l.floor = :floor)")
    List<StatEntity> getAllStatByFloor(int floor);

}

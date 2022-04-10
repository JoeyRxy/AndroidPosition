package com.sjtu.demoapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.sjtu.demoapp.InfoStruct;

import java.util.List;

@Dao
public interface InfoDao {

    @Query("SELECT * FROM infos")
    List<InfoStruct> loadAll();

    @Query("SELECT * FROM infos WHERE id IN (:infoids)")
    List<InfoStruct> loadAllByInfoId(long... infoids);

    @Query("SELECT * FROM infos WHERE loc_id = (SELECT id FROM location WHERE floor = :floor)")
    List<InfoStruct> loadAllByFloor(int floor);

    @Query("SELECT * FROM infos WHERE loc_id = :loc_id")
    List<InfoStruct> loadByLocId(long loc_id);

    @Query("SELECT * FROM infos WHERE loc_id = (SELECT id FROM location WHERE floor = :floor AND x = :x AND y = :y)")
    List<InfoStruct> loadByCoord(int floor, float x, float y);

    @Query("SELECT * FROM infos WHERE loc_id = (SELECT id FROM location WHERE floor = 0 AND x = :x AND y = :y)")
    List<InfoStruct> loadByCoord(float x, float y);

    @Insert
    void insertAll(InfoStruct... infoStructs);

    @Insert
    void insert(InfoStruct infoStruct);

    @Delete
    void delete(InfoStruct infoStruct);
}

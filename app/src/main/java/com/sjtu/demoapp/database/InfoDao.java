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
    List<InfoStruct> loadAllByInfoId(int... infoids);

    @Query("SELECT * FROM infos WHERE floor BETWEEN (:currentFloor - 1) AND (:currentFloor + 1)")
    List<InfoStruct> loadData(int currentFloor);

    @Insert
    void insertAll(InfoStruct... infoStructs);

    @Insert
    void insert(InfoStruct infoStruct);

    @Delete
    void delete(InfoStruct infoStruct);
}

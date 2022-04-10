package com.sjtu.demoapp;

import androidx.annotation.ColorInt;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.sjtu.demoapp.database.MapConverter;

import java.util.HashMap;

@Entity(tableName = "stat")
public class StatEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "loc_id")
    long locId;

    public long getLocId() {
        return locId;
    }

    public void setLocId(long locId) {
        this.locId = locId;
    }

    // pci -> stat
    @ColumnInfo(name = "stat_map")
    HashMap<Integer, Stat> statMap;

    public StatEntity(long locId, HashMap<Integer, Stat> statMap) {
        this.locId = locId;
        this.statMap = statMap;
    }

    public HashMap<Integer, Stat> getStatMap() {
        return statMap;
    }

    public void setStatMap(HashMap<Integer, Stat> statMap) {
        this.statMap = statMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}

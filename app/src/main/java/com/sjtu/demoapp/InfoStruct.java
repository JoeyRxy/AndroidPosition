package com.sjtu.demoapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Entity(tableName = "infos")
public class InfoStruct implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "loc_id")
    private long locId;

    public long getLocId() {
        return locId;
    }

    public void setLocId(long locId) {
        this.locId = locId;
    }

    //如果修改map的格式别忘了改数据库的数据格式转换的东西
    HashMap<Integer, Integer> cellSignalStrengthMap;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public HashMap<Integer, Integer> getCellSignalStrengthMap() {
        return cellSignalStrengthMap;
    }

    public void setCellSignalStrengthMap(HashMap<Integer, Integer> cellSignalStrengthMap) {
        this.cellSignalStrengthMap = cellSignalStrengthMap;
    }

    public InfoStruct() {
        cellSignalStrengthMap = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoStruct that = (InfoStruct) o;
        return locId == that.locId && cellSignalStrengthMap.equals(that.cellSignalStrengthMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locId, cellSignalStrengthMap);
    }
}

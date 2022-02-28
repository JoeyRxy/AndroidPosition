package com.sjtu.demoapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@Entity(tableName = "infos")
public class InfoStruct implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    float x = 0;
    float y = 0;
    int floor = 1;

    //如果修改map的格式别忘了改数据库的数据格式转换的东西
    HashMap<Integer, Integer> cellSignalStrengthMap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
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
}

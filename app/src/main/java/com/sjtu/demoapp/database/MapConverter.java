package com.sjtu.demoapp.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sjtu.demoapp.Stat;

import java.lang.reflect.Type;
import java.util.HashMap;

public class MapConverter {
    private static final Gson gson = new Gson();
    static Type resType = new TypeToken<HashMap<Integer, Stat>>(){}.getType();
    static Type listType = new TypeToken<HashMap<Integer, Integer>>(){}.getType();

    @TypeConverter
    public String objectToString(HashMap<Integer, Integer> map) {
        return gson.toJson(map);
    }

    @TypeConverter
    public HashMap<Integer, Integer> stringToObject(String json) {
        return gson.fromJson(json, listType);
    }

    @TypeConverter
    public String objectToString2(HashMap<Integer, Stat> map) {return gson.toJson(map);}

    @TypeConverter
    public HashMap<Integer, Stat> stringToObject2(String json) {
        return gson.fromJson(json, resType);
    }

}

package com.sjtu.demoapp.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MapConverter {
    private static final Gson gson = new Gson();
    @TypeConverter
    public String objectToString(HashMap<Integer, Integer> map) {
        return gson.toJson(map);
    }

    @TypeConverter
    public HashMap<Integer, Integer> stringToObject(String json) {
        Type listType = new TypeToken<HashMap<Integer, Integer>>(){}.getType();
        return gson.fromJson(json, listType);
    }

}

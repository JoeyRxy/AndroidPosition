package com.sjtu.demoapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.sjtu.demoapp.InfoStruct;
import com.sjtu.demoapp.Location;
import com.sjtu.demoapp.MyApplication;
import com.sjtu.demoapp.StatEntity;

@Database(entities = {InfoStruct.class, Location.class, StatEntity.class}, version = 1,exportSchema = false)
@TypeConverters({MapConverter.class})
public abstract class InfoDatabase extends RoomDatabase {
    public abstract InfoDao infoDao();
    public abstract LocationDao locationDao();
    public abstract StatDao statDao();
    private static InfoDatabase INSTANCE;


    public static InfoDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (InfoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(MyApplication.context, InfoDatabase.class, "info.db")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}

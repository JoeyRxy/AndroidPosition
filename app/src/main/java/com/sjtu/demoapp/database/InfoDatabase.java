package com.sjtu.demoapp.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.sjtu.demoapp.InfoStruct;
import com.sjtu.demoapp.MyApplication;

@Database(entities = {InfoStruct.class}, version = 1,exportSchema = false)
@TypeConverters({MapConverter.class})
public abstract class InfoDatabase extends RoomDatabase {
    public abstract InfoDao infoDao();
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

package com.astutusdesigns.habitood.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.astutusdesigns.habitood.datamodels.RPinpoint;
import com.astutusdesigns.habitood.datamodels.RTeam;
import com.astutusdesigns.habitood.datamodels.RUser;

/**
 * This singleton class will provide a database object to perform queries
 * against.
 * Created by TMiller on 1/12/2018.
 */

@Database(entities = {RTeam.class, RPinpoint.class, RUser.class}, version = 5, exportSchema = false)
@TypeConverters(RoomTypeConverters.class)
public abstract class HabitoodDatabase extends RoomDatabase {
    private static HabitoodDatabase INSTANCE;

    public abstract RTeamDao teamDao();
    public abstract RPinpointDao pinpointDao();
    public abstract RUserDao userDao();

    public static HabitoodDatabase getDatabase(Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, HabitoodDatabase.class, "HabitoodDatabase")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}

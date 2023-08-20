package com.astutusdesigns.habitood.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

/**
 * Room orm Type Converters required to store data with non-primitive types.
 * Created by TMiller on 1/12/2018.
 */

public class RoomTypeConverters {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    @TypeConverter
    public String listToString(List<String> stringList) {
        return new Gson().toJson(stringList);
    }

    @TypeConverter
    public List<String> listFromString(String value) {
        return new Gson().fromJson(value, List.class);
    }
}

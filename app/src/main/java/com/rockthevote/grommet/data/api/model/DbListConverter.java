package com.rockthevote.grommet.data.api.model;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DbListConverter {
    @TypeConverter
    public static ArrayList<String> toList(String data) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(data, listType);
    }

    @TypeConverter
    public static String toString(ArrayList<String> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}

package com.rockthevote.grommet.data.api.model;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ArrayAdapter {

    @ToJson
    public List<String> toJson(ArrayList<String> list) {
        return list;
    }

    @FromJson
    public ArrayList<String> fromJson(List<String> list) {
        return new ArrayList(list);
    }

    @ToJson
    public List<ValidLocation> fromValidLocationToJson(ArrayList<ValidLocation> list) {
        return list;
    }

    @FromJson
    public ArrayList<ValidLocation> fromJsonToValidLocation(List<ValidLocation> list) {
        return new ArrayList(list);
    }

}
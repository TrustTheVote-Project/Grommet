package com.rockthevote.grommet.data;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Mechanical Man on 2019-11-23.
 */
@AutoValue
public abstract class Counties {

    @Json(name = "counties")
    abstract public List<County> counties();

    public static JsonAdapter<Counties> jsonAdapter(Moshi moshi) {
        return new AutoValue_Counties.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_Counties.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder counties(List<County> value);

        abstract Counties build();
    }

    public HashMap<String, List<String>> toHashMap() {

        HashMap<String, List<String>> countyZipMap = new HashMap<>(counties().size());

        for (County county : counties()) {
            countyZipMap.put(county.name(), county.zipCodes());
        }

        return countyZipMap;
    }

}

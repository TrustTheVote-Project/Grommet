package com.rockthevote.grommet.data;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

/**
 * Created by Mechanical Man on 2019-11-23.
 */
@AutoValue
public abstract class County {

    public abstract String name();

    @Json(name = "zip_codes")
    public abstract List<String> zipCodes();

    public static JsonAdapter<County> jsonAdapter(Moshi moshi) {
        return new AutoValue_County.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_County.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder name(String value);

        abstract Builder zipCodes(List<String> value);

        abstract County build();
    }
}

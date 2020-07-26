package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiGeoLocation {
    @Json(name = "lat")
    public abstract double latitude();

    @Json(name = "long")
    public abstract double longitude();

    public static JsonAdapter<ApiGeoLocation> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiGeoLocation.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_ApiGeoLocation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder latitude(double value);

        public abstract Builder longitude(double value);

        public abstract ApiGeoLocation build();
    }

}

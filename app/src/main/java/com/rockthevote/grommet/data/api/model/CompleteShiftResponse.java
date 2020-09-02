package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;

@AutoValue
public abstract class CompleteShiftResponse {

    @Json(name = "errors")
    public abstract ArrayList<String> errors();

    public static JsonAdapter<CompleteShiftResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_CompleteShiftResponse.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return null;
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder errors(ArrayList<String> errors);

        public abstract CompleteShiftResponse build();
    }
}

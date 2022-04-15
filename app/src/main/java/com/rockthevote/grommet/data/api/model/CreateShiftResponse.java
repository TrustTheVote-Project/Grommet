package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class CreateShiftResponse {

    @Json(name = "shift_id")
    public abstract String shiftId();

    public static JsonAdapter<CreateShiftResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_CreateShiftResponse.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return null;
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder shiftId(String shiftId);

        public abstract CreateShiftResponse build();
    }
}
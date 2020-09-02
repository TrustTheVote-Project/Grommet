package com.rockthevote.grommet.data.api.model.validation;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;

@AutoValue
public abstract class IsValidResponse {

    @Json(name = "is_valid")
    public abstract boolean isValid();

    @Json(name = "errors")
    public abstract ArrayList<String> errors();

    public static JsonAdapter<IsValidResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_IsValidResponse.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_IsValidResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder errors(ArrayList<String> errors);

        public abstract Builder isValid(boolean isValid);

        public abstract IsValidResponse build();
    }

}

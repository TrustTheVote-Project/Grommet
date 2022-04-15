package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class UpdateShiftBody {
    @Json(name = "clock_in_datetime")
    public abstract String clockIn();

    @Json(name = "clock_out_datetime")
    public abstract String clockOut();

    @Json(name = "abandoned_registrations")
    public abstract int abandonedCount();

    @Json(name = "completed_registrations")
    public abstract int completedCount();

    public static JsonAdapter<UpdateShiftBody> jsonAdapter(Moshi moshi) {
        return new AutoValue_UpdateShiftBody.MoshiJsonAdapter(moshi);
    }

    public static UpdateShiftBody.Builder builder() {
        return new AutoValue_UpdateShiftBody.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract UpdateShiftBody.Builder clockIn(String clockIn);

        public abstract UpdateShiftBody.Builder clockOut(String clockOut);

        public abstract UpdateShiftBody.Builder abandonedCount(int abandonedCount);

        public abstract UpdateShiftBody.Builder completedCount(int completedCount);

        public abstract UpdateShiftBody build();

    }
}

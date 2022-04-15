package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class CreateShiftBody{

    @Json(name = "partner_id")
    public abstract int partnerId();

    @Json(name = "canvasser_email")
    public abstract String canvasserEmail();

    @Json(name = "device_id")
    public abstract String deviceId();

    @Json(name = "shift_location")
    public abstract long shiftLocation();

    @Json(name = "canvasser_first_name")
    public abstract String canvasserFirstName();

    @Json(name = "canvasser_last_name")
    public abstract String canvasserLastName();

    @Json(name = "canvasser_phone")
    public abstract String canvasserPhone();

    public static JsonAdapter<CreateShiftBody> jsonAdapter(Moshi moshi) {
        return new AutoValue_CreateShiftBody.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_CreateShiftBody.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder partnerId(int partnerId);

        public abstract Builder canvasserEmail(String canvasserEmail);

        public abstract Builder deviceId(String deviceId);

        public abstract Builder shiftLocation(long shiftLocation);

        public abstract Builder canvasserFirstName(String canvasserFirstName);

        public abstract Builder canvasserLastName(String canvasserLastName);

        public abstract Builder canvasserPhone(String canvasserPhone);

        public abstract CreateShiftBody build();

    }
}
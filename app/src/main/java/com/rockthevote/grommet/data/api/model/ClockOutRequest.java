package com.rockthevote.grommet.data.api.model;

/**
 * Created by Mechanical Man, LLC on 7/13/17. Grommet
 */

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import androidx.annotation.Nullable;

@AutoValue
public abstract class ClockOutRequest {

    @Json(name = "source_tracking_id")
    public abstract String sourceTrackingId();

    @Json(name = "partner_tracking_id")
    public abstract String partnerTrackingId();

    @Json(name = "geo_location")
    public abstract ApiGeoLocation geoLocation();

    @Json(name = "open_tracking_id")
    public abstract String openTrackingId();

    @Json(name = "canvasser_name")
    public abstract String canvasserName();

    @Json(name = "clock_out_datetime")
    public abstract String clockOutDatetime();

    @Json(name = "session_timeout_length")
    @Nullable
    public abstract Long sessionTimeoutLength();

    @Json(name = "abandoned_registrations")
    public abstract int abandonedRegistrations();

    @Json(name = "completed_registrations")
    public abstract int completedRegistrations();

    public static JsonAdapter<ClockOutRequest> jsonAdapter(Moshi moshi) {
        return new AutoValue_ClockOutRequest.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_ClockOutRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder sourceTrackingId(String sourceTrackingId);

        public abstract Builder partnerTrackingId(String partnerTrackingId);

        public abstract Builder geoLocation(ApiGeoLocation geoLocation);

        public abstract Builder openTrackingId(String openTrackingId);

        public abstract Builder canvasserName(String canvasserName);

        public abstract Builder clockOutDatetime(String clockOutDatetime);

        public abstract Builder sessionTimeoutLength(Long sessionTimeoutLength);

        public abstract Builder abandonedRegistrations(int abandonedRegistrations);

        public abstract Builder completedRegistrations(int completedRegistrations);

        public abstract ClockOutRequest build();

    }

}

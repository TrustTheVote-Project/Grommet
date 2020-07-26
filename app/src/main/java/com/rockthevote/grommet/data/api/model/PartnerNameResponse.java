package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.Date;

@AutoValue
public abstract class PartnerNameResponse {

    @Json(name = "app_version")
    public abstract int appVersion();

    @Json(name = "is_valid")
    public abstract boolean isValid();

    @Json(name = "partner_name")
    public abstract String partnerName();

    @Json(name = "session_timeout_length")
    public abstract int sessionTimeoutLength();

    @Json(name = "registration_deadline_date")
    public abstract Date registrationDeadlineDate();

    @Json(name = "registration_notification_text")
    public abstract RegistrationNotificationText registrationNotificationText();

    @Json(name = "volunteer_text")
    public abstract PartnerVolunteerText partnerVolunteerText();


    public static JsonAdapter<PartnerNameResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_PartnerNameResponse.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_PartnerNameResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder appVersion(int appVersion);

        public abstract Builder isValid(boolean isValid);

        public abstract Builder partnerName(String partnerName);

        public abstract Builder sessionTimeoutLength(int sessionTimeoutLength);

        public abstract Builder registrationDeadlineDate(Date registrationDeadlineDate);

        public abstract Builder registrationNotificationText(RegistrationNotificationText registrationNotificationText);

        public abstract Builder partnerVolunteerText(PartnerVolunteerText partnerVolunteerText);

        public abstract PartnerNameResponse build();
    }

}

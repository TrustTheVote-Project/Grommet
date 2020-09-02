package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.util.Dates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MockPartnerNames {
    static final PartnerNameResponse SUCCESS = PartnerNameResponse.builder()
            .isValid(true)
            .partnerName("OSET Org")
            .sessionTimeoutLength(2)
            .registrationDeadlineDate(Dates.parseISO8601_ShortDate("2020-09-01"))
            .registrationNotificationText(RegistrationNotificationText.builder()
                    .english("mock english text")
                    .spanish("mock spanish text")
                    .build())
            .partnerVolunteerText(PartnerVolunteerText.builder()
                    .english("mock english volunteer text")
                    .spanish("mock spanish volunteer text")
                    .build())
            .errors(new ArrayList(Arrays.asList("error1", "error2", "error3")))
            .validLocations(new ArrayList(Arrays.asList("PVA", "PVA", "PVA")))
            .build();
}

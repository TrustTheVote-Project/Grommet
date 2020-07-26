package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewData

data class RegistrationData(
    val newRegistrantData: NewRegistrantData? = null,
    val addressData: PersonalInfoData? = null,
    val additionalInfoData: AdditionalInfoData? = null,
    val assistanceData: AssistanceData? = null,
    val reviewData: ReviewData? = null
)


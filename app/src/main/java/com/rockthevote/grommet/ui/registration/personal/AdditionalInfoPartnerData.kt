package com.rockthevote.grommet.ui.registration.personal

import com.rockthevote.grommet.data.api.model.PartnerVolunteerText

/**
 * Created by Mechanical Man on 6/14/20.
 */
data class AdditionalInfoPartnerData(
        val partnerName: String = "",
        val partnerVolunteerText: PartnerVolunteerText
)

package com.rockthevote.grommet.ui.registration.personal

import com.rockthevote.grommet.data.db.model.Party
import com.rockthevote.grommet.data.db.model.PhoneType
import com.rockthevote.grommet.data.db.model.PreferredLanguage
import com.rockthevote.grommet.data.db.model.Race


data class AdditionalInfoData(
    // Mandatory
    val party: Party,
    val emailAddress: String,
    val phoneNumber: String,
    val phoneType: PhoneType,
    val hasChangedPoliticalParty: Boolean,
    val knowsPennDotNumber: Boolean,
    val knowsSsnLastFour: Boolean,
    val partnerEmailOptIn: Boolean,
    val partnerSmsOptIn: Boolean,
    val partnerVolunteerOptIn: Boolean,
    // Optional
    val otherPoliticalParty: String?,
    val pennDotNumber: String?,
    val ssnLastFour: String?,
    val preferredLanguage: PreferredLanguage?,
    val race: Race?
)
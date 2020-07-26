package com.rockthevote.grommet.ui.registration.assistance

import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.registration.name.PersonNameData

data class AssistanceData(
    // Mandatory
    val hasSomeoneAssisted: Boolean,
    // Optional
    val helperName: PersonNameData?,
    val helperAddress: AddressData?,
    val helperPhone: String?,
    val hasConfirmedTerms: Boolean
)
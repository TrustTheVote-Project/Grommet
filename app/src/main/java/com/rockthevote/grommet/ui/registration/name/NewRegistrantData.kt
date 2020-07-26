package com.rockthevote.grommet.ui.registration.name

import java.util.*

data class NewRegistrantData(
    // Required Fields
    val name: PersonNameData,
    val birthday: Date,
    val isUsCitizen: Boolean,
    val is18OrOlderByNextElection: Boolean,
    // Optional
    val hasChangedName: Boolean,
    val previousName: PersonNameData?
)
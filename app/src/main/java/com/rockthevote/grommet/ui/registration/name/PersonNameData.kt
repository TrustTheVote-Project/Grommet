package com.rockthevote.grommet.ui.registration.name

import com.rockthevote.grommet.data.db.model.Prefix
import com.rockthevote.grommet.data.db.model.Suffix

data class PersonNameData(
    // Required Fields
    val firstName: String,
    val title: Prefix,
    val lastName: String,
    // Optional
    val middleName: String?,
    val suffix: Suffix?
)
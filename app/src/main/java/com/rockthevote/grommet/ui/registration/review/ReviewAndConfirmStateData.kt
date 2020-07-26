package com.rockthevote.grommet.ui.registration.review

data class ReviewAndConfirmStateData(
    val name: String,
    val birthday: String,
    val email: String,
    val phone: String,
    val residentialAddress: String,
    val mailingAddress: String?,
    val race: String,
    val party: String
)
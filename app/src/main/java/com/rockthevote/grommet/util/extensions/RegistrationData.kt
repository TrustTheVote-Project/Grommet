package com.rockthevote.grommet.util.extensions

import com.rockthevote.grommet.ui.registration.RegistrationData
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmStateData
import com.rockthevote.grommet.util.Dates

fun RegistrationData.toReviewAndConfirmStateData(): ReviewAndConfirmStateData {
    val registrantName = newRegistrantData?.name?.toFullName() ?: ""

    val birthDate = newRegistrantData?.birthday
    val birthday = birthDate?.let {
        Dates.formatAsISO8601_ShortDate(it)
    } ?: ""

    val residentialAddress = addressData?.homeAddress?.toFriendlyString() ?: ""
    val mailingAddress = addressData?.mailingAddress?.toFriendlyString()

    return ReviewAndConfirmStateData(
        name = registrantName,
        birthday = birthday,
        email = additionalInfoData?.emailAddress ?: "",
        phone = additionalInfoData?.phoneNumber ?: "",
        residentialAddress = residentialAddress,
        mailingAddress = mailingAddress,
        race = additionalInfoData?.race?.toString() ?: "",
        party = additionalInfoData?.party?.toString() ?: ""
    )
}
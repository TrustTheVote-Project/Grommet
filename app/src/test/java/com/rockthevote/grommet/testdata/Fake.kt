package com.rockthevote.grommet.testdata

import com.rockthevote.grommet.data.db.model.*
import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.name.PersonNameData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmStateData
import com.rockthevote.grommet.ui.registration.review.ReviewData
import java.util.*

object Fake {
    val PERSON_NAME_DATA = PersonNameData(
        firstName = "John",
        title = Prefix.MR,
        lastName = "Smith",
        middleName = "Danger",
        suffix = Suffix.SIXTH
    )

    val ADDRESS_DATA = AddressData(
        streetAddress = "123 Fake St",
        city = "Atlanta",
        state = "ON",
        zipCode = "09876",
        county = "Maricopa",
        streetAddressTwo = null,
        unitType = null,
        unitNumber = null
    )

    val EMPTY_REVIEW_AND_CONFIRM_STATE_DATA = ReviewAndConfirmStateData(
        name = "",
        birthday = "",
        email = "",
        phone = "",
        residentialAddress = "",
        mailingAddress = null,
        race = "",
        party = ""
    )

    val NEW_REGISTRANT_DATA = NewRegistrantData(
        name = PERSON_NAME_DATA,
        birthday = Date(),
        isUsCitizen = true,
        is18OrOlderByNextElection = true,
        hasChangedName = false,
        previousName = null
    )

    val PERSONAL_INFO_DATA = PersonalInfoData(
        homeAddress = ADDRESS_DATA,
        isMailingAddressDifferent = false,
        hasPreviousAddress = false,
        mailingAddress = null,
        previousAddress = null
    )

    val ADDITIONAL_INFO_DATA = AdditionalInfoData(
        party = Party.GREEN,
        emailAddress = "test@test.org",
        phoneNumber = "202-555-5555",
        phoneType = PhoneType.MOBILE,
        hasChangedPoliticalParty = false,
        knowsPennDotNumber = true,
        knowsSsnLastFour = true,
        partnerEmailOptIn = false,
        partnerSmsOptIn = false,
        partnerVolunteerOptIn = false,
        otherPoliticalParty = null,
        pennDotNumber = "99999999",
        ssnLastFour = "1234",
        preferredLanguage = PreferredLanguage.CREOLE,
        race = Race.NATIVE_HAWAIIAN
    )

    val ASSISTANCE_DATA = AssistanceData(
        hasSomeoneAssisted = true,
        helperName = PERSON_NAME_DATA,
        helperAddress = ADDRESS_DATA,
        helperPhone = "301-555-5555",
        hasConfirmedTerms = true
    )

    val REVIEW_DATA = ReviewData(
        true,
        ByteArray(0),
        "en"
    )
}
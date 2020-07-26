package com.rockthevote.grommet.ui.registration.personal

import com.rockthevote.grommet.data.db.model.Party
import com.rockthevote.grommet.data.db.model.PhoneType
import com.rockthevote.grommet.data.db.model.PreferredLanguage
import com.rockthevote.grommet.data.db.model.Race
import com.rockthevote.grommet.databinding.FragmentAdditionalInfoBinding
import com.rockthevote.grommet.util.extensions.toEditable

fun FragmentAdditionalInfoBinding.toAdditionalInfoData(): AdditionalInfoData {
    // Values with !! should have been validated before this method is allowed to be called
    //  and should not be null
    val party = spinnerParty.spinnerText!!.let {
        Party.fromString(it)
    }

    val emailAddress = emailEditText.text!!.toString()
    val phoneNumber = phone.text!!.toString()
    val phoneType = spinnerPhoneType.spinnerText!!.let {
        PhoneType.fromString(it)
    }

    val hasChangedPoliticalParty = politicalPartyChangeTextbox.isChecked
    val knowsPennDotNumber = !doesNotHavePennDotCheckbox.isChecked
    val knowsSsnLastFour = !ssnLastFourCheckbox.isChecked
    val isEmailOptInChecked = emailOptIn.isChecked
    val isCanReceiveTextChecked = checkboxCanReceiveText.isChecked
    val isPartnerVolunteerOptInChecked = checkboxPartnerVolunteerOptIn.isChecked
    val preferredLanguage = spinnerPreferredLanguage.spinnerText?.let {
        PreferredLanguage.fromString(it)
    }
    val race = spinnerRace.spinnerText?.let { Race.fromString(it) }

    val otherPoliticalParty = if (party == Party.OTHER_PARTY) otherPartyEditText.text?.toString() else null
    val pennDotNumber = if (knowsPennDotNumber) pennDotEditText.text?.toString() else null
    val ssnLastFour = if (knowsSsnLastFour )ssnLastFourEditText.text?.toString() else null

    return AdditionalInfoData(
        party = party,
        emailAddress = emailAddress,
        phoneNumber = phoneNumber,
        phoneType = phoneType,
        hasChangedPoliticalParty = hasChangedPoliticalParty,
        knowsPennDotNumber = knowsPennDotNumber,
        knowsSsnLastFour = knowsSsnLastFour,
        partnerEmailOptIn = isEmailOptInChecked,
        partnerSmsOptIn = isCanReceiveTextChecked,
        partnerVolunteerOptIn = isPartnerVolunteerOptInChecked,
        otherPoliticalParty = otherPoliticalParty,
        pennDotNumber = pennDotNumber,
        ssnLastFour = ssnLastFour,
        preferredLanguage = preferredLanguage,
        race = race
    )
}

fun AdditionalInfoData.toFragmentAdditionalInfoBinding(binding: FragmentAdditionalInfoBinding) {
    with (binding) {
        spinnerParty.setEditText(party.toString())
        emailEditText.text = emailAddress.toEditable()
        phone.text = phoneNumber.toEditable()
        spinnerPhoneType.setEditText(phoneType.toString())
        politicalPartyChangeTextbox.isChecked = hasChangedPoliticalParty
        doesNotHavePennDotCheckbox.isChecked = !knowsPennDotNumber
        ssnLastFourCheckbox.isChecked = !knowsSsnLastFour
        emailOptIn.isChecked = partnerEmailOptIn
        checkboxCanReceiveText.isChecked = partnerSmsOptIn
        checkboxPartnerVolunteerOptIn.isChecked = partnerVolunteerOptIn
        otherPartyEditText.text = otherPoliticalParty?.toEditable()
        pennDotEditText.text = pennDotNumber?.toEditable()
        ssnLastFourEditText.text = ssnLastFour?.toEditable()
        preferredLanguage?.let { spinnerPreferredLanguage.setEditText(it.toString()) }
        race?.let { spinnerRace.setEditText(it.toString()) }
    }
}
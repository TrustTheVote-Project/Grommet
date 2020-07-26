package com.rockthevote.grommet.ui.registration.address

import com.rockthevote.grommet.databinding.FragmentPersonalInfoBinding
import com.rockthevote.grommet.util.extensions.bindToAddressView
import com.rockthevote.grommet.util.extensions.toAddress

fun FragmentPersonalInfoBinding.toAddressData(): PersonalInfoData {
    val homeAddress = homeAddress.toAddress()!!
    val isMailingAddressDifferent = mailingAddressIsDifferent.isChecked
    val hasPreviousAddressChanged = addressChanged.isChecked

    val mailingAddress = if (isMailingAddressDifferent) mailingAddress.toAddress() else null

    val previousAddress = if (hasPreviousAddressChanged) previousAddress.toAddress() else null

    return PersonalInfoData(
        homeAddress = homeAddress,
        isMailingAddressDifferent = isMailingAddressDifferent,
        hasPreviousAddress = hasPreviousAddressChanged,
        mailingAddress = mailingAddress,
        previousAddress = previousAddress
    )
}

fun PersonalInfoData.toFragmentPersonalInfoBinding(binding: FragmentPersonalInfoBinding) {
    homeAddress.bindToAddressView(binding.homeAddress)
    binding.mailingAddressIsDifferent.isChecked = isMailingAddressDifferent
    binding.addressChanged.isChecked = hasPreviousAddress

    mailingAddress?.bindToAddressView(binding.mailingAddress)
    previousAddress?.bindToAddressView(binding.previousAddress)
}
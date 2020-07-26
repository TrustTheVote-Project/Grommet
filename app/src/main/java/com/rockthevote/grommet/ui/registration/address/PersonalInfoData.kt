package com.rockthevote.grommet.ui.registration.address

data class PersonalInfoData(
    // Mandatory
    val homeAddress: AddressData,
    val isMailingAddressDifferent: Boolean,
    val hasPreviousAddress: Boolean,
    // Optional
    val mailingAddress: AddressData?,
    val previousAddress: AddressData?
)


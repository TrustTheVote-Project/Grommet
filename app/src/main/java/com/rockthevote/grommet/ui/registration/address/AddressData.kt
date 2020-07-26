package com.rockthevote.grommet.ui.registration.address

data class AddressData(
    // Mandatory
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val county: String?,
    // Optional
    val streetAddressTwo: String?,
    val unitType: String?,
    val unitNumber: String?
)
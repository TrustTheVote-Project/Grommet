package com.rockthevote.grommet.util.extensions

import com.rockthevote.grommet.databinding.ViewAddressBinding
import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.views.AddressView

fun AddressView.toAddress(): AddressData? {
    val addressBinding = ViewAddressBinding.bind(this)

    with (addressBinding) {
        val streetAddress = street.text?.toString() ?: return null
        val city = city.text?.toString() ?: return null
        val state = spinnerState.spinnerText ?: return null
        val zipCode = zip.text?.toString() ?: return null
        val county = spinnerCounty.spinnerText ?: return null

        val streetAddressTwo = street2.text?.toString()
        val unitType = spinnerUnitType.spinnerText
        val unitNumber = unit.text?.toString()

        return AddressData(
            streetAddress,
            city,
            state,
            zipCode,
            county,
            streetAddressTwo,
            unitType,
            unitNumber
        )
    }
}
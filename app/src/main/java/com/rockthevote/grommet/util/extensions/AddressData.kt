package com.rockthevote.grommet.util.extensions

import com.rockthevote.grommet.databinding.ViewAddressBinding
import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.views.AddressView

fun AddressData.toFriendlyString(): String {
    val lineOne = streetAddress
    val lineTwo = streetAddressTwo

    val unitLine = listOf(unitType, unitNumber)
        .listOfNotNullOrEmpty()
        .joinToString(" ")

    val lineThree = if (unitLine.isEmpty()) {
        null
    } else {
        unitLine
    }

    val lineFour = "$city, $state $zipCode"

    return listOf(
        lineOne,
        lineTwo,
        lineThree,
        lineFour
    ).listOfNotNullOrEmpty()
        .joinToString("\n")
}

fun AddressData.bindToAddressView(view: AddressView) {
    val binding = ViewAddressBinding.bind(view)
    with (binding) {
        street.text = streetAddress.toEditable()
        city.text = this@bindToAddressView.city.toEditable()
        spinnerState.setEditText(state)
        zip.text = zipCode.toEditable()

        spinnerCounty.setEditText(county ?: "")
        street2.text = streetAddressTwo?.toEditable()
        unitType?.let { spinnerUnitType.setEditText(it) }
        unit.text = unitNumber?.toEditable()
    }
}
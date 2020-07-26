package com.rockthevote.grommet.util.extensions

import com.rockthevote.grommet.databinding.ViewNameBinding
import com.rockthevote.grommet.ui.registration.name.PersonNameData
import com.rockthevote.grommet.ui.views.NameView

fun PersonNameData.bindToNameView(view: NameView) {
    ViewNameBinding.bind(view)?.let {
        it.firstName.text = firstName.toEditable()
        it.spinnerTitle.setEditText(title.toString())
        it.lastName.text = lastName.toEditable()

        it.middleName.text = middleName?.toEditable()
        suffix?.toString()?.let { suffixString -> it.spinnerSuffix.setEditText(suffixString) }
    }
}

fun PersonNameData.toFullName(): String {
    val prefix = title.toString()
    val first = firstName
    val middle = middleName
    val last = lastName
    val suffix = suffix?.toString()
    return listOf(
        prefix,
        first,
        middle,
        last,
        suffix
    ).listOfNotNullOrEmpty()
        .joinToString(" ")
}
package com.rockthevote.grommet.util.extensions

import com.rockthevote.grommet.data.db.model.Prefix
import com.rockthevote.grommet.data.db.model.Suffix
import com.rockthevote.grommet.databinding.ViewNameBinding
import com.rockthevote.grommet.ui.registration.name.PersonNameData
import com.rockthevote.grommet.ui.views.NameView

@Throws(IllegalArgumentException::class)
fun NameView.toPersonName(): PersonNameData? {
    val nameBinding = ViewNameBinding.bind(this)

    return with(nameBinding) {
        val title = Prefix.fromString(spinnerTitle.spinnerText!!)
        if (title == Prefix.NONE) return null

        val suffix = spinnerSuffix.editText.text?.toString()?.let {
            val result = Suffix.fromString(it)
            if (result == Suffix.EMPTY) null else result
        }

        val transformedMiddleName = middleName.text?.run { if (isNullOrEmpty()) null else toString() }

        PersonNameData(
            firstName = firstName.text?.toString() ?: return null,
            title = title,
            lastName = lastName.text?.toString() ?: return null,
            middleName = transformedMiddleName,
            suffix = suffix
        )
    }
}
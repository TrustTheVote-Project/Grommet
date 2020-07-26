package com.rockthevote.grommet.ui.registration.name

import com.rockthevote.grommet.databinding.FragmentNewRegistrantBinding
import com.rockthevote.grommet.util.Dates
import com.rockthevote.grommet.util.extensions.bindToNameView
import com.rockthevote.grommet.util.extensions.toEditable
import com.rockthevote.grommet.util.extensions.toPersonName

fun FragmentNewRegistrantBinding.toNameRegistrationData(): NewRegistrantData {
    return NewRegistrantData(
        name = name.toPersonName()!!,
        birthday = Dates.parseISO8601_ShortDate(edittextBirthday.text.toString())!!,
        isUsCitizen = checkboxIsUsCitizen.isChecked,
        is18OrOlderByNextElection = checkboxIsEighteen.isChecked,
        hasChangedName = nameChanged.isChecked,
        previousName = if (nameChanged.isChecked) previousName.toPersonName() else null
    )
}

fun NewRegistrantData.toFragmentNewRegistratntBinding(binding: FragmentNewRegistrantBinding) {
    binding.let {
        name.bindToNameView(it.name)
        it.edittextBirthday.text = Dates.formatAsISO8601_ShortDate(birthday).toEditable()
        it.checkboxIsUsCitizen.isChecked = isUsCitizen
        it.checkboxIsEighteen.isChecked = is18OrOlderByNextElection
        it.nameChanged.isChecked = hasChangedName
        previousName?.bindToNameView(it.previousName)
    }
}
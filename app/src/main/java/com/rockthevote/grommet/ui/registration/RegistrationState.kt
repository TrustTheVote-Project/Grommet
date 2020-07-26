package com.rockthevote.grommet.ui.registration

import androidx.annotation.StringRes

sealed class RegistrationState {
    object Init: RegistrationState()
    object InProgress : RegistrationState()
    object Abandoned : RegistrationState()
    object Complete : RegistrationState()
    class RegistrationError(
        val isAcknowledged: Boolean = false,
        @StringRes val errorMsg: Int,
        @StringRes val formatVar: IntArray?
    ): RegistrationState()
}
package com.rockthevote.grommet.ui.registration

import androidx.annotation.StringRes

/**
 * Provided registration data is invalid.
 */
class InvalidRegistrationException(
    msg: String,
    @StringRes val userMessage: Int,
    @StringRes vararg val formatVar: Int
) : Exception(msg)
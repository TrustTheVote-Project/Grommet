package com.rockthevote.grommet.ui.registration.name

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class BirthdayValidationState {
    object Success : BirthdayValidationState()
    class Error(val regDate: String) : BirthdayValidationState()
}
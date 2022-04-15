package com.rockthevote.grommet.ui.eventFlow

sealed class ValidVersionState {
    object Valid : ValidVersionState()
    object NotValid : ValidVersionState()
    object Error : ValidVersionState()
}
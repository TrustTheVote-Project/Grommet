package com.rockthevote.grommet.ui.eventFlow

sealed class CompleteShiftState {
    object Completed : CompleteShiftState()
    object NotValid : CompleteShiftState()
    object Error : CompleteShiftState()
}
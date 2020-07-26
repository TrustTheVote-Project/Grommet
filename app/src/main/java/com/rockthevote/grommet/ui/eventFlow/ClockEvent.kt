package com.rockthevote.grommet.ui.eventFlow

import androidx.annotation.StringRes

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class ClockEvent {

    object Loading : ClockEvent()

    abstract class Effect : ClockEvent()
    class ClockingError(@StringRes val errorMsgId: Int) : Effect()
}
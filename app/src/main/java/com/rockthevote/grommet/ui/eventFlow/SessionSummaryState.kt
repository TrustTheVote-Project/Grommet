package com.rockthevote.grommet.ui.eventFlow

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class SessionSummaryState {

    abstract class Effect : SessionSummaryState()
    object Cleared : Effect()
    object ShiftUpdated : Effect()
    object NetworkError : Effect()
    object Error : Effect()
}
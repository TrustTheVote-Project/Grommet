package com.rockthevote.grommet.ui.eventFlow

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class CanvasserInfoState {
    abstract class Effect : CanvasserInfoState()
    object Success : Effect()
    object Error : Effect()
    object LocationError : Effect()
}
package com.rockthevote.grommet.ui.eventFlow

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class PartnerLoginState {
    object Init : PartnerLoginState()
    object Loading : PartnerLoginState()

    abstract class Effect : PartnerLoginState()
    object Success : Effect()
    object Error : Effect()
    object InvalidVersion : Effect()
    object NotFound : Effect()
}
package com.rockthevote.grommet.ui.eventFlow

import androidx.annotation.StringRes

sealed class EndCollectionState {

    object Loading : EndCollectionState()

    abstract class Effect : EndCollectionState()
    class Error(@StringRes val errorMsgId: Int) : Effect()
    class Done() : Effect()
    class DoneStrangerClt() : Effect()
}
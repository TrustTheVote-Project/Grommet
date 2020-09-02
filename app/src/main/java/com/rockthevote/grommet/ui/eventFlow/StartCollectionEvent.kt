package com.rockthevote.grommet.ui.eventFlow

import androidx.annotation.StringRes

sealed class StartCollectionEvent {

    object Loading : StartCollectionEvent()

    abstract class Effect : StartCollectionEvent()
    class Error(@StringRes val errorMsgId: Int) : Effect()
    class Done() : Effect()
}
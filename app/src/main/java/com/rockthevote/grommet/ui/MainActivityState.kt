package com.rockthevote.grommet.ui

sealed class MainActivityState {
    object Init : MainActivityState()
    object Loading : MainActivityState()
    object Error : MainActivityState()

    data class Content(
            val pendingUploads: Int,
            val failedUploads: Int
    ) : MainActivityState()
}
package com.rockthevote.grommet.ui.registration.review

sealed class ReviewAndConfirmState {
    class Content(val data: ReviewAndConfirmStateData) : ReviewAndConfirmState()
}
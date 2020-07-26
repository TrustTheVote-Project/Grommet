package com.rockthevote.grommet.ui

sealed class MainActivityError {
    class UploadRegistrationError(val stringMessageId: Int) : MainActivityError()
}
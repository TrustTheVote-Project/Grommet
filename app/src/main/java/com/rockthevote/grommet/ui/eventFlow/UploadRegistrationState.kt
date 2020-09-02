package com.rockthevote.grommet.ui.eventFlow

sealed class UploadRegistrationState {
    object Init : UploadRegistrationState()
    object Loading : UploadRegistrationState()
    object Error : UploadRegistrationState()

    data class Content(
            val pendingUploads: Int,
            val failedUploads: Int
    ) : UploadRegistrationState()
}
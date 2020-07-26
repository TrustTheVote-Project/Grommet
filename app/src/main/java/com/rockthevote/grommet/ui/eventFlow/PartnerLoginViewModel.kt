package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.BuildConfig
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.api.model.PartnerNameResponse
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Mechanical Man on 5/16/20.
 */
class PartnerLoginViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val rockyService: RockyService,
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao
) : ViewModel() {

    val partnerInfoPartnerID: LiveData<Long> =
            Transformations.map(partnerInfoDao.getCurrentPartnerInfoLive()) { result ->
                result?.partnerId ?: -1
            }

    private val _partnerLoginState = MutableLiveData<PartnerLoginState>(PartnerLoginState.Init)
    val partnerLoginState: LiveData<PartnerLoginState> = _partnerLoginState

    private val _effect = LiveEvent<PartnerLoginState.Effect?>()
    val effect: LiveData<PartnerLoginState.Effect?> = _effect

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        setStateToError()
    }

    fun validatePartnerId(partnerId: Long) {
        updateState(PartnerLoginState.Loading)

        if (partnerId == partnerInfoPartnerID.value) {
            // just continue on if the value is the same
            updateState(PartnerLoginState.Init)
            updateEffect(PartnerLoginState.Success)

        } else {
            viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
                runCatching {
                    val result = rockyService.getPartnerName(partnerId.toString(), BuildConfig.VERSION_CODE.toString()).toBlocking().value()

                    val response = result.response()

                    if (response?.code() == 404) {
                        throw PartnerLoginNotFoundException("404 not found")
                    }

                    if (result.isError) {
                        throw result.error() ?: PartnerLoginViewModelException("Error retrieving result")
                    } else {
                        response?.body()
                            ?: throw PartnerLoginViewModelException("Successful result with empty body received")
                    }
                }.onSuccess {

                    updateState(PartnerLoginState.Init)

                    val currentVersion = BuildConfig.VERSION_CODE
                    val requiredVersion = it.appVersion()

                    val effect = when {
                        currentVersion < requiredVersion -> PartnerLoginState.InvalidVersion
                        else -> completePartnerValidation(partnerId, it)
                    }

                    updateEffect(effect)
                }.onFailure {
                    Timber.d(it, "API request failure - partner validation")

                    when (it) {
                        is PartnerLoginNotFoundException -> {
                            val effect = PartnerLoginState.NotFound
                            updateEffect(effect)
                        }
                        else -> {
                            setStateToError()
                        }
                    }
                }
            }
        }
    }

    private fun completePartnerValidation(
            partnerId: Long,
            partnerNameResponse: PartnerNameResponse
    ): PartnerLoginState.Effect {
        partnerInfoDao.deleteAllPartnerInfo()
        sessionDao.clearAllSessionInfo()
        partnerInfoDao.insertPartnerInfo(PartnerInfo(
            partnerId = partnerId,
            appVersion = partnerNameResponse.appVersion().toFloat(),
            isValid = partnerNameResponse.isValid,
            partnerName = partnerNameResponse.partnerName(),
            registrationDeadlineDate = partnerNameResponse.registrationDeadlineDate(),
            registrationNotificationText = partnerNameResponse.registrationNotificationText(),
            volunteerText = partnerNameResponse.partnerVolunteerText()
        ))
        return PartnerLoginState.Success
    }

    fun clearPartnerInfo() {
        Timber.d("Deleting partner info")
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            partnerInfoDao.deleteAllPartnerInfo()
            sessionDao.clearAllSessionInfo()
        }
    }

    private fun setStateToError() {
        updateState(PartnerLoginState.Init)
        updateEffect(PartnerLoginState.Error)
    }

    private fun updateState(newState: PartnerLoginState) {
        Timber.d("Handling new state: $newState")
        _partnerLoginState.postValue(newState)
    }

    private fun updateEffect(newEffect: PartnerLoginState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }

    private class PartnerLoginViewModelException(msg: String) : Exception(msg)
    private class PartnerLoginNotFoundException(msg: String) : Exception(msg)
}

class PartnerLoginViewModelFactory(
        private val rockyService: RockyService,
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return PartnerLoginViewModel(dispatchers, rockyService, partnerInfoDao, sessionDao) as T
    }
}
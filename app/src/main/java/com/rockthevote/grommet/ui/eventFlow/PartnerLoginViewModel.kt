package com.rockthevote.grommet.ui.eventFlow

import android.util.Log
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.api.model.PartnerNameResponse
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
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

//    val partnerInfoPartnerName: LiveData<String> =
//            Transformations.map(partnerInfoDao.getCurrentPartnerInfoLive()) { result ->
//                result?.isValid ?: ""
//            }

    private val _partnerLoginState = MutableLiveData<PartnerLoginState>(PartnerLoginState.Init)
    val partnerLoginState: LiveData<PartnerLoginState> = _partnerLoginState

    private val _effect = LiveEvent<PartnerLoginState.Effect?>()
    val effect: LiveData<PartnerLoginState.Effect?> = _effect

    private val _partnerName = LiveEvent<String>()
    val partnerName: LiveData<String> = _partnerName
//    private val _isValidVersion = LiveEvent<ValidVersionState>()
//    val isValidVersion: LiveData<ValidVersionState> = _isValidVersion

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        setStateToError()
    }


    fun validatePartnerId(partnerId: Long) {
        updateState(PartnerLoginState.Loading)
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            if (partnerId == partnerInfoPartnerID.value) {
                // just continue on if the value is the same
                updateState(PartnerLoginState.Init)
                _partnerName.postValue(partnerInfoDao.getCurrentPartnerInfo().partnerName)
                updateEffect(PartnerLoginState.Success)

            } else {
                runCatching {
                    val result = rockyService.getPartnerName(partnerId.toString()).toBlocking().value()

                    val response = result.response()

                    if (response?.code() == 404) {
                        throw PartnerLoginNotFoundException("404 not found")
                    }

                    if (result.isError) {
                        throw result.error()
                                ?: PartnerLoginViewModelException("Error retrieving result")
                    } else {
                        response?.body()
                                ?: throw PartnerLoginViewModelException("Successful result with empty body received")
                    }
                }.onSuccess {

                    updateState(PartnerLoginState.Init)

                    val effect = when {
                        it.isValid -> completePartnerValidation(partnerId, it)
                        else -> PartnerLoginState.InvalidVersion
                    }

//                    val effect = when {
//                        it.isValid -> PartnerLoginState.Success
//                        else -> PartnerLoginState.InvalidVersion
//                    }

                    updateEffect(effect)
                }.onFailure {
                    Timber.d(it, "API request failure - partner validation")

                    when (it) {
                        is PartnerLoginNotFoundException -> {
                            val effect = PartnerLoginState.NotFound
                            updateEffect(effect)
                        }
                        is NullPointerException -> {
                            Timber.e("NullPointerException")
                            updateEffect(PartnerLoginState.InvalidVersion)
                            updateState(PartnerLoginState.Init)
                        }
                        else -> {
                            Timber.e("other error")
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
                errors = partnerNameResponse.errors(),
                validLocations = partnerNameResponse.validLocations(),
                isValid = partnerNameResponse.isValid,
                partnerName = partnerNameResponse.partnerName(),
                registrationDeadlineDate = partnerNameResponse.registrationDeadlineDate(),
                registrationNotificationText = partnerNameResponse.registrationNotificationText(),
                volunteerText = partnerNameResponse.partnerVolunteerText()
        ))
        _partnerName.postValue(partnerNameResponse.partnerName())
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
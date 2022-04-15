package com.rockthevote.grommet.ui.eventFlow

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.R
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.api.model.UpdateShiftBody
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.data.db.model.SessionStatus
import com.rockthevote.grommet.util.SharedPrefKeys
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

/**
 * Created by Mechanical Man on 5/30/20.
 */
class SessionTimeTrackingViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao,
        private val registrationDao: RegistrationDao,
        private val sharedPreferences: SharedPreferences,
        private val rockyService: RockyService
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        throw throwable
    }

    private val _effect = LiveEvent<SessionSummaryState.Effect?>()
    val effect: LiveData<SessionSummaryState.Effect?> = _effect
    var isEffectDone = false

//    private val _clockState = LiveEvent<ClockEvent>()
//    val clockState: LiveData<ClockEvent> = _clockState

    private val _endStrState = LiveEvent<EndCollectionState.Effect?>()
    val endStrState: LiveData<EndCollectionState.Effect?> = _endStrState

    private val _sessionStatus = LiveEvent<SessionStatus>()
    val sessionStatus: LiveData<SessionStatus> = _sessionStatus

    private val _state = MutableLiveData<UploadRegistrationState>()
    val state: LiveData<UploadRegistrationState> = _state

    private val _completeShiftState = MutableLiveData<CompleteShiftState>(CompleteShiftState.NotValid)
    val completeShiftState: LiveData<CompleteShiftState> = _completeShiftState

    val sessionData = Transformations.map(partnerInfoDao.getPartnerInfoWithSessionAndRegistrations()) { result ->
        result?.let {
            val partnerInfo = result.partnerInfo
            val session = result.sessionWithRegistrations?.session
            val registrations = result.sessionWithRegistrations?.registrations

            SessionSummaryData(
                    partnerInfo?.partnerName ?: "",
                    session?.canvasserName ?: "",
                    session?.openTrackingId ?: "",
                    session?.partnerTrackingId ?: "",
                    session?.deviceId ?: "",
                    session?.smsCount ?: 0,
                    session?.driversLicenseCount ?: 0,
                    session?.ssnCount ?: 0,
                    session?.emailCount ?: 0,
                    session?.registrationCount ?: 0,
                    session?.abandonedCount ?: 0,
                    registrations ?: emptyList(),
                    session?.clockInTime,
                    session?.clockOutTime,
                    session?.canvasserLastName ?: ""
            )
        } ?: run {
            SessionSummaryData()
        }
    }

    fun uploadRegistrations() {
        updateState(UploadRegistrationState.Loading)
        isEffectDone = true
        viewModelScope.launch(dispatchers.io) {

            val adapter = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                    .adapter(RockyRequest::class.java)

            val requests = loadRequestsFromDb().map {
                Timber.e(it.registrationData.toString())
                Timber.e("---------------------------------------------")

                it to adapter.fromJson(it.registrationData)
            }

            val results = requests.map {
                // Maps and simultaneously makes the regist
                // ration request, adding the deferred result to [second]
                Timber.e(it.second.toString())
                Timber.e("---------------------------------------------")
                Timber.e(it.first.toString())
                it.first to async { rockyService.register(it.second).toBlocking().value() }
            }

            val successfulRegistrations = results.filter { registrationPair ->
                runCatching {
                    val result = registrationPair.second.await()
                    !result.isError
                }.getOrElse {
                    // Bump the number of upload attempts in the registration
                    val uploadAttempts = registrationPair.first.uploadAttempts + 1
                    val updatedRegistration = registrationPair.first.copy(uploadAttempts = uploadAttempts)
                    registrationDao.update(updatedRegistration)

                    Timber.w(it, "Error making registration call")
                    false
                }
            }.map {
                it.first
            }

//            if (successfulRegistrations.size != results.size) {
//                _errorStream.postValue(MainActivityError.UploadRegistrationError(R.string.generic_error))
//            }

            registrationDao.delete(*successfulRegistrations.toTypedArray())

            // Using the database as a source of truth
            val pendingUploads = registrationDao.getAll().size

            val result = UploadRegistrationState.Content(
                    pendingUploads = pendingUploads,
                    failedUploads = 0 // TODO determine if we need failed uploads
            )

            updateState(result)
        }
    }

    private val sharedPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            SharedPrefKeys.KEY_SESSION_STATUS -> updateSessionStatus()
        }
    }

    init {
        updateSessionStatus()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPrefListener)
    }

    fun getRegistrationFromString(data: String): String {
        val adapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(RockyRequest::class.java)
        val registration: RockyRequest? = adapter.fromJson(data)
        registration?.rockyRequest?.voterRecordsRequest?.voterRegistration?.name
        val firstName = registration?.rockyRequest?.voterRecordsRequest?.voterRegistration?.name?.firstName
        val lastName = registration?.rockyRequest?.voterRecordsRequest?.voterRegistration?.name?.lastName
        val sName = registration?.rockyRequest?.voterRecordsRequest?.voterRegistration?.name?.middleName
        return "$firstName $lastName"
    }

    private fun updateState(newState: UploadRegistrationState) {
        Timber.d("Handling new state: $newState")
        _state.postValue(newState)
    }



//    private fun updateClockState(value: ClockEvent) {
//        _clockState.postValue(value)
//    }

    private suspend fun loadRequestsFromDb() = registrationDao.getAll()


    fun completeShift(isZeroRegistrations: Boolean) {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {

            runCatching {
                val shiftId = sessionDao.getCurrentSession()?.shiftId

                if (isZeroRegistrations) {

                    val session = sessionDao.getCurrentSession()
                    val sessionShiftId = session?.shiftId
                    val updateShiftBody = session?.abandonedCount?.let {
                        UpdateShiftBody.builder()
                                .abandonedCount(it)
                                .completedCount(session.registrationCount)
                                .clockIn(session.clockInTime.toString())
                                .clockOut(session.clockOutTime.toString())
                                .build()
                    }
                    Timber.e(session?.abandonedCount.toString())
                    Timber.e(session?.clockInTime.toString())
                    Timber.e(session?.clockOutTime.toString())
                    Timber.e(session?.registrationCount.toString())
                    Timber.e(updateShiftBody.toString())
                    val result = rockyService.updateShift(sessionShiftId, updateShiftBody).toBlocking().value()

                    if (result.isError) {
                        throw result.error()
                                ?: UpdateShiftException("Error retrieving result")
                    }
                }

                val result = rockyService.completeShift(shiftId).toBlocking().value()

                val response = result.response()

                if (response?.code() == 404) {
                    updateCompleteShiftState(CompleteShiftState.Error)
                }
                Timber.e(response?.body().toString())
                if (result.isError) {
                    throw result.error() ?: CompleteShiftException("Error retrieving result")
                }
            }.onSuccess {
                Timber.e("updateCompleteShiftState(CompleteShiftState.Completed)")
                updateCompleteShiftState(CompleteShiftState.Completed)
//                if(it?.)
//                updateState(PartnerLoginState.Init)
//
//                val currentVersion = BuildConfig.VERSION_CODE
//                val requiredVersion = it.appVersion()
//
//                val effect = when {
//                    currentVersion < requiredVersion -> PartnerLoginState.InvalidVersion
//                    else -> completePartnerValidation(partnerId, it)
//                }

//                updateEffect(effect)
            }.onFailure {
                Timber.e(it, "API request failure - partner validation")
                updateCompleteShiftState(CompleteShiftState.Error)
//                when (it) {
//                    is PartnerLoginViewModel.PartnerLoginNotFoundException -> {
//                        val effect = PartnerLoginState.NotFound
//                        updateEffect(effect)
//                    }
//                    else -> {
//                        setStateToError()
//                    }
//                }
            }

        }
    }

    private fun updateCompleteShiftState(newState: CompleteShiftState) {
        Timber.d("Handling new state: $newState")
        _completeShiftState.postValue(newState)
    }

    fun endCollection(date: Date, isStranger: Boolean) {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            runCatching {
                viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
                    val session = sessionDao.getCurrentSession() ?: return@launch
                    val newSessionData = session.copy(clockOutTime = date)
                    sessionDao.updateSession(newSessionData)
                }
            }.onSuccess {
                if (isStranger) {
                    updateEndStgEvent(EndCollectionState.DoneStrangerClt())
                } else {
                    updateEndStgEvent(EndCollectionState.Done())
                }
            }.onFailure {
                updateEndStgEvent(EndCollectionState.Error(R.string.error_title))
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun updateShift() {


        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            runCatching {

                val session = sessionDao.getCurrentSession()
                val shiftId = session?.shiftId
                Timber.e(session?.abandonedCount.toString())
                Timber.e(session?.clockInTime.toString())
                Timber.e(session?.clockOutTime.toString())
                Timber.e(session?.registrationCount.toString())
                val updateShiftBody = session?.abandonedCount?.let {
                    UpdateShiftBody.builder()
                            .abandonedCount(it)
                            .completedCount(session.registrationCount)
                            .clockIn(session.clockInTime.toString())
                            .clockOut(session.clockOutTime.toString())
                            .build()
                }
                Timber.e(session?.abandonedCount.toString())
                Timber.e(session?.clockInTime.toString())
                Timber.e(session?.clockOutTime.toString())
                Timber.e(session?.registrationCount.toString())
                Timber.e(updateShiftBody.toString())
                val result = rockyService.updateShift(shiftId, updateShiftBody).toBlocking().value()

                if (result.isError) {
                    throw result.error()
                            ?: UpdateShiftException("Error retrieving result")
                }

//                else {
//                    result.response()?.body().toString()
//                            ?: throw UpdateShiftException("Successful result with empty body received")
//                }
            }.onSuccess {

                updateEffect(SessionSummaryState.ShiftUpdated)
            }.onFailure {
                Timber.d(it, "failure updating canvasser info")

                val effect = when (it) {
                    is UpdateShiftException -> SessionSummaryState.NetworkError
                    else -> SessionSummaryState.Error
                }
                updateEffect(effect)
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            sessionDao.clearAllSessionInfo()
            updateEffect(SessionSummaryState.Cleared)
            updateState(UploadRegistrationState.Init)
        }
    }

    private fun updateEndStgEvent(event: EndCollectionState.Effect) {
        _endStrState.postValue(event)
    }

    fun updateSessionStatus() {
        viewModelScope.launch(dispatchers.io) {
            val statusString = sharedPreferences.getString(SharedPrefKeys.KEY_SESSION_STATUS, null)
                    ?: return@launch
            val status = SessionStatus.fromString(statusString) ?: return@launch
            _sessionStatus.postValue(status)
        }
    }

    private fun updateEffect(newEffect: SessionSummaryState.Effect) {
        Timber.d("lox Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPrefListener)
    }

    private class ClockInOutException(msg: String) : Exception(msg)
    private class UpdateShiftException(msg: String) : Exception(msg)
    private class CompleteShiftException(msg: String) : Exception(msg)

}

class SessionTimeTrackingViewModelFactory(
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao,
        private val registrationDao: RegistrationDao,
        private val sharedPreferences: SharedPreferences,
        private val rockyService: RockyService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return SessionTimeTrackingViewModel(
                dispatchers,
                partnerInfoDao,
                sessionDao,
                registrationDao,
                sharedPreferences,
                rockyService) as T
    }
}

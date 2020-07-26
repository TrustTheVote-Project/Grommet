package com.rockthevote.grommet.ui.eventFlow

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.R
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.api.model.ClockInRequest
import com.rockthevote.grommet.data.api.model.ClockOutRequest
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.SessionStatus
import com.rockthevote.grommet.util.Dates
import com.rockthevote.grommet.util.SharedPrefKeys
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _clockState = LiveEvent<ClockEvent>()
    val clockState: LiveData<ClockEvent> = _clockState

    private val _sessionStatus = LiveEvent<SessionStatus>()
    val sessionStatus: LiveData<SessionStatus> = _sessionStatus

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
                    session?.clockOutTime
            )
        } ?: run {
            SessionSummaryData()
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

    fun clockOut() {
        viewModelScope.launch(dispatchers.io) {
            val canClockOut = registrationDao.getAll().isEmpty()

            withContext(dispatchers.main) {
                if (canClockOut) {
                    makeClockOutRequest()
                } else {
                    updateClockState(ClockEvent.ClockingError(R.string.clock_out_must_upload))
                }
            }
        }
    }

    fun clockIn() {
        makeClockInRequest()
    }

    private fun updateClockState(value: ClockEvent) {
        _clockState.postValue(value)
    }

    private fun makeClockInRequest() {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            updateClockState(ClockEvent.Loading)

            runCatching {
                val session = sessionDao.getCurrentSession()
                val clockInRequest = ClockInRequest.builder()
                        .canvasserName(session?.canvasserName)
                        .geoLocation(session?.geoLocation)
                        .clockInDatetime(Dates.formatAsISO8601_Date(session?.clockOutTime))
                        .openTrackingId(session?.openTrackingId)
                        .partnerTrackingId(session?.partnerTrackingId)
                        .sourceTrackingId(session?.sourceTrackingId)
                        .build()

                val result = rockyService.clockIn(clockInRequest).toBlocking().value()

                if (result.isError) {
                    throw result.error()
                            ?: ClockInOutException("Error retrieving result")
                }
            }.onSuccess {
                // update session data and status
                viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
                    val session = sessionDao.getCurrentSession() ?: return@launch
                    val newSessionData = session.copy(clockInTime = Date())
                    sessionDao.updateSession(newSessionData)
                }

                sharedPreferences.edit().putString(
                        SharedPrefKeys.KEY_SESSION_STATUS,
                        SessionStatus.CLOCKED_IN.toString())
                        .apply()

            }.onFailure {
                updateClockState(ClockEvent.ClockingError(R.string.generic_network_error))
            }
        }
    }

    private fun makeClockOutRequest() {

        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            updateClockState(ClockEvent.Loading)

            runCatching {
                val session = sessionDao.getCurrentSession()
                val clockoutRequest = ClockOutRequest.builder()
                        .canvasserName(session?.canvasserName)
                        .abandonedRegistrations(session?.abandonedCount ?: 0)
                        .completedRegistrations(session?.registrationCount ?: 0)
                        .geoLocation(session?.geoLocation)
                        .clockOutDatetime(Dates.formatAsISO8601_Date(session?.clockOutTime))
                        .openTrackingId(session?.openTrackingId)
                        .partnerTrackingId(session?.partnerTrackingId)
                        .sourceTrackingId(session?.sourceTrackingId)
                        .build()

                val result = rockyService.clockOut(clockoutRequest).toBlocking().value()

                if (result.isError) {
                    throw result.error()
                            ?: ClockInOutException("Error retrieving result")
                }
            }.onSuccess {
                // update session data and status
                viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
                    val session = sessionDao.getCurrentSession() ?: return@launch
                    val newSessionData = session.copy(clockOutTime = Date())
                    sessionDao.updateSession(newSessionData)
                }

                sharedPreferences.edit().putString(
                        SharedPrefKeys.KEY_SESSION_STATUS,
                        SessionStatus.CLOCKED_OUT.toString())
                        .apply()
            }.onFailure {
                updateClockState(ClockEvent.ClockingError(R.string.generic_error))
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            sessionDao.clearAllSessionInfo()
            updateEffect(SessionSummaryState.Cleared)
        }
    }

    fun updateSessionStatus() {
        viewModelScope.launch(dispatchers.io) {
            val statusString = sharedPreferences.getString(SharedPrefKeys.KEY_SESSION_STATUS, null) ?: return@launch
            val status = SessionStatus.fromString(statusString) ?: return@launch
            _sessionStatus.postValue(status)
        }
    }

    private fun updateEffect(newEffect: SessionSummaryState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPrefListener)
    }

    private class ClockInOutException(msg: String) : Exception(msg)

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

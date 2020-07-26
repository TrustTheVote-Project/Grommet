package com.rockthevote.grommet.ui

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.R
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.data.db.model.SessionStatus
import com.rockthevote.grommet.util.SharedPrefKeys.KEY_SESSION_STATUS
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import timber.log.Timber

class MainActivityViewModel(
    private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
    private val rockyService: RockyService,
    private val registrationDao: RegistrationDao,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _state = MutableLiveData<MainActivityState>(MainActivityState.Init)
    val state: LiveData<MainActivityState> = _state

    private val _sessionStatus = LiveEvent<SessionStatus>()
    val sessionStatus: LiveData<SessionStatus> = _sessionStatus

    private val _errorStream = MediatorLiveData<MainActivityError>()
    val errorStream: LiveData<MainActivityError> = _errorStream

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private val supervisorJob = SupervisorJob()

    private val rockyRequestScope = CoroutineScope(dispatchers.io + coroutineExceptionHandler + supervisorJob)

    fun refreshPendingUploads() {
        viewModelScope.launch(dispatchers.io) {
            updateState(MainActivityState.Loading)

            val requests = loadRequestsFromDb()

            val content = MainActivityState.Content(
                pendingUploads = requests.size,
                failedUploads = 0 // TODO need to figure out how/if we're going to calculate this
            )

            updateState(content)
        }
    }

    fun uploadRegistrations() {
        updateState(MainActivityState.Loading)

        rockyRequestScope.launch {

            val adapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(RockyRequest::class.java)

            val requests = loadRequestsFromDb().map {
                it to adapter.fromJson(it.registrationData)
            }

            val results = requests.map {
                // Maps and simultaneously makes the registration request, adding the deferred result to [second]
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

            if (successfulRegistrations.size != results.size) {
                _errorStream.postValue(MainActivityError.UploadRegistrationError(R.string.generic_error))
            }

            registrationDao.delete(*successfulRegistrations.toTypedArray())

            // Using the database as a source of truth
            val pendingUploads = registrationDao.getAll().size

            val result = MainActivityState.Content(
                pendingUploads = pendingUploads,
                failedUploads = 0 // TODO determine if we need failed uploads
            )

            updateState(result)
        }
    }

    /**
     * Resolves session status, if a session doesn't exists, session status is
     * [SessionStatus.NEW_SESSION]
     */
    fun loadSessionStatus() {
        viewModelScope.launch(dispatchers.io) {
            val rawState = sharedPreferences.getString(KEY_SESSION_STATUS, null)

            val status = SessionStatus.fromString(rawState) ?: SessionStatus.PARTNER_UPDATE

            _sessionStatus.postValue(status)
        }
    }

    fun updateSessionStatus(sessionStatus: SessionStatus) {
        viewModelScope.launch(dispatchers.io) {
            sharedPreferences
                .edit()
                .putString(KEY_SESSION_STATUS, sessionStatus.toString())
                .apply()

            loadSessionStatus()
        }
    }

    private suspend fun loadRequestsFromDb() = registrationDao.getAll()

    private fun updateState(newState: MainActivityState) {
        Timber.d("Handling new state: $newState")
        _state.postValue(newState)
    }

    override fun onCleared() {
        super.onCleared()
        supervisorJob.cancelChildren()
    }

    fun asyncCanRegister(successCallback: () -> Unit, failCallback: () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            val rawState = sharedPreferences.getString(KEY_SESSION_STATUS, null)

            val status = SessionStatus.fromString(rawState) ?: SessionStatus.PARTNER_UPDATE

            withContext(dispatchers.main) {
                if (status == SessionStatus.CLOCKED_IN) {
                    successCallback()
                } else {
                    failCallback()
                }
            }
        }
    }

}

class MainActivityViewModelFactory(
    private val rockyService: RockyService,
    private val registrationDao: RegistrationDao,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return MainActivityViewModel(dispatchers, rockyService, registrationDao, sharedPreferences) as T
    }
}
package com.rockthevote.grommet.ui

import androidx.lifecycle.*
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import timber.log.Timber

class MainActivityViewModel(
    private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
    private val rockyService: RockyService,
    private val registrationDao: RegistrationDao
) : ViewModel() {

    private val _state = MutableLiveData<MainActivityState>(MainActivityState.Init)
    val state: LiveData<MainActivityState> = _state

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    init {
        refreshPendingUploads()
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
                    !registrationPair.second.await().isError
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

    private suspend fun loadRequestsFromDb() = registrationDao.getAll()

    private fun updateState(newState: MainActivityState) {
        Timber.d("Handling new state: $newState")
        _state.postValue(newState)
    }

    override fun onCleared() {
        super.onCleared()
        supervisorJob.cancelChildren()
    }
}

class MainActivityViewModelFactory(
    private val rockyService: RockyService,
    private val registrationDao: RegistrationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return MainActivityViewModel(dispatchers, rockyService, registrationDao) as T
    }
}
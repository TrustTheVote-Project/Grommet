package com.rockthevote.grommet.ui.eventFlow

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.*
import com.google.android.gms.location.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.R
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.api.model.ApiGeoLocation
import com.rockthevote.grommet.data.api.model.CreateShiftBody
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Mechanical Man on 5/25/20.
 */

class CanvasserInfoViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao,
        private val fusedLocationProviderClient: FusedLocationProviderClient,
        private val rockyService: RockyService
) : ViewModel() {

    val canvasserInfoData = Transformations.map(partnerInfoDao.getPartnerInfoWithSession()) { result ->
        result?.let {
            CanvasserInfoData(
                    partnerInfoId = result.partnerInfo?.partnerInfoId ?: 0,
                    partnerName = result.partnerInfo?.partnerName ?: "",
                    canvasserName = result.session?.canvasserName ?: "",
                    openTrackingId = result.session?.openTrackingId ?: "",
                    partnerTrackingId = result.session?.partnerTrackingId ?: "",
                    deviceId = result.session?.deviceId ?: "",
                    locations = result.partnerInfo?.validLocations ?: ArrayList(),
                    canvasserPhone = result.session?.canvasserPhone ?: "",
                    canvasserLastName = result.session?.canvasserLastName ?: "",
                    canvasserMail = result.session?.canvasserEmail ?: ""
            )
        } ?: run {
            CanvasserInfoData()
        }
    }

    private val _statusState = LiveEvent<StartCollectionEvent>()
    val statusState: LiveData<StartCollectionEvent> = _statusState

    private val _effect = LiveEvent<CanvasserInfoState.Effect?>()
    val effect: LiveData<CanvasserInfoState.Effect?> = _effect

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Error getting data")
        updateEffect(CanvasserInfoState.Error)
    }


    private class CanvasserInfoViewModelException(msg: String) : Exception(msg)


    fun clearSession() = viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
        sessionDao.clearAllSessionInfo()
    }

    fun startCollection(date: Date) {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            runCatching {
                viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
                    val session = sessionDao.getCurrentSession() ?: return@launch
                    val newSessionData = session.copy(clockInTime = date)
                    sessionDao.updateSession(newSessionData)
                }

            }.onSuccess {
                updateStatusState(StartCollectionEvent.Done());
            }.onFailure {
                updateStatusState(StartCollectionEvent.Error(R.string.error_title))
            }
        }
    }


    private fun updateStatusState(value: StartCollectionEvent) {
        _statusState.postValue(value)
    }

    @SuppressLint("MissingPermission")
    fun createShift(canvasserName: String,
                    canvasserLastName: String,
                    partnerTrackingId: String,
                    openTrackingId: String,
                    email: String,
                    phone: String,
                    deviceId: String) {


        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {

            val data = canvasserInfoData.value ?: CanvasserInfoData()
            var location: Location? = null

            val createShift = CreateShiftBody.builder()
                    .partnerId(data.partnerInfoId.toInt())
                    .shiftLocation(Integer.parseInt(partnerTrackingId))
                    .canvasserFirstName(canvasserName)
                    .canvasserLastName(canvasserLastName)
                    .canvasserEmail(email)
                    .canvasserPhone(phone)
                    .deviceId(deviceId).build()
            Timber.e(createShift.toString())

            if (canvasserName == data.canvasserName
                    && partnerTrackingId == data.partnerTrackingId
                    && openTrackingId == data.openTrackingId
                    && deviceId == data.deviceId
            ) {
                // nothing changed, don't update the database, just continue
                updateEffect(CanvasserInfoState.Success)
            } else {
                runCatching {
                    val result = rockyService.createShift(createShift).toBlocking().value()
                    location = fusedLocationProviderClient.getLocation()

                    if (result.isError) {
                        throw result.error()
                                ?: CanvasserInfoViewModelException("Error retrieving result")
                    } else {
                        result.response()?.body()
                                ?: throw CanvasserInfoViewModelException("Successful result with empty body received")
                    }
                }.onSuccess {
                    Timber.e(it.shiftId().toString())
                    sessionDao.clearAllSessionInfo()
                    Timber.e(canvasserLastName)
                    Timber.e(canvasserName)
                    sessionDao.insert(
                            Session(
                                    partnerInfoId = canvasserInfoData.value?.partnerInfoId ?: 0,
                                    canvasserName = canvasserName,
                                    sourceTrackingId = canvasserName + Calendar.getInstance().timeInMillis,
                                    partnerTrackingId = partnerTrackingId,
                                    canvasserLastName = canvasserLastName,
                                    geoLocation = ApiGeoLocation.builder()
                                            .latitude(location?.latitude!!)
                                            .longitude(location?.longitude!!)
                                            .build(),
                                    openTrackingId = openTrackingId,
                                    deviceId = deviceId,
                                    canvasserEmail = email,
                                    canvasserPhone = phone,
                                    shiftId = it.shiftId()
                            )
                    )
                    updateEffect(CanvasserInfoState.Success)
                }.onFailure {
                    Timber.d(it, "failure updating canvasser info")

                    val effect = when (it) {
                        is LocationException -> CanvasserInfoState.Error
                        is CanvasserInfoViewModelException -> CanvasserInfoState.NetworkError
                        else -> CanvasserInfoState.Error
                    }
                    updateEffect(effect)
                }
            }

        }
    }

    private fun updateEffect(newEffect: CanvasserInfoState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }
}

class CanvasserInfoViewModelFactory(
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao,
        private val fusedLocationProviderClient: FusedLocationProviderClient,
        private val rockyService: RockyService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return CanvasserInfoViewModel(
                dispatchers,
                partnerInfoDao,
                sessionDao,
                fusedLocationProviderClient,
                rockyService) as T
    }
}

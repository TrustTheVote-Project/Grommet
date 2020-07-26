package com.rockthevote.grommet.ui.eventFlow

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.*
import com.google.android.gms.location.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.api.model.ApiGeoLocation
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

/**
 * Created by Mechanical Man on 5/25/20.
 */

class CanvasserInfoViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao,
        private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {

    val canvasserInfoData = Transformations.map(partnerInfoDao.getPartnerInfoWithSession()) { result ->
        result?.let {
            CanvasserInfoData(
                    result.partnerInfo?.partnerInfoId ?: 0,
                    result.partnerInfo?.partnerName ?: "",
                    result.session?.canvasserName ?: "",
                    result.session?.openTrackingId ?: "",
                    result.session?.partnerTrackingId ?: "",
                    result.session?.deviceId ?: ""
            )
        } ?: run {
            CanvasserInfoData()
        }
    }

    private val _effect = LiveEvent<CanvasserInfoState.Effect?>()
    val effect: LiveData<CanvasserInfoState.Effect?> = _effect

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Error getting data")
        updateEffect(CanvasserInfoState.Error)
    }

    @SuppressLint("MissingPermission")
    fun updateCanvasserInfo(canvasserName: String,
                            partnerTrackingId: String,
                            openTrackingId: String,
                            deviceId: String) {


        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {

            val data = canvasserInfoData.value ?: CanvasserInfoData()

            if (canvasserName == data.canvasserName
                    && partnerTrackingId == data.partnerTrackingId
                    && openTrackingId == data.openTrackingId
                    && deviceId == data.deviceId
            ) {
                // nothing changed, don't update the database, just continue
                updateEffect(CanvasserInfoState.Success)
            } else {
                runCatching {

                    val location: Location = fusedLocationProviderClient.getLocation()

                    sessionDao.clearAllSessionInfo()
                    sessionDao.insert(
                            Session(
                                    partnerInfoId = canvasserInfoData.value?.partnerInfoId ?: 0,
                                    canvasserName = canvasserName,
                                    sourceTrackingId = canvasserName + Calendar.getInstance().timeInMillis,
                                    partnerTrackingId = partnerTrackingId,
                                    geoLocation = ApiGeoLocation.builder()
                                            .latitude(location.latitude)
                                            .longitude(location.longitude)
                                            .build(),
                                    openTrackingId = openTrackingId,
                                    deviceId = deviceId
                            )
                    )
                }.onSuccess {
                    updateEffect(CanvasserInfoState.Success)
                }.onFailure {
                    Timber.d(it, "failure updating canvasser info")

                   val effect = when (it) {
                        is LocationException -> CanvasserInfoState.Error
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
        private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return CanvasserInfoViewModel(
                dispatchers,
                partnerInfoDao,
                sessionDao,
                fusedLocationProviderClient) as T
    }
}

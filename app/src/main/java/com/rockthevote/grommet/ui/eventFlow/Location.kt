package com.rockthevote.grommet.ui.eventFlow

import android.annotation.SuppressLint
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

private val LOCATION_TIMEOUT = TimeUnit.SECONDS.toMillis(5)

/**
 * Attempts to get a location by first retrieiving the last known location. If that location
 * is unavailable, tries to get a single location actively
 *
 * Throws [LocationException] if unable to retrieve a location
 */
@Throws(LocationException::class)
@RequiresPermission(anyOf = ["android.permission.ACCESS_FINE_LOCATION"])
suspend fun FusedLocationProviderClient.getLocation(): Location = coroutineScope {

    val timeoutJob = Job()

    val timeoutContext = coroutineContext + timeoutJob

    val deferredLocation: Deferred<Location> = async {

        // <Location> is important here, lint says it's unneeded, the compiler thinks otherwise
        suspendCancellableCoroutine<Location> { cont ->
            lastLocation.addOnCompleteListener {
                val result = it.result
                if (result != null) {
                    cont.resume(result)
                    timeoutJob.cancel()
                } else {
                    attemptForceLocationUpdate(cont, timeoutJob)
                }

            }
        }
    }

    // If for some reason a location result doesn't manifest in
    launch(timeoutContext) {
        delay(LOCATION_TIMEOUT)
        throw LocationException("Unable to find location within timeout")
    }

    deferredLocation.await()
}

@RequiresPermission(anyOf = ["android.permission.ACCESS_FINE_LOCATION"])
private fun FusedLocationProviderClient.attemptForceLocationUpdate(cont: CancellableContinuation<Location>, timeoutJob: CompletableJob) {
    val callback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            Timber.d("Location result: $locationResult")

            if (locationResult != null) {
                cont.resume(locationResult.lastLocation)
            } else {
                cont.cancel(LocationException("Location not available, empty result"))
            }

            timeoutJob.cancel()
        }

        override fun onLocationAvailability(availability: LocationAvailability?) {
            super.onLocationAvailability(availability)

            if (availability?.isLocationAvailable == false) {
                Timber.d("Location is unavailable")
                cont.cancel(LocationException("Location not available"))
            }
        }
    }
    requestLocationUpdates(
            LocationRequest.create()
                    .setInterval(500)
                    .setFastestInterval(0)
                    .setMaxWaitTime(0)
                    .setNumUpdates(1)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
            callback,
            null
    )
}

class LocationException(msg: String) : Exception(msg)
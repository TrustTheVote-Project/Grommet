package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.data.db.model.GeoLocation

/**
 * Session data necessary to construct the rocky request JSON object
 */
data class SessionData(
    val partnerId: Long,
    val canvasserName: String,
    val sourceTrackingId: String,
    val partnerTrackingId: String,
    val geoLocation: GeoLocation,
    val openTrackingId: String
)
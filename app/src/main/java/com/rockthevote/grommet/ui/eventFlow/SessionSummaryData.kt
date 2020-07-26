package com.rockthevote.grommet.ui.eventFlow

import com.rockthevote.grommet.data.db.model.Registration
import com.rockthevote.grommet.data.db.model.SessionStatus
import java.util.*

/**
 * Created by Mechanical Man on 5/30/20.
 */
data class SessionSummaryData(
        val partnerName: String = "",
        val canvasserName: String = "",
        val openTrackingId: String = "", // the location
        val partnerTrackingId: String = "", // the zip code
        val deviceId: String = "", // the tablet number
        val smsCount: Int = 0,
        val dlnCount: Int = 0,
        val ssnCount: Int = 0,
        val emailOptInCount: Int = 0,
        val totalRegistrations: Int = 0,
        val abandonedRegistrations: Int = 0,
        val registrations: List<Registration?> = emptyList(),
        val clockInTime: Date? = null,
        val clockOutTime: Date? = null
)
package com.rockthevote.grommet.ui.eventFlow

import com.rockthevote.grommet.data.api.model.ValidLocation

/**
 * Created by Mechanical Man on 5/25/20.
 */
data class CanvasserInfoData(
        val partnerInfoId: Long = 0,
        val partnerName: String = "",
        val canvasserName: String = "",
        val canvasserLastName: String = "",
        val canvasserPhone: String = "",
        val canvasserMail: String = "",
        val openTrackingId: String = "", // the location
        val partnerTrackingId: String = "", // the zip code
        val deviceId: String = "", // the tablet number
        val locations: ArrayList<ValidLocation> =  ArrayList<ValidLocation>()
)
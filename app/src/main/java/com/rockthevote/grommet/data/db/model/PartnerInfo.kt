package com.rockthevote.grommet.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText
import java.util.*

/**
 * Created by Mechanical Man on 4/12/20.
 */
@Entity(tableName = "partner_info")
data class PartnerInfo(
        @PrimaryKey
        @ColumnInfo(name = "partner_info_id")
        val partnerInfoId: Long = 0,

        // the key to login as this partner
        @ColumnInfo(name = "partner_id")
        val partnerId: Long,

        @ColumnInfo(name = "app_version")
        val appVersion: Float,

        @ColumnInfo(name = "is_valid")
        val isValid: Boolean,

        @ColumnInfo(name = "partner_name")
        val partnerName: String,

        @ColumnInfo(name = "registration_deadline_date")
        val registrationDeadlineDate: Date,

        @ColumnInfo(name = "registration_notification_text")
        val registrationNotificationText: RegistrationNotificationText,

        @ColumnInfo(name = "volunteer_text")
        val volunteerText: PartnerVolunteerText
)
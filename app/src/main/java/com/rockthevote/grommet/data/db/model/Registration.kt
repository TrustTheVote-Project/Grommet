package com.rockthevote.grommet.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity for registrations in the database. Autogenerates a [registrationId], taking in
 * full json string as [registrationData]. Retains [uploadAttempts] for error handling
 * purposes.
 */
@Entity(tableName = "registration"//, TODO Readd foreign key requirements
//        foreignKeys = [ForeignKey(
//                entity = Session::class,
//                parentColumns = ["session_id"],
//                childColumns = ["session_id"],
//                onDelete = ForeignKey.CASCADE
//        )]
)
data class Registration(
        @PrimaryKey(autoGenerate = true)
        val registrationId: Long = 0,
        @ColumnInfo(name = "session_id")
        val sessionId: Long = 0, // todo remove default
        @ColumnInfo(name = "registration_data")
        val registrationData: String,
        @ColumnInfo(name = "upload_attempts")
        val uploadAttempts: Int = 0
)
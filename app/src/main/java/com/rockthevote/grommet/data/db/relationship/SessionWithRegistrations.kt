package com.rockthevote.grommet.data.db.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.rockthevote.grommet.data.db.model.Registration
import com.rockthevote.grommet.data.db.model.Session

/**
 * Created by Mechanical Man on 5/3/20.
 */
data class SessionWithRegistrations(
        @Embedded val session: Session?,
        @Relation(
                parentColumn = "session_id",
                entityColumn = "session_id"
        )
        val registrations: List<Registration?>
)
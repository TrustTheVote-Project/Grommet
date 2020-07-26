package com.rockthevote.grommet.data.db.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.data.db.model.Session

/**
 * Created by Mechanical Man on 5/3/20.
 */
data class PartnerInfoWithSession(
        @Embedded val partnerInfo: PartnerInfo?,
        @Relation(
                parentColumn = "partner_info_id",
                entityColumn = "partner_info_id"
        )
        val session: Session?
)
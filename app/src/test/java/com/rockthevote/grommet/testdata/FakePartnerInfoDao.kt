package com.rockthevote.grommet.testdata

import androidx.lifecycle.LiveData
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.data.db.relationship.PartnerInfoWithSession
import com.rockthevote.grommet.data.db.relationship.PartnerInfoWithSessionAndRegistrations

class FakePartnerInfoDao(
        private val currentPartnerInfo: PartnerInfo
) : PartnerInfoDao {

    override fun getCurrentPartnerInfoLive(): LiveData<PartnerInfo> {
        TODO("Not yet implemented")
    }

    override fun getPartnerInfo(partnerInfoId: Long?): PartnerInfo {
        return currentPartnerInfo
    }

    override fun getCurrentPartnerInfo(): PartnerInfo {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<PartnerInfo> {
        TODO("Not yet implemented")
    }

    override fun getPartnerInfoWithSession(): LiveData<PartnerInfoWithSession?> {
        TODO("Not yet implemented")
    }

    override fun getPartnerInfoWithSessionAndRegistrations(): LiveData<PartnerInfoWithSessionAndRegistrations?> {
        TODO("Not yet implemented")
    }

    override fun insertPartnerInfo(partnerInfo: PartnerInfo) {
        TODO("Not yet implemented")
    }

    override fun deleteAllPartnerInfo() {
        TODO("Not yet implemented")
    }


}
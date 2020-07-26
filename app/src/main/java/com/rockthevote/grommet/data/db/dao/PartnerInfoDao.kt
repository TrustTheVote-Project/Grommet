package com.rockthevote.grommet.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.data.db.relationship.PartnerInfoWithSession
import com.rockthevote.grommet.data.db.relationship.PartnerInfoWithSessionAndRegistrations

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface PartnerInfoDao {
    @Query("SELECT * FROM partner_info LIMIT 1")
    fun getCurrentPartnerInfoLive(): LiveData<PartnerInfo>

    @Query("SELECT * FROM partner_info WHERE partner_info_id = :partnerInfoId LIMIT 1")
    fun getPartnerInfo(partnerInfoId: Long?): PartnerInfo

    @Query("SELECT * FROM partner_info LIMIT 1")
    fun getCurrentPartnerInfo(): PartnerInfo

    @Query("SELECT * FROM partner_info")
    fun getAll(): List<PartnerInfo>

    @Transaction
    @Query("SELECT * FROM partner_info")
    fun getPartnerInfoWithSession(): LiveData<PartnerInfoWithSession?>

    @Transaction
    @Query("SELECT * FROM partner_info")
    fun getPartnerInfoWithSessionAndRegistrations(): LiveData<PartnerInfoWithSessionAndRegistrations?>

    @Insert(onConflict = IGNORE)
    fun insertPartnerInfo(partnerInfo: PartnerInfo)

    @Query("DELETE FROM partner_info")
    fun deleteAllPartnerInfo()


}
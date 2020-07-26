package com.rockthevote.grommet.data.db.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrations
import com.rockthevote.grommet.data.db.relationship.PartnerInfoWithSessionAndRegistrations

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface SessionDao {
    @Query("SELECT * FROM session")
    fun getAll(): List<Session>

    @Query("SELECT * FROM session LIMIT 1")
    fun getCurrentSession(): Session?

    @Query("SELECT * FROM session LIMIT 1")
    fun getCurrentSessionLive(): LiveData<Session?>

    @Transaction
    @Query("SELECT * FROM session LIMIT 1")
    fun getSessionWithRegistrations(): LiveData<SessionWithRegistrations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg session: Session)

    @Query("DELETE FROM session")
    fun clearAllSessionInfo()

    @Update
    fun updateSession(session: Session)
}
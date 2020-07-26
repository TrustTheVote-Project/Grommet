package com.rockthevote.grommet.data.db.dao

import androidx.room.*
import com.rockthevote.grommet.data.db.model.Registration

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registration")
    suspend fun getAll(): List<Registration>

    @Insert
    suspend fun insert(vararg registrations: Registration)

    @Delete
    suspend fun delete(vararg registrations: Registration)

    @Update
    suspend fun update(registration: Registration)
}


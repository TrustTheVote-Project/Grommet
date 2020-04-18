package com.rockthevote.grommet.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.Registration

/**
 * Created by Mechanical Man on 3/24/20.
 */

@Database(entities = [Registration::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun registrationDao(): RegistrationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance
                    ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "grommet_db"
                ).build()
            }
        }
    }
}
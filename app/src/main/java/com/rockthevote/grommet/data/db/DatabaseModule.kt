package com.rockthevote.grommet.data.db

import android.app.Application
import com.rockthevote.grommet.data.db.AppDatabase.Companion.getInstance
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
        injects = [BaseRegistrationFragment::class],
        complete = false,
        library = true
)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return getInstance(application)
    }

    @Provides
    fun provideRegistrationDao(db: AppDatabase) = db.registrationDao()

    @Provides
    fun provideSessionDao(db: AppDatabase) = db.sessionDao()

    @Provides
    fun providePartnerInfo(db: AppDatabase) = db.partnerInfoDao()
}
package com.rockthevote.grommet.data.db

import android.app.Application
import com.rockthevote.grommet.data.IsInstrumentationTest
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module(
        injects = [BaseRegistrationFragment::class],
        complete = false,
        library = true,
        overrides = true
)
class DebugDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@IsInstrumentationTest isInstrumentationTest: Boolean,
                           application: Application): AppDatabase { // Return an in-memory DB for testing
        return if (isInstrumentationTest) {
            TestAppDatabase.getInstance(application)
        } else {
            AppDatabase.getInstance(application)
        }
    }

}
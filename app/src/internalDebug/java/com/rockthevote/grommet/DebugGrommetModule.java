package com.rockthevote.grommet;

import com.rockthevote.grommet.data.DebugDataModule;
import com.rockthevote.grommet.ui.DebugUiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = GrommetModule.class,
        includes = {
                DebugUiModule.class,
                DebugDataModule.class,
                DebugActionsModule.class
        },
        overrides = true
)
public final class DebugGrommetModule {
    // Low-tech flag to force certain debug build behaviors when running in an instrumentation test.
    // This value is used in the creation of singletons so it must be set before the graph is created.
    static boolean instrumentationTest = false;

    @Provides
    @Singleton
    @IsInstrumentationTest boolean provideIsInstrumentationTest() {
        return instrumentationTest;
    }
}

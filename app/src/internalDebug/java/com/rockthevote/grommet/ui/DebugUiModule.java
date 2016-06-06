package com.rockthevote.grommet.ui;

import com.rockthevote.grommet.IsInstrumentationTest;
import com.rockthevote.grommet.ui.debug.DebugView;
import com.rockthevote.grommet.ui.debug.DebugViewContainer;
import com.rockthevote.grommet.ui.debug.SocketActivityHierarchyServer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                DebugViewContainer.class,
                DebugView.class
        },
        complete = false,
        library = true,
        overrides = true
)
public class DebugUiModule {
    @Provides
    @Singleton
    ViewContainer provideViewContainer(DebugViewContainer debugViewContainer,
                                       @IsInstrumentationTest boolean isInstrumentationTest) {
        // Do not add the debug controls for when we are running inside of an instrumentation test.
        return isInstrumentationTest ? ViewContainer.DEFAULT : debugViewContainer;
    }

    @Provides
    @Singleton
    ActivityHierarchyServer provideActivityHierarchyServer() {
        return new SocketActivityHierarchyServer();
    }
}

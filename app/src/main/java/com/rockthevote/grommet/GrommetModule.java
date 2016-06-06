package com.rockthevote.grommet;

import android.app.Application;

import com.rockthevote.grommet.data.DataModule;
import com.rockthevote.grommet.ui.UiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                UiModule.class,
                DataModule.class
        },
        injects = {
                GrommetApp.class
        }
)
public final class GrommetModule {
    private final GrommetApp app;

    public GrommetModule(GrommetApp app) {
        this.app = app;
    }

    @Provides @Singleton Application provideApplication() { return app;}
}

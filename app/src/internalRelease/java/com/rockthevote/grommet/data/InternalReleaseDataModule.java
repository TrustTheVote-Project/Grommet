package com.rockthevote.grommet.data;

import androidx.appcompat.app.AppCompatActivity;

import com.rockthevote.grommet.data.api.InternalReleaseApiModule;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Mechanical Man, LLC on 8/1/17. Grommet
 */

@Module(
        includes = InternalReleaseApiModule.class,
        overrides = true,
        library = true,
        complete = false
)
public final class InternalReleaseDataModule {

    @Provides
    @Singleton
    HockeyAppHelper provideHockeyAppHelper() {
        return new HockeyAppHelper() {
            @Override
            public void checkForUpdates(AppCompatActivity activity) {
                UpdateManager.register(activity);
            }

            @Override
            public void checkForCrashes(AppCompatActivity activity) {
                CrashManager.register(activity);
            }

            @Override
            public void unRegister() {
                UpdateManager.unregister();
            }
        };
    }
}

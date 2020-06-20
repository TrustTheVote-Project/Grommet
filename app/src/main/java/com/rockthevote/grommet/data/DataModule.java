package com.rockthevote.grommet.data;

import android.app.Application;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.rockthevote.grommet.data.api.ApiModule;
import com.rockthevote.grommet.data.api.RockyAdapterFactory;
import com.rockthevote.grommet.data.api.StringNormalizerFactory;
import com.rockthevote.grommet.data.api.model.DateAdapter;
import com.rockthevote.grommet.ui.MainActivity;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.squareup.moshi.Moshi;

import java.io.File;

import javax.inject.Singleton;

import androidx.appcompat.app.AppCompatActivity;
import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import static android.content.Context.MODE_PRIVATE;
import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;


@Module(
        includes = {
                ApiModule.class,
        },
        injects = {
                MainActivity.class,
                BaseRegistrationFragment.class
        },
        complete = false,
        library = true
)
public final class DataModule {
    static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return app.getSharedPreferences("grommet", MODE_PRIVATE);
    }

    @Provides
    @Singleton
    FusedLocationProviderClient provideFusedLocationProviderClient(Application app) {
        return LocationServices.getFusedLocationProviderClient(app);
    }

    @Provides
    @Singleton
    RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides
    @Singleton
    Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new StringNormalizerFactory())
                .add(RockyAdapterFactory.create())
                .add(new DateAdapter())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app).build();
    }

    @Provides
    @Singleton
    HockeyAppHelper provideHockeyAppHelper() {
        return new HockeyAppHelper() {
            @Override
            public void checkForUpdates(AppCompatActivity activity) {
                // do nothing
            }

            @Override
            public void checkForCrashes(AppCompatActivity activity) {
                // do nothing
            }

            @Override
            public void unRegister() {
                // do nothing
            }
        };
    }

    static OkHttpClient.Builder createOkHttpClient(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache);
    }
}

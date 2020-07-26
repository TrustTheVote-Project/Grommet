package com.rockthevote.grommet.data.api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;

/**
 * Created by Mechanical Man, LLC on 8/1/17. Grommet
 */

@Module(
        complete = false,
        library = true,
        overrides = true
)
public final class InternalReleaseApiModule {
    public static final HttpUrl STAGING_API_URL = HttpUrl.parse("https://staging.rocky.rockthevote.com/api/v4/");

    @Provides
    @Singleton
    HttpUrl provideBaseUrl() {
        return STAGING_API_URL;
    }

}

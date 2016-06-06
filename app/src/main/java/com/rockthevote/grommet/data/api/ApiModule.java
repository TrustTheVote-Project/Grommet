package com.rockthevote.grommet.data.api;

import com.squareup.moshi.Moshi;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module(
        complete = false,
        library = true,
        injects = {
//                OauthService.class
        }
)
public final class ApiModule {
    public static final HttpUrl PRODUCTION_API_URL = HttpUrl.parse("https://api.rocky.gov/");

    @Provides @Singleton HttpUrl provideBaseUrl() {
        return PRODUCTION_API_URL;
    }

    @Provides @Singleton @Named("Api") OkHttpClient provideApiClient(OkHttpClient client) {
        return createApiClient(client).build();
    }

    @Provides @Singleton Retrofit provideRetroFit(HttpUrl baseUrl, @Named("Api")OkHttpClient client,
                                                  Moshi moshi){
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi)) //
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //
                .build();
    }

    @Provides @Singleton RockyService provideRockyService(Retrofit retrofit) {
        return retrofit.create(RockyService.class);
    }

    // add the O-Auth interceptor here
    static OkHttpClient.Builder createApiClient(OkHttpClient client) {
        return client.newBuilder();
    }
}

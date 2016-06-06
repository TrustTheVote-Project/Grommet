package com.rockthevote.grommet.data.api;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.data.ApiEndpoint;
import com.rockthevote.grommet.data.IsMockMode;
import com.rockthevote.grommet.data.NetworkDelay;
import com.rockthevote.grommet.data.NetworkFailurePercent;
import com.rockthevote.grommet.data.NetworkVariancePercent;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Module(
        complete = false,
        library = true,
        overrides = true
)
public final class DebugApiModule {
    @Provides
    @Singleton
    HttpUrl provideHttpUrl(@ApiEndpoint Preference<String> apiEndpoint) {
        return HttpUrl.parse(apiEndpoint.get());
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").v(message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        return loggingInterceptor;
    }

    @Provides
    @Singleton
    @Named("Api")
    OkHttpClient provideApiClient(OkHttpClient client,
                                  HttpLoggingInterceptor loggingInterceptor) {
        return ApiModule.createApiClient(client)
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Provides
    @Singleton
    NetworkBehavior provideNetworkBehavior(@NetworkDelay Preference<Long> networkDelay,
                                           @NetworkFailurePercent Preference<Integer> networkFailurePercent,
                                           @NetworkVariancePercent Preference<Integer> networkVariancePercent) {
        NetworkBehavior behavior = NetworkBehavior.create();
        behavior.setDelay(networkDelay.get(), MILLISECONDS);
        behavior.setFailurePercent(networkFailurePercent.get());
        behavior.setVariancePercent(networkVariancePercent.get());
        return behavior;
    }

    @Provides
    @Singleton
    MockRetrofit provideMockRetrofit(Retrofit retrofit, NetworkBehavior networkBehavior) {
        return new MockRetrofit.Builder(retrofit)
                .networkBehavior(networkBehavior)
                .build();
    }

    @Provides
    @Singleton
    RockyService provideRockyService(Retrofit retrofit, @IsMockMode boolean isMockMode,
                                     MockRockyService mockRockyService) {
        return isMockMode ? mockRockyService : retrofit.create(RockyService.class);
    }
}

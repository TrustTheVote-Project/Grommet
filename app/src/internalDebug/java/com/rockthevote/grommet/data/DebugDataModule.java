package com.rockthevote.grommet.data;

import android.app.Application;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.CustomRxSharedPreferences;
import com.rockthevote.grommet.GrommetApp;
import com.rockthevote.grommet.data.api.DebugApiModule;
import com.rockthevote.grommet.data.prefs.InetSocketAddressPreferenceAdapter;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import androidx.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module(
        includes = DebugApiModule.class,
        complete = false,
        library = true,
        overrides = true,
        injects = {GrommetApp.class}
)

public final class DebugDataModule {
    private static final int DEFAULT_ANIMATION_SPEED = 1; // 1x (normal) speed.
    private static final boolean DEFAULT_PICASSO_DEBUGGING = false; // Debug indicators displayed
    private static final boolean DEFAULT_PIXEL_GRID_ENABLED = false; // No pixel grid overlay.
    private static final boolean DEFAULT_PIXEL_RATIO_ENABLED = false; // No pixel ratio overlay.
    private static final boolean DEFAULT_SCALPEL_ENABLED = false; // No crazy 3D view tree.
    private static final boolean DEFAULT_SCALPEL_WIREFRAME_ENABLED = false; // Draw views by default.
    private static final boolean DEFAULT_SEEN_DEBUG_DRAWER = false; // Show debug drawer first time.
    private static final boolean DEFAULT_CAPTURE_INTENTS = true; // Capture external intents.

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
//        return app.getSharedPreferences("grommet", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    CustomRxSharedPreferences provideCustomRxSharedPreferences(SharedPreferences prefs) {
        return CustomRxSharedPreferences.create(prefs);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app,
                                     Preference<InetSocketAddress> networkProxyAddress) {
        return DataModule.createOkHttpClient(app)
                .sslSocketFactory(createBadSslSocketFactory())
                .proxy(InetSocketAddressPreferenceAdapter.createProxy(networkProxyAddress.get()))
                .build();
    }

    @Provides
    @Singleton
    @ApiEndpoint
    Preference<String> provideEndpointPreference(CustomRxSharedPreferences preferences) {
        return preferences.getString("debug_endpoint", ApiEndpoints.MOCK_MODE.url);
    }

    @Provides
    @Singleton
    @IsMockMode
    boolean provideIsMockMode(@ApiEndpoint Preference<String> endpoint,
                              @IsInstrumentationTest boolean isInstrumentationTest) {
        // Running in an instrumentation forces mock mode.
        return isInstrumentationTest || ApiEndpoints.isMockMode(endpoint.get());
    }

    @Provides
    @Singleton
    @NetworkDelay
    Preference<Long> provideNetworkDelay(CustomRxSharedPreferences preferences) {
        return preferences.getLong("debug_network_delay", 2000l);
    }

    @Provides
    @Singleton
    @NetworkFailurePercent
    Preference<Integer> provideNetworkFailurePercent(CustomRxSharedPreferences preferences) {
        return preferences.getInteger("debug_network_failure_percent", 3);
    }

    @Provides
    @Singleton
    @NetworkVariancePercent
    Preference<Integer> provideNetworkVariancePercent(CustomRxSharedPreferences preferences) {
        return preferences.getInteger("debug_network_variance_percent", 40);
    }

    @Provides
    @Singleton
    Preference<InetSocketAddress> provideNetworkProxyAddress(CustomRxSharedPreferences preferences) {

        return preferences.getObject(
                "debug_network_proxy",
                InetSocketAddressPreferenceAdapter.parse("default:0"),
                new InetSocketAddressPreferenceAdapter());
    }

    @Provides
    @Singleton
    @CaptureIntents
    Preference<Boolean> provideCaptureIntentsPreference(CustomRxSharedPreferences preferences) {
        return preferences.getBoolean("debug_capture_intents", DEFAULT_CAPTURE_INTENTS);
    }

    @Provides
    @Singleton
    @AnimationSpeed
    Preference<Integer> provideAnimationSpeed(CustomRxSharedPreferences preferences) {
        return preferences.getInteger("debug_animation_speed", DEFAULT_ANIMATION_SPEED);
    }


    @Provides
    @Singleton
    @SeenDebugDrawer
    Preference<Boolean> provideSeenDebugDrawer(CustomRxSharedPreferences preferences) {
        return preferences.getBoolean("debug_seen_debug_drawer", DEFAULT_SEEN_DEBUG_DRAWER);
    }

    private static SSLSocketFactory createBadSslSocketFactory() {
        try {
            // Construct SSLSocketFactory that accepts any cert.
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager permissive = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            context.init(null, new TrustManager[]{permissive}, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
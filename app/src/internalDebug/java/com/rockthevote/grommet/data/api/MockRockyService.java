package com.rockthevote.grommet.data.api;

import android.content.SharedPreferences;

import com.rockthevote.grommet.data.api.model.RegistrationResponse;
import com.rockthevote.grommet.data.api.model.Voter;
import com.rockthevote.grommet.util.EnumPreferences;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.Query;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.Calls;
import retrofit2.mock.MockRetrofit;
import rx.Observable;

@Singleton
public final class MockRockyService implements RockyService {

    private final BehaviorDelegate<RockyService> delegate;
    private final SharedPreferences preferences;
    private final Map<Class<? extends Enum<?>>, Enum<?>> responses = new LinkedHashMap<>();

    @Inject
    MockRockyService(MockRetrofit mockRetrofit, SharedPreferences preferences) {
        this.delegate = mockRetrofit.create(RockyService.class);
        this.preferences = preferences;

        //TODO create mock responses
        loadResponse(MockRegistrationResponse.class, MockRegistrationResponse.SUCCESS);

    }

    /**
     * Initializes the current response for {@code responseClass} from {@code SharedPreferences}, or
     * uses {@code defaultValue} if a response was not found.
     */
    private <T extends Enum<T>> void loadResponse(Class<T> responseClass, T defaultValue) {
        responses.put(responseClass, EnumPreferences.getEnumValue(preferences, responseClass, //
                responseClass.getCanonicalName(), defaultValue));
    }

    public <T extends Enum<T>> T getResponse(Class<T> responseClass) {
        return responseClass.cast(responses.get(responseClass));
    }

    public <T extends Enum<T>> void setResponse(Class<T> responseClass, T value) {
        responses.put(responseClass, value);
        EnumPreferences.saveEnumValue(preferences, responseClass.getCanonicalName(), value);
    }

    @Override
    public Observable<Result> register(@Body Voter voter) {
        RegistrationResponse response = getResponse(MockRegistrationResponse.class).response;

        return delegate.returning(Calls.response(response)).register(voter);
    }
}

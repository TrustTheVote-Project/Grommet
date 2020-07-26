package com.rockthevote.grommet.data.api;

import android.content.SharedPreferences;

import com.rockthevote.grommet.data.api.model.ClockInRequest;
import com.rockthevote.grommet.data.api.model.ClockOutRequest;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationResponse;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.util.EnumPreferences;

import java.io.IOException;
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
import rx.Single;

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
        loadResponse(MockPartnerNameResponse.class, MockPartnerNameResponse.SUCCESS);
        loadResponse(MockClockInResponse.class, MockClockInResponse.SUCCESS);
        loadResponse(MockClockOutResponse.class, MockClockOutResponse.SUCCESS);
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
    public Single<Result<RegistrationResponse>> register(@Body RockyRequest rockyRequest) {
        RegistrationResponse response = getResponse(MockRegistrationResponse.class).response;

        return delegate.returning(Calls.response(response)).register(rockyRequest);
    }

    @Override
    public Single<Result<PartnerNameResponse>> getPartnerName(@Query("partner_id") String partnerId,
                                                              @Query("grommet_version") String version) {
        PartnerNameResponse response = getResponse(MockPartnerNameResponse.class).response;
        return delegate.returning(Calls.response(response)).getPartnerName(partnerId, version);
    }

    @Override
    public Single<Result<Void>> clockIn(@Body ClockInRequest clockInRequest) {
        MockClockInResponse response = getResponse(MockClockInResponse.class);
        switch (response) {
            case FAILURE:
                return delegate.returning(Calls.failure(new IOException("mock network failure"))).clockIn(clockInRequest);
            default:
                return delegate.returning(Calls.response("")).clockIn(clockInRequest);
        }
    }

    @Override
    public Single<Result<Void>> clockOut(@Body ClockOutRequest clockOutRequest) {
        MockClockOutResponse response = getResponse(MockClockOutResponse.class);
        switch (response) {
            case FAILURE:
                return delegate.returning(Calls.failure(new IOException("mock network failure"))).clockOut(clockOutRequest);
            default:
                return delegate.returning(Calls.response("")).clockOut(clockOutRequest);
        }
    }
}

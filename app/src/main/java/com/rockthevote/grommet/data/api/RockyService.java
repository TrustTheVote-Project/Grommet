package com.rockthevote.grommet.data.api;


import com.rockthevote.grommet.data.api.model.ClockInRequest;
import com.rockthevote.grommet.data.api.model.ClockOutRequest;
//import com.rockthevote.grommet.data.api.model.CreateShiftBody;
import com.rockthevote.grommet.data.api.model.CompleteShiftResponse;
import com.rockthevote.grommet.data.api.model.CreateShiftBody;
import com.rockthevote.grommet.data.api.model.CreateShiftResponse;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationResponse;
import com.rockthevote.grommet.data.api.model.UpdateShiftBody;
import com.rockthevote.grommet.data.api.model.validation.IsValidResponse;
import com.rockthevote.grommet.data.db.model.RockyRequest;

import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface RockyService {

    @POST("voterregistrationrequest")
    Single<Result<RegistrationResponse>> register(@Body RockyRequest rockyRequest);

    @GET("completeShift/{shift_id}/complete")
    Single<Result<CompleteShiftResponse>> completeShift(@Path("shift_id") String shiftId);

    @GET("partnerIdValidation")
    Single<Result<PartnerNameResponse>> getPartnerName(@Query("partner_id") String partnerId);

    @POST("clockIn")
    Single<Result<Void>> clockIn(@Body ClockInRequest clockInRequest);

    @POST("clockOut")
    Single<Result<Void>> clockOut(@Body ClockOutRequest clockOutRequest);

    @POST("canvassing_shifts")
    Single<Result<CreateShiftResponse>> createShift(@Body CreateShiftBody createShiftBody);

    @PUT("canvassing_shifts/{shift_id}")
//    Single<Result<Void>> updateShift(@Query("shift_id") String shiftId);
    Single<Result<Void>> updateShift(@Path("shift_id") String shiftId,@Body UpdateShiftBody body);

    @GET("validateVersion")
    Single<Result<IsValidResponse>> getValidateVersion(@Query("version") String version);
}

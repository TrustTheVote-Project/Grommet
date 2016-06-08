package com.rockthevote.grommet.data.api;


import com.rockthevote.grommet.data.api.model.Voter;

import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface RockyService {

    @POST("register")
    Observable<Result> register(@Body Voter Voter);
}

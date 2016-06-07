package com.rockthevote.grommet.data.api;


import retrofit2.adapter.rxjava.Result;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface RockyService {

    @POST("register")
    Observable<Result> register(@Query("name") String name);
}

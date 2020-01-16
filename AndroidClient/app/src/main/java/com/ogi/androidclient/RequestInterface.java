package com.ogi.androidclient;

import com.ogi.androidclient.models.ServerRequest;
import com.ogi.androidclient.models.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterface {
    @POST("LatihanAndroidLoginRegisterWithPhpMysql/")
    Call<ServerResponse> operation(@Body ServerRequest request);
}

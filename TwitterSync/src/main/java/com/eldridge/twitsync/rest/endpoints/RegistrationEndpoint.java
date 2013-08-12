package com.eldridge.twitsync.rest.endpoints;

import com.eldridge.twitsync.rest.endpoints.payload.RegistrationPayload;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by ryaneldridge on 8/9/13.
 */
public interface RegistrationEndpoint {

    @POST("/register/device")
    void registerDevice(@Body RegistrationPayload registrationPayload, Callback<Response> cb);

}

package com.eldridge.twitsync.rest.endpoints;

import com.eldridge.twitsync.rest.endpoints.payload.StatusUpdatePayload;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by ryaneldridge on 8/13/13.
 */
public interface StatusEndpoint {

    @POST("/status/update")
    void statusUpdate(@Body StatusUpdatePayload statusUpdatePayload, Callback<Response> response);

}

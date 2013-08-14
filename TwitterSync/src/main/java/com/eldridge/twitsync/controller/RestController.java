package com.eldridge.twitsync.controller;

import android.content.Context;

import retrofit.RestAdapter;

/**
 * Created by ryaneldridge on 8/9/13.
 */
public class RestController {

    private static final String TAG = RestController.class.getSimpleName();

    private static RestController instance;
    private Context context;

    //private static final String SERVER_URL = "http://10.0.1.6:3000";
    private static final String SERVER_URL = "http://192.168.241.104:8080";
    private static RestAdapter restAdapter;

    private RestController() {
        restAdapter = new RestAdapter.Builder().setServer(SERVER_URL).build();
    }

    public static RestController getInstance(Context context) {
        if (instance == null) {
            synchronized (RestController.class) {
                instance = new RestController();
                instance.context = context;
            }
        }
        return instance;
    }

    public RestAdapter getRestAdapter() {
        return restAdapter;
    }

}
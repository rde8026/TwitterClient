package com.eldridge.twitsync.controller;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.eldridge.twitsync.BuildConfig;
import com.eldridge.twitsync.rest.endpoints.RegistrationEndpoint;
import com.eldridge.twitsync.rest.endpoints.payload.RegistrationPayload;
import com.eldridge.twitsync.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ryaneldridge on 8/8/13.
 */
public class GcmController {

    private static final String TAG = GcmController.class.getSimpleName();

    private static GcmController instance;
    private Context context;

    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 14; //14 days

    private static final int THREAD_POOL_SIZE = 20;

    private static final String PROJECT_ID = "379401999507";

    private ExecutorService executorService;

    private String regId;

    private GcmController() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public static GcmController getInstance(Context context) {
        if (instance == null) {
            synchronized (GcmController.class) {
                instance = new GcmController();
                instance.context = context;
            }
        }
        return instance;
    }

    public void registerDevice() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    Long userId = PreferenceController.getInstance(context).getUserId();
                    if (userId != -1) {
                        regId = PreferenceController.getInstance(context).getRegistrationId();

                        int registeredVersion = PreferenceController.getInstance(context).getGcmRegisteredVersion();
                        int currentVersion = PreferenceController.getInstance(context).getAppVersion();

                        if ( regId.length() == 0 || registeredVersion != currentVersion || PreferenceController.getInstance(context).isRegistrationExpired() ) {
                            Log.d(TAG, "** Registration is null - attempting to register now **");
                            regId = registerGcm();

                            String uniqueDeviceId = Utils.getUniqueDeviceId(context);

                            RestAdapter adapter = RestController.getInstance(context).getRestAdapter();
                            RegistrationEndpoint registrationEndpoint = adapter.create(RegistrationEndpoint.class);
                            registrationEndpoint.registerDevice(new RegistrationPayload(regId, uniqueDeviceId, String.valueOf(userId)), new Callback<Response>() {
                                @Override
                                public void success(Response response, Response response2) {
                                    Log.d(TAG, "**** OK GOT A SUCCESS ****");
                                    PreferenceController.getInstance(context).saveGcmRegistration(regId);
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Log.d(TAG, "**** FAILURE ****");
                                    Log.e(TAG, "*** Retro Error: " + retrofitError.getMessage() + " ***");
                                }
                            });

                            long delta = System.currentTimeMillis() - startTime;
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "***** Device Registration took " + delta + " ms ******");
                            }

                        }

                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "****** UserId not set yet so we can NOT register the device at this point ******");
                        }
                    }
                } catch (IOException ioe) {
                    Log.e(TAG, "", ioe);
                }
            }
        });
    }

    private String registerGcm() throws IOException {
        return GoogleCloudMessaging.getInstance(context).register(PROJECT_ID);
    }

}

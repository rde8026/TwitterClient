package com.eldridge.twitsync.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.eldridge.twitsync.controller.TwitterApiController;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by ryaneldridge on 8/13/13.
 */
public class GcmIntentService extends IntentService {

    private static final String TAG = GcmIntentService.class.getSimpleName();

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        final Context context = this;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equalsIgnoreCase(messageType)) {
            Log.e(TAG, "** Got Error Message form GCM **");
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equalsIgnoreCase(messageType)) {
            Log.d(TAG, "** Deleted Message on Server ***");
        } else {
            if (extras != null && !extras.isEmpty()) {
                final String lastMessage = extras.getString("messageId");
                Log.d(TAG, "** Received Notification that another device has read further along - updating **");

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (!"".equalsIgnoreCase(lastMessage)) {
                            TwitterApiController.getInstance(context).syncFromGcm(Long.valueOf(lastMessage));
                        }
                        return null;
                    }
                }.execute();
            }
        }

    }
}

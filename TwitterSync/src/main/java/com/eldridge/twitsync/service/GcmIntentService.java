package com.eldridge.twitsync.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equalsIgnoreCase(messageType)) {
            Log.e(TAG, "** Got Error Message form GCM **");
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equalsIgnoreCase(messageType)) {
            Log.d(TAG, "** Deleted Message on Server ***");
        } else {
            if (extras != null && !extras.isEmpty()) {
                String lastMessage = extras.getString("messageId");
                Log.d(TAG, "***************** Last Message: " + lastMessage + " **********************");
            }
        }

    }
}

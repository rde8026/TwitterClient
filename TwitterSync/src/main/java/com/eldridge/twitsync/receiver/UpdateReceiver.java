package com.eldridge.twitsync.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by ryaneldridge on 8/8/13.
 */
public class UpdateReceiver extends BroadcastReceiver {

    private static final String TAG = UpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equalsIgnoreCase(messageType)) {
            Log.d(TAG, "** Send Error: " + intent.getExtras().toString() + " ***");
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equalsIgnoreCase(messageType)) {
            Log.d(TAG, "** Deleted Message on Server: " + intent.getExtras().toString() + " ***");
        } else {
            String msg = intent.getExtras().toString();
            Log.d(TAG, "**** Message from Server: " + msg + " *****");
        }

    }
}

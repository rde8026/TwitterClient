package com.eldridge.twitsync.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.eldridge.twitsync.service.GcmIntentService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by ryaneldridge on 8/8/13.
 */
public class UpdateReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = UpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}

package com.eldridge.twitsync.util;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by ryaneldridge on 8/13/13.
 */
public class Utils {

    public static String getUniqueDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}

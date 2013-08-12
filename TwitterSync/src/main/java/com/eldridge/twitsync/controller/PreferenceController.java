package com.eldridge.twitsync.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.sql.Timestamp;

/**
 * Created by ryaneldridge on 8/2/13.
 */
public class PreferenceController {

    private static final String TAG = PreferenceController.class.getSimpleName();

    private static PreferenceController instance;
    private Context context;

    private static final String AUTH_PREFERENCES = "AUTH_PREFERENCES";

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SECRET = "SECRET";
    public static final String USER_ID = "USER_ID";

    public static final String GCM_REG_TOKEN = "GCM_REG_TOKEN";
    public static final String GCM_PROPERTY_ON_SERVER_EXPIRATION_TIME = "PROPERTY_ON_SERVER_EXPIRATION_TIME";
    public static final String GCM_REGISTERED_VERSION = "REGISTERED_VERSION";

    private PreferenceController() {
    }

    public static PreferenceController getInstance(Context context) {
        if (instance == null) {
            synchronized (PreferenceController.class) {
                instance = new PreferenceController();
                instance.context = context;
            }
        }
        return instance;
    }


    public boolean checkForExistingCredentials() {
        SharedPreferences preferences = context.getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        String token = preferences.getString(ACCESS_TOKEN, "");
        String secret = preferences.getString(SECRET, "");

        if ( (token != null && !"".equalsIgnoreCase(token)) && (secret != null && !"".equalsIgnoreCase(secret)) ) {
            return true;
        }
        return false;
    }

    public boolean setAccessTokenAndSecret(String accessToken, String secret) {
        SharedPreferences preferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.putString(SECRET, secret);
        return editor.commit();
    }

    public String getAcessToken() {
        SharedPreferences preferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(ACCESS_TOKEN, "");
    }

    public String getSecret() {
        SharedPreferences preferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(SECRET, "");
    }

    public boolean setUserId(Long id) {
        SharedPreferences preferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(USER_ID, id);
        return editor.commit();
    }

    public Long getUserId() {
        SharedPreferences preferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getLong(USER_ID, -1);
    }

    public String getRegistrationId() {
        SharedPreferences preferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(GCM_REG_TOKEN, "");
    }

    private SharedPreferences getSharedPreferences(String name, int mode) {
        return context.getSharedPreferences(name, mode);
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public boolean isRegistrationExpired() {
        final SharedPreferences prefs = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        // checks if the information is not stale
        long expirationTime =
                prefs.getLong(GCM_PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }

    public int getGcmRegisteredVersion() {
        SharedPreferences sharedPreferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(GCM_REGISTERED_VERSION, -1);
    }

    public void saveGcmRegistration(String registrationId) {
        SharedPreferences sharedPreferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        int appVersion = getAppVersion();
        Log.d(TAG, "** Savign GCM Token for App Version " + appVersion + " **");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GCM_REG_TOKEN, registrationId);
        editor.putInt(GCM_REGISTERED_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis() + GcmController.REGISTRATION_EXPIRY_TIME_MS;
        Log.d(TAG, "** Setting registration expiry time to " + new Timestamp(expirationTime) + " **");
        editor.putLong(GCM_PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }

    public void clearGcmRegistration() {
        SharedPreferences sharedPreferences = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GCM_REG_TOKEN, "");
        editor.commit();
    }

}

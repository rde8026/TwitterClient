package com.eldridge.twitsync.controller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ryaneldridge on 8/2/13.
 */
public class PreferenceController {

    private static PreferenceController instance;
    private Context context;
    //Preference s
    private static final String AUTH_PREFERENCES = "AUTH_PREFERENCES";

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SECRET = "SECRET";
    public static final String USER_ID = "USER_ID";

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

    private SharedPreferences getSharedPreferences(String name, int mode) {
        return context.getSharedPreferences(name, mode);
    }

}

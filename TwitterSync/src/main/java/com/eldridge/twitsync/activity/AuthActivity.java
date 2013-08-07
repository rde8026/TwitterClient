package com.eldridge.twitsync.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.controller.TwitterRegisterController;
import com.eldridge.twitsync.fragment.LoadingFragment;
import com.eldridge.twitsync.message.beans.AuthUrlMessage;
import com.eldridge.twitsync.message.beans.AuthorizationCompleteMessage;
import com.eldridge.twitsync.message.beans.AuthorizationErrorMessage;
import com.squareup.otto.Subscribe;

import oauth.signpost.OAuth;

/**
 * Created by ryaneldridge on 8/2/13.
 */
public class AuthActivity extends SherlockFragmentActivity {

    private FragmentManager fragmentManager;

    public static final int AUTHORIZATION_SUCCESS = 1000;
    public static final int AUTHORIZATION_FAILURE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(android.R.id.content, new LoadingFragment());
        ft.commit();
        TwitterRegisterController.getInstance(getApplicationContext()).getAuthUrl();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusController.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusController.getInstance().unRegister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void authorizationError(AuthorizationErrorMessage authorizationErrorMessage) {
        Log.e(AuthActivity.class.getSimpleName(), "Error: " + authorizationErrorMessage.getMessage());
        if (authorizationErrorMessage.getCode() == 1001) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void authorizationUrlResponse(AuthUrlMessage authUrlMessage) {
        final String url = authUrlMessage.getAuthorizationUrl();
        //Log.d(AuthActivity.class.getSimpleName(), "Received authorization url from Twitter: " + url);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void authorizationComplete(AuthorizationCompleteMessage authorizationCompleteMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(TwitterRegisterController.CALLBACK_URL) && !PreferenceController.getInstance(getApplicationContext()).checkForExistingCredentials()) {
            String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
            TwitterRegisterController.getInstance(getApplicationContext()).retrieveAccessToken(verifier);
        }
    }

}

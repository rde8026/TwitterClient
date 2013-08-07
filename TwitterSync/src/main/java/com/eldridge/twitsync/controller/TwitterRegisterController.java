package com.eldridge.twitsync.controller;

import android.content.Context;
import android.util.Log;

import com.eldridge.twitsync.message.beans.AuthUrlMessage;
import com.eldridge.twitsync.message.beans.AuthorizationCompleteMessage;
import com.eldridge.twitsync.message.beans.AuthorizationErrorMessage;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

/**
 * Created by ryaneldridge on 8/2/13.
 */
public class TwitterRegisterController {

    private static final String TAG = TwitterRegisterController.class.getSimpleName();

    private static TwitterRegisterController instance;
    private Context context;
    private PreferenceController preferenceController;

    /*private static final String CONSUMER_KEY = "kuQqsJvs9E4PPhQEx9miw";
    private static final String CONSUMER_SECRET = "C53Nxk3VSeRr4toUGBM4eo52J1eLmf5QDNMqeK124";*/

    public static final String CONSUMER_KEY = "qZHnxfswxWg5sKknC8Q";
    public static final String CONSUMER_SECRET = "rPHKN3MiMgNFsv0WMbW665NI7CINdp9qE1CsJULnc";

    public static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    public static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

    public static final String CALLBACK_URL = "callback://twittersync";

    private CommonsHttpOAuthConsumer httpOAuthConsumer;
    private OAuthProvider oAuthProvider;

    private TwitterRegisterController(Context context) {
        preferenceController = PreferenceController.getInstance(context);
    }

    public static TwitterRegisterController getInstance(Context context) {
        if (instance == null) {
            synchronized (TwitterRegisterController.class) {
                instance = new TwitterRegisterController(context);
                instance.context = context;
            }
        }
        return instance;
    }

    public void getAuthUrl() {
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    httpOAuthConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                    oAuthProvider = new DefaultOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
                    String authUrl = oAuthProvider.retrieveRequestToken(httpOAuthConsumer, CALLBACK_URL);

                    BusController.getInstance().postMessage(new AuthUrlMessage(authUrl));

                } catch (Exception e) {
                    Log.e(TAG, "Exception in requestToken", e);
                    BusController.getInstance().postMessage(new AuthorizationErrorMessage(e.getMessage(), 1000));
                }

            }
        };
        t.start();
    }

    public void retrieveAccessToken(final String verifier) {
        new Thread() {
            @Override
            public void run() {
                try {
                    oAuthProvider.retrieveAccessToken(httpOAuthConsumer, verifier);

                    /*String token = httpOAuthConsumer.getToken();
                    String tokenSecret = httpOAuthConsumer.getTokenSecret();*/

                    String accessToken = httpOAuthConsumer.getToken();
                    String secret = httpOAuthConsumer.getTokenSecret();

                    preferenceController.setAccessTokenAndSecret(accessToken, secret);

                    BusController.getInstance().postMessage(new AuthorizationCompleteMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception in retrieveAccessToken", e);
                    BusController.getInstance().postMessage(new AuthorizationErrorMessage(e.getMessage(), 1001));
                }
            }
        }.start();
    }

}


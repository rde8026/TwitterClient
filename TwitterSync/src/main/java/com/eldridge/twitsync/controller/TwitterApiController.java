package com.eldridge.twitsync.controller;

import android.content.Context;
import android.util.Log;

import com.eldridge.twitsync.BuildConfig;
import com.eldridge.twitsync.message.beans.ErrorMessage;
import com.eldridge.twitsync.message.beans.TimelineUpdateMessage;
import com.eldridge.twitsync.message.beans.TwitterUserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class TwitterApiController {

    private static final String TAG = TwitterApiController.class.getSimpleName();

    private static TwitterApiController instance;
    private Context context;
    private Twitter twitter;

    public static final int GET_USER_INFO_ERROR_CODE = 2000;
    public static final int GET_USER_TIMELINE_ERROR_CODE = 2001;

    private static final int COUNT = 50;

    private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService executorService;


    private TwitterApiController(Context context) {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        String accessToken = PreferenceController.getInstance(context).getAcessToken();
        String secret = PreferenceController.getInstance(context).getSecret();

        AccessToken at = new AccessToken(accessToken, secret);

        Configuration cb =
                new ConfigurationBuilder()
                    .setJSONStoreEnabled(true)
                    .build();
        twitter = new TwitterFactory(cb).getInstance();
        twitter.setOAuthConsumer(TwitterRegisterController.CONSUMER_KEY, TwitterRegisterController.CONSUMER_SECRET);
        twitter.setOAuthAccessToken(at);
    }

    public static TwitterApiController getInstance(Context context) {
        if (instance == null) {
            synchronized (TwitterApiController.class) {
                instance = new TwitterApiController(context);
                instance.context = context;
            }
        }
        return instance;
    }

    public void getUserInfo() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = twitter.verifyCredentials();
                    PreferenceController.getInstance(context).setUserId(user.getId());
                    BusController.getInstance().postMessage(new TwitterUserMessage(user));
                } catch (TwitterException te) {
                    Log.e(TAG, "", te);
                    BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_INFO_ERROR_CODE));
                }
            }
        });
    }

    public void getUserTimeLine() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
               try {
                   List<Status> tweets = CacheController.getInstance(context).getLatestCachedTweets();
                   if (tweets != null && !tweets.isEmpty()) {
                       BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets));
                   } else {
                       getUserTimeLineFromTwitter();
                   }
               } catch (Exception e) {
                   Log.e(TAG, "", e);
                   getUserTimeLineFromTwitter();
               }
            }
        });
    }

    public void getUserTimeLineFromTwitter() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Paging paging = new Paging();
                    paging.setCount(50);
                    ResponseList<Status> tweets = getPagedTweets(paging);
                    BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets, false));
                    CacheController.getInstance(context).addToCache(tweets);
                } catch (TwitterException te) {
                    Log.e(TAG, "", te);
                    BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_TIMELINE_ERROR_CODE));
                }
            }
        });
    }

    public void refreshUserTimeLine(final Long statusId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Paging paging = new Paging();
                    paging.setSinceId(statusId);
                    ResponseList<Status> tweets = getPagedTweets(paging);
                    BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets, true, true));
                    CacheController.getInstance(context).addToCache(tweets);
                } catch (TwitterException te) {
                    Log.e(TAG, "", te);
                    BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_TIMELINE_ERROR_CODE));
                }
            }
        });
    }

    //This is NOT THREADED so it must be called from a background thread.
    public ResponseList<Status> syncGetUserTimeLineHistory(final Long statusId) throws TwitterException {
        Paging paging = new Paging();
        paging.setMaxId(statusId);
        paging.setCount(COUNT * 2);
        ResponseList<Status> tweets = getPagedTweets(paging);
        CacheController.getInstance(context).addToCache(tweets);
        return tweets;
    }

    private ResponseList<Status> getPagedTweets(Paging paging) throws TwitterException {
        ResponseList<Status> tweets = (paging != null) ? twitter.getHomeTimeline(paging) : twitter.getHomeTimeline();
        return tweets;
    }

}

package com.eldridge.twitsync.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.CacheController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.controller.TwitterRegisterController;
import com.eldridge.twitsync.db.Tweet;
import com.eldridge.twitsync.message.beans.TimelineUpdateMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by reldridge1 on 9/5/13.
 */
public class TwitterStreamingService extends Service {

    private static final String TAG = TwitterStreamingService.class.getSimpleName();

    /*private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService executorService;*/

    private TwitterStream twitterStream;
    private Context _context;
    private List<Status> tweets;

    private static final int TWEET_STAGE_COUNT = 5;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "**** " + TAG + " onStartCommand has been called*****");
        _context = getApplicationContext();

        if (twitterStream == null) {
            String accessToken = PreferenceController.getInstance(_context).getAcessToken();
            String secret = PreferenceController.getInstance(_context).getSecret();

            AccessToken at = new AccessToken(accessToken, secret);

            Configuration cb =
                    new ConfigurationBuilder()
                            .setJSONStoreEnabled(true)
                            .build();

            twitterStream = new TwitterStreamFactory(cb).getInstance();
            twitterStream.setOAuthConsumer(TwitterRegisterController.CONSUMER_KEY, TwitterRegisterController.CONSUMER_SECRET);
            twitterStream.setOAuthAccessToken(at);
            twitterStream.addListener(userStreamListener);
            twitterStream.user();

            tweets = new ArrayList<Status>();

            Log.d(TAG, "**** TwitterStream was created and should be running ***");
        }

        return START_STICKY;
    }

    private UserStreamListener userStreamListener = new UserStreamListener() {
        @Override
        public void onDeletionNotice(long directMessageId, long userId) {

        }

        @Override
        public void onFriendList(long[] longs) {

        }

        @Override
        public void onFavorite(User user, User user2, Status status) {

        }

        @Override
        public void onUnfavorite(User user, User user2, Status status) {

        }

        @Override
        public void onFollow(User user, User user2) {

        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {

        }

        @Override
        public void onUserListMemberAddition(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListMemberDeletion(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListSubscription(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListUnsubscription(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListCreation(User user, UserList userList) {

        }

        @Override
        public void onUserListUpdate(User user, UserList userList) {

        }

        @Override
        public void onUserListDeletion(User user, UserList userList) {

        }

        @Override
        public void onUserProfileUpdate(User user) {

        }

        @Override
        public void onBlock(User user, User user2) {

        }

        @Override
        public void onUnblock(User user, User user2) {

        }

        @Override
        public void onStatus(Status status) {
            Log.e(TAG, "**** onStatus was called by " + TAG + " ****");
            CacheController.getInstance(_context).addToCache(CacheController.getInstance(_context).createTweetObject(status), true);
            Log.d(TAG, "**** Cached new Tweet with ID: " + status.getId() + " ****");
            List<Status> tweet = new ArrayList<Status>();
            tweet.add(status);
            BusController.getInstance().postMessage(new TimelineUpdateMessage(tweet, true, true, true));
            //TODO: Update Server with latest message cached
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

        }

        @Override
        public void onTrackLimitationNotice(int i) {

        }

        @Override
        public void onScrubGeo(long l, long l2) {

        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {

        }

        @Override
        public void onException(Exception e) {
            Log.e(TAG, "Exception in UserStreamListener");
            Log.e(TAG, "", e);
            Crashlytics.logException(e);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "**** " + TAG + " onCreate has been called ****");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "**** " + TAG + " onDestroy has been called ****");
        if (twitterStream != null) {
            twitterStream.cleanUp();
            Log.d(TAG, "**** TwitterStream cleaned up ****");
        }
    }

    public class LocalBinder extends Binder {
        TwitterStreamingService getService() {
            return TwitterStreamingService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}

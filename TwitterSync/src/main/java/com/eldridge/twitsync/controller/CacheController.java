package com.eldridge.twitsync.controller;

import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.eldridge.twitsync.BuildConfig;
import com.eldridge.twitsync.db.Tweet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.json.DataObjectFactory;

/**
 * Created by reldridge1 on 8/6/13.
 */
public class CacheController {

    private static final String TAG = CacheController.class.getSimpleName();

    private static CacheController instance;
    private Context context;

    private static final int CACHE_SIZE = 51;

    private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService executorService;

    private CacheController() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public static CacheController getInstance(Context context) {
        if (instance == null) {
            synchronized (CacheController.class) {
                instance = new CacheController();
                instance.context = context;
            }
        }
        return instance;
    }

    public void addToCache(final ResponseList<Status> items) {
        ActiveAndroid.beginTransaction();
        try {
            long startTime = System.currentTimeMillis();
            for (Status s : items) {
                if (checkTweetsExistence(s)) {
                    Tweet tweet = new Tweet();
                    tweet.tweetId = s.getId();
                    tweet.timestamp = s.getCreatedAt().getTime();//System.currentTimeMillis();
                    tweet.json = DataObjectFactory.getRawJSON(s);
                    tweet.save();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
            if (BuildConfig.DEBUG) {
                long delta = System.currentTimeMillis() - startTime;
                Log.d(TAG, "** Cached " + items.size() + " to DB in " + delta + "ms **");
            }
        } catch (IOException ioe) {
            Log.e(TAG, "", ioe);
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    private boolean checkTweetsExistence(Status s) throws IOException {
        Tweet existing = new Select().from(Tweet.class).where("tweetId = ?", s.getId()).executeSingle();
        return existing == null;
    }

    public void trimCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "** Checking Cache size **");
                ActiveAndroid.beginTransaction();
                try {
                    //List<Tweet> top = new Select().from(Tweet.class).limit(CACHE_SIZE).execute();
                    List<Tweet> top = new Select().from(Tweet.class).orderBy("timestamp ASC").limit(CACHE_SIZE).execute();
                    if (top != null && !top.isEmpty()) {
                        List<Tweet> outliers = new Select().from(Tweet.class).where("Id >= " + top.get(top.size() - 1).getId()).execute();
                        if (outliers != null && !outliers.isEmpty()) {
                            long startTime = System.currentTimeMillis();
                            Log.d(TAG, "** Removing " + outliers.size() + " from Cache **");
                            for (Tweet t : outliers) {
                                t.delete();
                            }
                            ActiveAndroid.setTransactionSuccessful();
                            long delta = System.currentTimeMillis() - startTime;
                            Log.d(TAG, "** Cache Cleanup took " + delta + "ms ***");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }
        });
    }

    public List<Status> getLatestCachedTweets() throws Exception {
        long startTime = System.currentTimeMillis();
        List<Tweet> cachedTweets = new Select().from(Tweet.class).orderBy("Id ASC").execute();
        List<Status> tweets = new ArrayList<Status>();
        for (Tweet t : cachedTweets) {
            Status s = DataObjectFactory.createStatus(t.json);
            tweets.add(s);
        }
        long delta = System.currentTimeMillis() - startTime;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "**** Cache returned and converted " + cachedTweets.size() + " in " + delta + " ms");
            Log.d(TAG, "**** Cached returned and converted " + cachedTweets.size() + " in " + delta / 1000 + " s");
        }
        return tweets;
    }


    public void clearDb() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                new Delete().from(Tweet.class).execute();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "*** Deleted ALL from cache db ***");
                }
            }
        });
    }

    private List<Tweet> getCachedTweets(String sort) {
        return new Select().from(Tweet.class).orderBy(sort).execute();
    }

}

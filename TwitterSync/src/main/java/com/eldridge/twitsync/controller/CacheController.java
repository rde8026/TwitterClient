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

    private static final int CACHE_SIZE = 251;

    private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService executorService;
    private static ArrayList<Tweet> memoryCache;

    private CacheController() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        memoryCache = new ArrayList<Tweet>();
        ActiveAndroid.setLoggingEnabled(true);
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

    public void addToCache(final ResponseList<Status> items, boolean top) {
        try {
            long startTime = System.currentTimeMillis();
            for (Status s : items) {
                Tweet tweet = new Tweet();
                tweet.tweetId = s.getId();
                tweet.timestamp = s.getCreatedAt().getTime();
                tweet.json = DataObjectFactory.getRawJSON(s);
                addTweetToMemoryCache(tweet, top);
            }
            if (BuildConfig.DEBUG) {
                long delta = System.currentTimeMillis() - startTime;
                //Log.d(TAG, "** Cached " + items.size() + " to DB in " + delta + "ms **");
                Log.d(TAG, "** Insert into memory cache took " + delta + " ms **");
            }
        } catch (IOException ioe) {
            Log.e(TAG, "", ioe);
        }
    }

    private boolean checkTweetsExistence(Status s) throws IOException {
        Tweet existing = new Select().from(Tweet.class).where("tweetId = ?", s.getId()).executeSingle();
        return existing == null;
    }

    private void addTweetToMemoryCache(Tweet t, boolean top) throws IOException {
        if (!memoryCache.contains(t)) {
            if (top) {
                memoryCache.add(0, t);
            } else {
                memoryCache.add(t);
            }
        }
    }

    public void trimCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                deleteDbRecords();
                try {
                    ActiveAndroid.beginTransaction();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "** Persistent Cache cleared **");
                        Log.d(TAG, "** Loading " + memoryCache.size() + " to persistent cache **");
                    }
                    Log.d(TAG, "** Memory Cache Size: " + memoryCache.size() + " **");
                    for (int i = 0; i < memoryCache.size(); i++) {
                        if (i <= CACHE_SIZE) {
                            Tweet t = memoryCache.get(i);
                            //TODO: Fix this issue - either don't drop the entire DB or figure out a way to deal w/ the Id being populated.
                            if (t.getId() != null) {
                                Tweet hackForUpdate = new Tweet();
                                hackForUpdate.timestamp = t.timestamp;
                                hackForUpdate.tweetId = t.tweetId;
                                hackForUpdate.json = t.json;
                                hackForUpdate.save();
                            } else {
                                t.save();
                            }
                        } else {
                            break;
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                } finally {
                    ActiveAndroid.endTransaction();
                    if (BuildConfig.DEBUG) {
                        List<Tweet> ts = new Select().from(Tweet.class).execute();
                        Log.d(TAG, "********** Cache Count is " + ts.size() + " **********");
                    }
                    memoryCache.clear();
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
            memoryCache.add(t);
        }
        long delta = System.currentTimeMillis() - startTime;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "**** Cache returned and converted " + cachedTweets.size() + " in " + delta + " ms");
            Log.d(TAG, "**** Cached returned and converted " + cachedTweets.size() + " in " + delta / 1000 + " s");
        }
        return tweets;
    }

    private void deleteDbRecords() {
        ActiveAndroid.beginTransaction();
        new Delete().from(Tweet.class).execute();
        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();
    }

    public void clearDb() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                new Delete().from(Tweet.class).execute();
                memoryCache.clear();
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

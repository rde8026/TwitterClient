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

    private static final int CACHE_SIZE = 250;

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
                    tweet.timestamp = System.currentTimeMillis();//s.getCreatedAt().getTime();
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
            trimCache();
        }
    }

    private boolean checkTweetsExistence(Status s) throws IOException {
        Tweet existing = new Select().from(Tweet.class).where("tweetId = ?", s.getId()).executeSingle();
        return existing == null;
    }

    private void trimCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "** Running trimCache to keep things reasonable **");
                ActiveAndroid.beginTransaction();
                try {
                    List<Tweet> tweets = getCachedTweets("timestamp ASC");
                    if (tweets.size() >= CACHE_SIZE) {
                        Log.d(TAG, "** Trimming Cache to " + CACHE_SIZE + " **");
                        int cutAmount = tweets.size() - CACHE_SIZE;
                        Log.d(TAG, "** Cutting " + cutAmount + " from cache **");
                        for (int i = 0; i < tweets.size(); i++) {
                            if (i < cutAmount) {
                                Tweet t = tweets.get(i);
                                t.delete();
                            } else {
                                break;
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
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
        List<Tweet> cachedTweets = getCachedTweets("timestamp DESC");
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


    private void clearDb() {
        new Delete().from(Tweet.class).execute();
    }

    private List<Tweet> getCachedTweets(String sort) {
        return new Select().from(Tweet.class).orderBy(sort).execute();
    }

}

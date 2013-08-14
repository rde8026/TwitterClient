package com.eldridge.twitsync.controller;

import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.eldridge.twitsync.BuildConfig;
import com.eldridge.twitsync.db.Tweet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
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

    private static final int CACHE_SIZE = 41;

    private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService executorService;
    private static ArrayList<Tweet> memoryCache;

    private static ArrayDeque<Tweet> deque;

    private CacheController() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        memoryCache = new ArrayList<Tweet>();
        deque = new ArrayDeque<Tweet>();
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

    public synchronized void addToCache(final ResponseList<Status> items, boolean top) {
        try {
            long startTime = System.currentTimeMillis();
            if (top) {
                for (Status s : items) {
                    Tweet tweet = new Tweet();
                    tweet.tweetId = s.getId();
                    tweet.timestamp = s.getCreatedAt().getTime();
                    tweet.json = DataObjectFactory.getRawJSON(s);
                    addTweetToMemoryCache(tweet, top);
                }
            } else {
                for (int i = items.size() - 1; i >= 0; i--) {
                    Status s = items.get(i);
                    Tweet tweet = new Tweet();
                    tweet.tweetId = s.getId();
                    tweet.timestamp = s.getCreatedAt().getTime();
                    tweet.json = DataObjectFactory.getRawJSON(s);
                    addTweetToMemoryCache(tweet, top);
                }
            }
            if (BuildConfig.DEBUG) {
                long delta = System.currentTimeMillis() - startTime;
                Log.d(TAG, "** Added " + items.size() + " to Cache **");
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

    private synchronized void addTweetToMemoryCache(Tweet t, boolean top) throws IOException {
        if (!memoryCache.contains(t)) {
            if (top) {
                deque.addFirst(t);
            } else {
                deque.addLast(t);
            }
        }
    }

    public synchronized void trimCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                deleteDbRecords();
                try {
                    ActiveAndroid.beginTransaction();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "** Persistent Cache cleared **");
                        Log.d(TAG, "** Loading " + deque.size() + " to cache **");
                    }
                    Iterator<Tweet> iterator = deque.iterator();
                    int count = 0;
                    while (iterator.hasNext()) {
                        if (count >= CACHE_SIZE) {
                            Log.d(TAG, "**** CACHE SIZE: " + CACHE_SIZE + " ****");
                            Log.d(TAG, "****** Cache Size has been reached ******");
                            break;
                        }

                        Tweet t = iterator.next();
                        Status s = DataObjectFactory.createStatus(t.json);
                        Log.d(TAG, "** Storing Tweet\n" + s.getText() + "\n **");
                        if (t.getId() != null) {
                            Log.d(TAG, "** Hack for ActiveAndroid **");
                            Tweet hackForUpdate = new Tweet();
                            hackForUpdate.timestamp = t.timestamp;
                            hackForUpdate.tweetId = t.tweetId;
                            hackForUpdate.json = t.json;
                            hackForUpdate.save();
                        } else {
                            t.save();
                        }
                        count++;
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
                    //memoryCache.clear();
                    deque.clear();
                }
            }
        });
    }

    public synchronized List<Status> getLatestCachedTweets() throws Exception {
        long startTime = System.currentTimeMillis();
        List<Tweet> cachedTweets = new Select().from(Tweet.class).execute(); //.orderBy("Id ASC")
        List<Status> tweets = new ArrayList<Status>();
        for (Tweet t : cachedTweets) {
            Status s = DataObjectFactory.createStatus(t.json);
            tweets.add(s);
            //memoryCache.add(t);
            deque.addFirst(t);
        }
        long delta = System.currentTimeMillis() - startTime;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "**** Cache returned and converted " + cachedTweets.size() + " in " + delta + " ms");
            Log.d(TAG, "**** Cached returned and converted " + cachedTweets.size() + " in " + delta / 1000 + " s");
        }
        return tweets;
    }

    private synchronized void deleteDbRecords() {
        ActiveAndroid.beginTransaction();
        new Delete().from(Tweet.class).execute();
        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();
    }

    public synchronized void clearDb() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                new Delete().from(Tweet.class).execute();
                //memoryCache.clear();
                deque.clear();
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

package com.eldridge.twitsync.controller;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.crashlytics.android.Crashlytics;
import com.eldridge.twitsync.BuildConfig;
import com.eldridge.twitsync.db.Tweet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

    private static final int CACHE_SIZE = 100;

    private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService executorService;

    private static ArrayDeque<Tweet> deque;
    private static LinkedHashMap<Date, Tweet> cache;

    private CacheController() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        deque = new ArrayDeque<Tweet>();
        cache = new LinkedHashMap<Date, Tweet>();
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

    public synchronized void addToCache(final ResponseList<Status> items, boolean front) {
        try {
            long startTime = System.currentTimeMillis();
            //If we are adding Tweets to the front of the cache they need to be in the
            //reverse order of how they will be presented in the list so our cache is properly ordered
            /*if (front) {
                Collections.reverse(items);
            }*/
            for (Status s : items) {
                addTweetToMemoryCache(createTweetObject(s), front);
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

    public synchronized void addToCache(final ArrayList<Tweet> items, boolean front) {
        try {
            long startTime = System.currentTimeMillis();
            /*if (front) {
                Collections.reverse(items);
            }*/
            for (Tweet t : items) {
                addTweetToMemoryCache(t, front);
            }
            if (BuildConfig.DEBUG) {
                long delta = System.currentTimeMillis() - startTime;
                Log.d(TAG, "** Added " + items.size() + " to Cache **");
                Log.d(TAG, "** Insert into memory cache took " + delta + "(ms) **");
            }
        } catch (IOException ioe) {
            Crashlytics.logException(ioe);
            Log.e(TAG, "", ioe);
        }
    }

    public synchronized void addToCache(Tweet tweet, boolean front) {
        try {
            long startTime = System.currentTimeMillis();
            addTweetToMemoryCache(tweet, front);
            if (BuildConfig.DEBUG) {
                long delta = System.currentTimeMillis() - startTime;
                Log.d(TAG, "** Insert into memory cache took " + delta + "(ms) **");
            }
        } catch (IOException ioe) {
            Crashlytics.logException(ioe);
            Log.e(TAG, "", ioe);
        }
    }

    public Tweet createTweetObject(Status s) {
        Tweet tweet = new Tweet();
        tweet.tweetId = s.getId();
        tweet.timestamp = s.getCreatedAt().getTime();
        tweet.json = DataObjectFactory.getRawJSON(s);
        return tweet;
    }

    private synchronized void addTweetToMemoryCache(Tweet t, boolean front) throws IOException {
        /*if (!deque.contains(t)) {
            if (front) {
                deque.addFirst(t);
            } else {
                deque.addLast(t);

            }
        }*/
        t.save();
    }

    public synchronized void trimCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //deleteDbRecords();
                try {
                    ActiveAndroid.beginTransaction();
                    List<Tweet> tweets = new Select().all().from(Tweet.class).execute();
                    Log.d(TAG, "**** Current Cache size is: " + tweets.size() + " ****");
                    if (tweets.size() > CACHE_SIZE) {
                        Log.d(TAG, "****** Cache Size has been reached ******");
                        Tweet t = tweets.get(CACHE_SIZE);
                        new Delete().from(Tweet.class).where("timestamp <= " + t.timestamp).execute();
                    }
                    /*if (BuildConfig.DEBUG) {
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
                        if (t.getId() != null) {
                            Tweet hackForUpdate = new Tweet();
                            hackForUpdate.timestamp = t.timestamp;
                            hackForUpdate.tweetId = t.tweetId;
                            hackForUpdate.json = t.json;
                            hackForUpdate.save();
                        } else {
                            t.save();
                        }
                        count++;
                    }*/
                    ActiveAndroid.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                } finally {
                    ActiveAndroid.endTransaction();
                    if (BuildConfig.DEBUG) {
                        List<Tweet> ts = new Select().from(Tweet.class).execute();
                        Log.d(TAG, "********** Cache Count is " + ts.size() + " **********");
                    }
                    deque.clear();
                }
            }
        });
    }

    public synchronized List<Status> getLatestCachedTweets() throws Exception {
        long startTime = System.currentTimeMillis();
        List<Tweet> cachedTweets = new Select().from(Tweet.class).orderBy("timestamp DESC").execute();
        List<Status> tweets = new ArrayList<Status>();

        for (Tweet t : cachedTweets) {
            Status s = DataObjectFactory.createStatus(t.json);
            tweets.add(s);
            //deque.addLast(t);
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
                deque.clear();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "*** Deleted ALL from cache db ***");
                }
            }
        });
    }

}

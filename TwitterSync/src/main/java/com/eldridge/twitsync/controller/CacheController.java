package com.eldridge.twitsync.controller;

import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.eldridge.twitsync.db.Tweet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.ResponseList;
import twitter4j.Status;

/**
 * Created by reldridge1 on 8/6/13.
 */
public class CacheController {

    private static final String TAG = CacheController.class.getSimpleName();

    private static CacheController instance;
    private Context context;



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
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ActiveAndroid.beginTransaction();
                try {
                    for (Status s : items) {
                        Tweet tweet = new Tweet();
                        tweet.tweetId = s.getId();
                        tweet.timestamp = s.getCreatedAt().getTime();
                        tweet.tweet = convertToByteArray(s);
                        tweet.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                    checkCache();
                } catch (IOException ioe) {
                    Log.e(TAG, "", ioe);
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }
        });
    }

    public void checkCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Select select = new Select();
                    List<Tweet> tweetList =
                            select
                            .from(Tweet.class)
                            .orderBy("timestamp DESC")
                            .execute();
                    ArrayList<Status> tweets = new ArrayList<Status>();
                    for (Tweet t : tweetList) {
                        Status s = convertBytesToStatus(t.tweet);
                        tweets.add(s);
                    }
                    Log.d(TAG, "**** Check Cache ****");
                } catch (IOException ioe) {
                    Log.e(TAG, "", ioe);
                } catch (ClassNotFoundException cnfe) {
                    Log.e(TAG, "", cnfe);
                }
            }
        });
    }

    public byte[] convertToByteArray(Status s) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(b);
        oos.writeObject(s);
        return b.toByteArray();
    }

    private Status convertBytesToStatus(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return (Status)o.readObject();
    }

    private void readFromCache() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

}

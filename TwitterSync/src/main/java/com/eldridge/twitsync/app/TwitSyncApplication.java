package com.eldridge.twitsync.app;

import com.activeandroid.ActiveAndroid;

/**
 * Created by reldridge1 on 8/6/13.
 */
public class TwitSyncApplication extends com.activeandroid.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}

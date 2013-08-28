package com.eldridge.twitsync.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.CacheController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.message.beans.TweetDetailMessage;
import com.squareup.otto.Subscribe;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends SherlockFragmentActivity {

    private FragmentManager fragmentManager;
    private PreferenceController preferenceController;
    private PullToRefreshAttacher pullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
        preferenceController = PreferenceController.getInstance(getApplicationContext());

        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (!preferenceController.checkForExistingCredentials()) {
            Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(authIntent);
        }
    }

    public PullToRefreshAttacher getPullToRefreshAttacher() {
        return pullToRefreshAttacher;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusController.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CacheController.getInstance(this).trimCache();
        BusController.getInstance().unRegister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void addDetailFragment(final TweetDetailMessage tweetDetailMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent detailIntent = new Intent(getApplicationContext(), TweetDetailActivity.class);
                detailIntent.putExtra(TweetDetailActivity.DETAIL_KEY, tweetDetailMessage.getStatus());
                startActivity(detailIntent);
            }
        });
    }

}

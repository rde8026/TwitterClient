package com.eldridge.twitsync.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.CacheController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.fragment.LoadingFragment;
import com.eldridge.twitsync.fragment.TweetDetailFragment;
import com.eldridge.twitsync.fragment.TweetsFragment;
import com.eldridge.twitsync.message.beans.AuthorizationCompleteMessage;
import com.eldridge.twitsync.message.beans.ScrollMessage;
import com.eldridge.twitsync.message.beans.TweetDetailMessage;
import com.squareup.otto.Subscribe;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends SherlockFragmentActivity {

    private FragmentManager fragmentManager;
    private PreferenceController preferenceController;
    private PullToRefreshAttacher pullToRefreshAttacher;
    private LoadingFragment mLoadingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
        preferenceController = PreferenceController.getInstance(getApplicationContext());

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (!preferenceController.checkForExistingCredentials()) {
            mLoadingFragment = new LoadingFragment();
            ft.add(R.id.fragmentLayout, mLoadingFragment);
            Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(authIntent);
        } else {
            ft.add(R.id.fragmentLayout, new TweetsFragment());
        }
        ft.commit();
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
    public void authorizationComplete(AuthorizationCompleteMessage authorizationCompleteMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.remove(mLoadingFragment);
                ft.add(R.id.fragmentLayout, new TweetsFragment());
                ft.commitAllowingStateLoss();
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void addDetailFragment(TweetDetailMessage tweetDetailMessage) {
        TweetDetailFragment detailFragment = TweetDetailFragment.newInstance(tweetDetailMessage.getStatus());
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentLayout, detailFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(TweetDetailFragment.NAME);
        fragmentTransaction.commit();
    }

}

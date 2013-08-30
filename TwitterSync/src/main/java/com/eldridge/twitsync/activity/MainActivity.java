package com.eldridge.twitsync.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.crashlytics.android.Crashlytics;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.adapter.ViewPagerAdapter;
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

    private ViewPager mViewPager;
    private ActionBar mActionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Crashlytics.start(this);

        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
        preferenceController = PreferenceController.getInstance(getApplicationContext());

        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main_viewpager_tabs_layout);
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mActionBar.setSelectedNavigationItem(position);
            }
        };

        mViewPager.setOnPageChangeListener(viewPagerListener);

        fragmentManager = getSupportFragmentManager();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(fragmentManager);
        mViewPager.setAdapter(viewPagerAdapter);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        ActionBar.Tab timeLineTab = mActionBar.newTab().setText(getString(R.string.timeline_tab_text)).setTabListener(tabListener);
        mActionBar.addTab(timeLineTab);

        ActionBar.Tab mentionsTab = mActionBar.newTab().setText(getString(R.string.mentions_tab_text)).setTabListener(tabListener);
        mActionBar.addTab(mentionsTab);

        ActionBar.Tab directMessageTab = mActionBar.newTab().setText(getString(R.string.direct_message_tab_text)).setTabListener(tabListener);
        mActionBar.addTab(directMessageTab);



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

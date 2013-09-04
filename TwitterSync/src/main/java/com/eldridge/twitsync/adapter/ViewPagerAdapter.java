package com.eldridge.twitsync.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.eldridge.twitsync.fragment.DirectMessageFragment;
import com.eldridge.twitsync.fragment.MentionsFragment;
import com.eldridge.twitsync.fragment.TweetsFragment;

/**
 * Created by reldridge1 on 8/30/13.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = ViewPagerAdapter.class.getSimpleName();
    private final int PAGE_COUNT = 3;
    private TweetsFragment tweetsFragment;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                //TweetsFragment tweetsFragment = new TweetsFragment();
                if (tweetsFragment == null) {
                    tweetsFragment = new TweetsFragment();
                }
                return tweetsFragment;
            case 1:
                MentionsFragment mentionsFragment = new MentionsFragment();
                return mentionsFragment;
            case 2:
                DirectMessageFragment directMessageFragment = new DirectMessageFragment();
                return directMessageFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}

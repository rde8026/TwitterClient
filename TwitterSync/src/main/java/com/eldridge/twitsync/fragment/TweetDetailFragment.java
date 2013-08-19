package com.eldridge.twitsync.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.eldridge.twitsync.R;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Created by reldridge1 on 8/19/13.
 */
public class TweetDetailFragment extends SherlockFragment {

    private static final String TAG = TweetDetailFragment.class.getSimpleName();
    private static final String STATUS_KEY = "STATUS";

    private LinearLayout detailLoadingWrapper;

    public static final String NAME = "DETAILS_FRAGMENT";

    public static TweetDetailFragment newInstance(Status status) {
        TweetDetailFragment tweetDetailFragment = new TweetDetailFragment();
        Bundle b = new Bundle();
        b.putSerializable(STATUS_KEY, status);
        tweetDetailFragment.setArguments(b);
        return tweetDetailFragment;
    }

    private Status getStatus() {
        return (Status)getArguments().getSerializable(STATUS_KEY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tweet_detail_layout, container, false);
        Status s = getStatus();
        detailLoadingWrapper = (LinearLayout) v.findViewById(R.id.detailLoadingWrapper);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleLoadingView();
        getSherlockActivity().getSupportActionBar().setTitle(R.string.tweet_details_ab_title);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void toggleLoadingView() {
        if (detailLoadingWrapper != null) {
           if (detailLoadingWrapper.getVisibility() == View.GONE) {
               detailLoadingWrapper.setVisibility(View.VISIBLE);
           } else {
               detailLoadingWrapper.setVisibility(View.GONE);
           }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSherlockActivity().getSupportFragmentManager().popBackStackImmediate(NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        return super.onOptionsItemSelected(item);
    }
}

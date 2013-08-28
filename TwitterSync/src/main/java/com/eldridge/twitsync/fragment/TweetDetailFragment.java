package com.eldridge.twitsync.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.activity.TweetDetailActivity;
import com.squareup.picasso.Picasso;

import butterknife.InjectView;
import butterknife.Views;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Created by reldridge1 on 8/19/13.
 */
public class TweetDetailFragment extends SherlockFragment {

    private static final String TAG = TweetDetailFragment.class.getSimpleName();

    private LinearLayout detailLoadingWrapper;

    public static final String NAME = "DETAILS_FRAGMENT";

    @InjectView(R.id.profileImage) ImageView profileImage;
    @InjectView(R.id.retweetCount) TextView retweetCount;
    @InjectView(R.id.tweetText) TextView tweetText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tweet_detail_layout, container, false);
        Views.inject(this, v);
        detailLoadingWrapper = (LinearLayout) v.findViewById(R.id.detailLoadingWrapper);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleLoadingView();
        getSherlockActivity().getSupportActionBar().setTitle(R.string.tweet_details_ab_title);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TweetDetailActivity tweetDetailActivity = (TweetDetailActivity)getSherlockActivity();
        Status status = tweetDetailActivity.getStatus();

        Picasso.with(tweetDetailActivity.getApplicationContext()).load(R.drawable.ic_launcher).resize(125, 125).into(profileImage);
        Picasso.with(tweetDetailActivity.getApplicationContext()).load(status.getUser().getBiggerProfileImageURLHttps()).resize(150, 150).into(profileImage);

        retweetCount.setText(String.format(getResources().getString(R.string.retweet_count_text), String.valueOf(status.getRetweetCount())));
        tweetText.setText(status.getText());

        toggleLoadingView();
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
            getSherlockActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

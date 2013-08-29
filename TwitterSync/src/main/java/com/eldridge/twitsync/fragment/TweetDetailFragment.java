package com.eldridge.twitsync.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.activity.TweetDetailActivity;
import com.eldridge.twitsync.beans.MediaUrlEntity;
import com.eldridge.twitsync.util.TypeEnum;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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

    @InjectView(R.id.profileImage) ImageView profileImage;
    @InjectView(R.id.retweetCount) TextView retweetCount;
    @InjectView(R.id.tweetText) TextView tweetText;

    @InjectView(R.id.mediaLayout) LinearLayout mediaLayout;
    @InjectView(R.id.mediaWebView) WebView mediaWebView;
    @InjectView(R.id.mediaLoadingIndicator) ProgressBar mediaLoadingIndicator;

    @InjectView(R.id.imageLayout) LinearLayout imageLayout;
    @InjectView(R.id.mediaImage) ImageView mediaImage;

    private List<MediaUrlEntity> mediaUrlEntities = new ArrayList<MediaUrlEntity>();

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

        mediaWebView.getSettings().setJavaScriptEnabled(true);
        mediaWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        mediaWebView.setWebViewClient(new DetailWebViewClient());
        mediaWebView.setWebChromeClient(new DetailWebChromeClient());

        TweetDetailActivity tweetDetailActivity = (TweetDetailActivity)getSherlockActivity();
        Status status = tweetDetailActivity.getStatus();

        Picasso.with(tweetDetailActivity.getApplicationContext()).load(R.drawable.ic_launcher).resize(100, 100).into(profileImage);
        Picasso.with(tweetDetailActivity.getApplicationContext())
                .load(status.getUser().getBiggerProfileImageURLHttps())
                .resize(100, 100)
                .into(profileImage);

        retweetCount.setText(String.format(getResources().getString(R.string.retweet_count_text), String.valueOf(status.getRetweetCount())));

        tweetText.setText(status.getText());

        if ( status.getMediaEntities() != null && status.getMediaEntities().length > 0 ) {
            MediaEntity[] mediaEntities = status.getMediaEntities();
            for (MediaEntity entity : mediaEntities) {
                mediaUrlEntities.add(new MediaUrlEntity(entity, TypeEnum.parse(entity.getType()) == TypeEnum.PHOTO));
            }
            MediaEntity entryOne = mediaEntities[0];

            Log.d(TAG, "*** Media Entity Type: " + entryOne.getType() + " ***");
            Log.d(TAG, "*** Media Entity Media URL: " + entryOne.getMediaURL() + " ***");
            Log.d(TAG, "*** Media Entity Display URL: " + entryOne.getDisplayURL() + " ***");
            Log.d(TAG, "*** Media Entity URL: " + entryOne.getURL() + " ***");

        }

        if ( status.getURLEntities() != null && status.getURLEntities().length > 0 ) {
            URLEntity[] urlEntities = status.getURLEntities();
            for (URLEntity entity : urlEntities) {
                mediaUrlEntities.add(new MediaUrlEntity(entity, false));
            }
            URLEntity entryOne = urlEntities[0];
            Log.d(TAG, "*** Entity Display URL: " + entryOne.getDisplayURL() + " ***");
            Log.d(TAG, "*** Entity URL: " + entryOne.getURL() + " ***");
            Log.d(TAG, "*** Entity Expanded URL: " + entryOne.getExpandedURL() + " ****");
        }

        if ( !mediaUrlEntities.isEmpty() ) {
            MediaUrlEntity entity = mediaUrlEntities.get(0);
            if ( entity.getUrlEntity() instanceof MediaEntity ) {
                MediaEntity mediaEntity = (MediaEntity) entity.getUrlEntity();
                if ( entity.isPhoto() ) {
                    imageLayout.setVisibility(View.VISIBLE);

                    Picasso.with(getSherlockActivity().getApplicationContext())
                            .load(mediaEntity.getMediaURL())
                            .placeholder(R.drawable.ic_launcher)
                            .error(R.drawable.ic_launcher)
                            .into(mediaImage);
                } else {
                    mediaLayout.setVisibility(View.VISIBLE);
                    mediaWebView.loadUrl(mediaEntity.getMediaURL());
                }
            } else if ( entity.getUrlEntity() instanceof URLEntity ) {
                URLEntity urlEntity = (URLEntity) entity.getUrlEntity();
                mediaLayout.setVisibility(View.VISIBLE);
                mediaWebView.loadUrl(urlEntity.getURL());
            } else {
                Log.d(TAG, "*** Unknown Entity ***");
            }
        }
        Log.d(TAG, "*** MediaUrlEntities size: " + mediaUrlEntities.size() + " ***");

        toggleLoadingView();
    }

    private class DetailWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }

    private class DetailWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            super.onProgressChanged(view, progress);
            if (progress < 100 && mediaLoadingIndicator.getVisibility() == ProgressBar.GONE) {
                mediaLoadingIndicator.setVisibility(View.VISIBLE);
            }
            mediaLoadingIndicator.setProgress(progress);
            if (progress == 100) {
                mediaWebView.setVisibility(View.VISIBLE);
                mediaLoadingIndicator.setVisibility(View.GONE);
            }
        }
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

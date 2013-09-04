package com.eldridge.twitsync.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.activity.MainActivity;
import com.eldridge.twitsync.adapter.EndlessTweetsAdapter;
import com.eldridge.twitsync.adapter.TweetsAdapter;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.CacheController;
import com.eldridge.twitsync.controller.GcmController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.controller.TwitterApiController;
import com.eldridge.twitsync.message.beans.AuthorizationCompleteMessage;
import com.eldridge.twitsync.message.beans.ErrorMessage;
import com.eldridge.twitsync.message.beans.TimelineUpdateMessage;
import com.eldridge.twitsync.message.beans.TweetDetailMessage;
import com.eldridge.twitsync.message.beans.TweetMessage;
import com.eldridge.twitsync.message.beans.TwitterUserMessage;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.Views;
import twitter4j.Status;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by ryaneldridge on 8/4/13.
 */
@SuppressWarnings("unused")
public class TweetsFragment extends SherlockListFragment implements PullToRefreshAttacher.OnRefreshListener {

    private String TAG = TweetsFragment.class.getSimpleName();
    private ListView listView;
    private LinearLayout linearLoading;
    private TweetsAdapter adapter;
    private EndlessTweetsAdapter endlessTweetsAdapter;

    @InjectView(R.id.tweetAction) Button tweetAction;
    @InjectView(R.id.tweetEdit) EditText tweetEdit;

    private PullToRefreshAttacher pullToRefreshAttacher;

    private InputMethodManager imm;
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        /*if (PreferenceController.getInstance(getSherlockActivity().getApplicationContext()).checkForExistingCredentials()) {
            TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getUserTimeLine();
        }

        imm = (InputMethodManager)getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.tweets_fragment_layout, container, false);
        Views.inject(this, v);

        tweetAction.setOnClickListener(tweetActionListener);

        listView = (ListView) v.findViewById(android.R.id.list);
        linearLoading = (LinearLayout) v.findViewById(R.id.linearLoading);
        pullToRefreshAttacher = ((MainActivity) getSherlockActivity()).getPullToRefreshAttacher();
        pullToRefreshAttacher.addRefreshableView(listView, this);
        //showLoadingView();
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceController.getInstance(getSherlockActivity().getApplicationContext()).checkForExistingCredentials()) {
            TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getUserTimeLine();
        }

        imm = (InputMethodManager)getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        showLoadingView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "**** TweetsFragment onResume called ****");
        BusController.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "**** TweetsFragment onPause called ****");
        BusController.getInstance().unRegister(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Status s = (Status)l.getItemAtPosition(position);
        BusController.getInstance().postMessage(new TweetDetailMessage(s));
    }

    @Subscribe
    public void userMessage(TwitterUserMessage twitterUserMessage) {
        Log.d(TAG, "*** Requesting Users TimeLine ***");
        TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getUserTimeLine();
    }

    @Subscribe
    public void userTimeLineMessage(final TimelineUpdateMessage timelineUpdateMessage) {
        getSherlockActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GcmController.getInstance(getSherlockActivity().getApplicationContext()).registerDevice();

                if (adapter == null) {
                    adapter = new TweetsAdapter(getSherlockActivity(), R.layout.tweet_item_layout, timelineUpdateMessage.getTweets());
                }

                if (endlessTweetsAdapter == null) {
                    endlessTweetsAdapter = new EndlessTweetsAdapter(adapter, getSherlockActivity());
                }

                getListView().setAdapter(endlessTweetsAdapter);

                if (timelineUpdateMessage.isRefresh()) {
                    if (timelineUpdateMessage.getTweets() != null && timelineUpdateMessage.getTweets().size() > 0 && timelineUpdateMessage.isPrepend()) {
                        for (Status s : timelineUpdateMessage.getTweets()) {
                            adapter.insert(s, 0);
                        }

                        endlessTweetsAdapter.notifyDataSetChanged();
                        listView.smoothScrollToPosition(0);
                    }
                }
                pullToRefreshAttacher.setRefreshComplete();
                showListView();
            }
        });
    }

    @Subscribe
    public void errorMessage(final ErrorMessage errorMessage) {
        getSherlockActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorMessage.getCode() == TwitterApiController.GET_USER_TIMELINE_ERROR_CODE) {
                    pullToRefreshAttacher.setRefreshComplete();
                    showListView();
                }
                if (endlessTweetsAdapter != null) {
                    endlessTweetsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void scrollMessages(final boolean top) {
        if (top) {
            listView.smoothScrollToPosition(0);
        } else {
            listView.smoothScrollToPosition(listView.getCount() - 1);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        try {
            Status mostRecent = (Status) endlessTweetsAdapter.getItem(0);
            TwitterApiController.getInstance(getSherlockActivity()).refreshUserTimeLine(mostRecent.getId());
        } catch (Exception e) {
            Log.e(TAG, "", e);
            Toast.makeText(getSherlockActivity(), "Yikes, something went wrong!", Toast.LENGTH_SHORT).show();
            pullToRefreshAttacher.setRefreshComplete();
        }
    }

    private void showLoadingView() {
        listView.setVisibility(View.GONE);
        linearLoading.setVisibility(View.VISIBLE);
    }

    private void showListView() {
        if (listView.getVisibility() == View.GONE && linearLoading.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.VISIBLE);
            linearLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.moveToTop) {
            scrollMessages(true);
        } else if (item.getItemId() == R.id.moveToBottom) {
            scrollMessages(false);
        } else if (item.getItemId() == R.id.action_delete_db) {
            CacheController.getInstance(getSherlockActivity()).clearDb();
            PreferenceController.getInstance(getSherlockActivity()).clearGcmRegistration();
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener tweetActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tweetText = tweetEdit.getText().toString();
            if (tweetText.length() > 0) {
                toggleRefreshMenuItem();
                TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).sendTweet(tweetText);
            } else {
                Toast.makeText(getSherlockActivity(), getSherlockActivity().getResources().getString(R.string.empty_tweet_toast_text), Toast.LENGTH_SHORT).show();
            }
            imm.hideSoftInputFromWindow(tweetEdit.getWindowToken(), 0);
        }
    };

    @Subscribe
    public void tweetUpdate(final TweetMessage tweetMessage) {
        getSherlockActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tweetMessage.isSuccess()) {
                    Status s = adapter.getItem(adapter.getCount() - 1);
                    TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).refreshUserTimeLine(s.getId());
                    tweetEdit.setText("");
                    tweetEdit.clearFocus();
                } else {
                    Toast.makeText(getSherlockActivity(), tweetMessage.getTwitterException().getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                toggleRefreshMenuItem();
            }
        });
    }

    private void toggleRefreshMenuItem() {
        MenuItem progressItem = this.menu.findItem(R.id.menuProgress);
        if (progressItem.isVisible()) {
            progressItem.setVisible(false);
        } else {
            progressItem.setVisible(true);
        }
    }

}

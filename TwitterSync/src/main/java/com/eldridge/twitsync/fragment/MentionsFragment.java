package com.eldridge.twitsync.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.activity.TweetDetailActivity;
import com.eldridge.twitsync.adapter.TweetsAdapter;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.controller.TwitterApiController;
import com.eldridge.twitsync.message.beans.MentionsMessage;
import com.eldridge.twitsync.message.beans.TweetDetailMessage;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.InjectView;
import butterknife.Views;
import twitter4j.Status;

/**
 * Created by reldridge1 on 8/30/13.
 */
public class MentionsFragment extends SherlockListFragment {

    private static final String TAG = MentionsFragment.class.getSimpleName();

    @InjectView(android.R.id.list) ListView listView;

    private TweetsAdapter adapter;
    private Menu menu;
    private boolean isRefresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mentions, container, false);
        Views.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceController.getInstance(getSherlockActivity().getApplicationContext()).checkForExistingCredentials()) {
            TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getMentions();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "**** Mentions onResume called ****");
        BusController.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "**** Mentions onPause called ****");
        BusController.getInstance().unRegister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragments_mentions, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refreshMentions) {
            toggleRefreshMenuItem();
            isRefresh = true;
            TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getMentions();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Status s = (Status)l.getItemAtPosition(position);
        Intent detailIntent = new Intent(getSherlockActivity().getApplicationContext(), TweetDetailActivity.class);
        detailIntent.putExtra(TweetDetailActivity.DETAIL_KEY, s);
        startActivity(detailIntent);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void getMentions(final MentionsMessage mentionsMessage) {
        getSherlockActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapter == null) {
                    adapter = new TweetsAdapter(getSherlockActivity(), R.layout.tweet_item_layout, mentionsMessage.getMentions());
                } else {
                    List<Status> mentions = mentionsMessage.getMentions();
                    for (Status s : mentions) {
                        adapter.insert(s, 0);
                    }
                    adapter.notifyDataSetChanged();
                }
                getListView().setAdapter(adapter);
                if (isRefresh) {
                    toggleRefreshMenuItem();
                }
            }
        });
    }

    private void toggleRefreshMenuItem() {
        MenuItem progressItem = this.menu.findItem(R.id.menuProgress);
        MenuItem refresh = this.menu.findItem(R.id.refreshMentions);
        if (progressItem.isVisible()) {
            progressItem.setVisible(false);
            refresh.setVisible(true);
        } else {
            progressItem.setVisible(true);
            refresh.setVisible(false);
        }
    }

}

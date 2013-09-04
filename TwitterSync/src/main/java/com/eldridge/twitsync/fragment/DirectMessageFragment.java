package com.eldridge.twitsync.fragment;


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
import com.eldridge.twitsync.adapter.DirectMessageAdapter;
import com.eldridge.twitsync.adapter.TweetsAdapter;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.controller.TwitterApiController;
import com.eldridge.twitsync.message.beans.DirectMessagesMessage;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.InjectView;
import butterknife.Views;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * Created by reldridge1 on 8/30/13.
 */
public class DirectMessageFragment extends SherlockListFragment {

    private static final String TAG = DirectMessageFragment.class.getSimpleName();

    @InjectView(android.R.id.list) ListView listView;

    private DirectMessageAdapter adapter;
    private Menu menu;
    private boolean isRefresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_direct_messages, container, false);
        Views.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceController.getInstance(getSherlockActivity().getApplicationContext()).checkForExistingCredentials()) {
            TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getDirectMessages();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "**** DirectMessage onResume called ****");
        BusController.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "**** DirectMessage onPause called ****");
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

    @SuppressWarnings("unused")
    @Subscribe
    public void getDirectMessages(final DirectMessagesMessage directMessages) {
        getSherlockActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapter == null) {
                    adapter = new DirectMessageAdapter(getSherlockActivity(), R.layout.tweet_item_layout, directMessages.getMessages());
                } else {
                    List<DirectMessage> messages = directMessages.getMessages();
                    for (DirectMessage d : messages) {
                        adapter.insert(d, 0);
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

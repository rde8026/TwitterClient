package com.eldridge.twitsync.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.controller.BusController;
import com.eldridge.twitsync.controller.PreferenceController;
import com.eldridge.twitsync.controller.TwitterApiController;

import butterknife.InjectView;
import butterknife.Views;

/**
 * Created by reldridge1 on 8/30/13.
 */
public class DirectMessageFragment extends SherlockListFragment {

    private static final String TAG = DirectMessageFragment.class.getSimpleName();

    @InjectView(android.R.id.list) ListView listView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (PreferenceController.getInstance(getSherlockActivity().getApplicationContext()).checkForExistingCredentials()) {
            TwitterApiController.getInstance(getSherlockActivity().getApplicationContext()).getDirectMessages();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_direct_messages, container, false);
        Views.inject(this, v);
        return v;
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



}

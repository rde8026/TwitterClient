package com.eldridge.twitsync.activity;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.eldridge.twitsync.R;
import com.eldridge.twitsync.controller.BusController;

import twitter4j.Status;

/**
 * Created by ryaneldridge on 8/27/13.
 */
public class TweetDetailActivity extends SherlockFragmentActivity {

    private static final String TAG = TweetDetailActivity.class.getSimpleName();

    public static final String DETAIL_KEY = "DETAIL_KEY";
    private Status status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        status = (Status)getIntent().getSerializableExtra(DETAIL_KEY);
        if (status == null) {
            finish();
            overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
        } else {
            setContentView(R.layout.activity_detail_layout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusController.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusController.getInstance().unRegister(this);
    }

    public Status getStatus() {
        return status;
    }


}

package com.eldridge.twitsync.message.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Status;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class TimelineUpdateMessage implements Serializable {

    private ResponseList<Status> tweets;
    private boolean refresh;
    private boolean prepend;


    public TimelineUpdateMessage() {
    }

    public TimelineUpdateMessage(ResponseList<Status> tweets) {
        this.tweets = tweets;
        this.refresh = false;
        this.prepend = false;
    }

    public TimelineUpdateMessage(ResponseList<Status> tweets, boolean refresh) {
        this.tweets = tweets;
        this.refresh = refresh;
        prepend = false;
    }

    public TimelineUpdateMessage(ResponseList<Status> tweets, boolean refresh, boolean prepend) {
        this.tweets = tweets;
        this.refresh = refresh;
        this.prepend = prepend;
    }

    public ResponseList<Status> getTweets() {
        return tweets;
    }

    public void setTweets(ResponseList<Status> tweets) {
        this.tweets = tweets;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isPrepend() {
        return prepend;
    }

    public void setPrepend(boolean prepend) {
        this.prepend = prepend;
    }

}

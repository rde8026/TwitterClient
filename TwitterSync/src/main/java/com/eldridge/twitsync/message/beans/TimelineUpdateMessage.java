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

    private List<Status> tweets;
    private boolean refresh;
    private boolean prepend;


    public TimelineUpdateMessage() {
    }

    public TimelineUpdateMessage(List<Status> tweets) {
        this.tweets = tweets;
        this.refresh = false;
        this.prepend = false;
    }

    public TimelineUpdateMessage(List<Status> tweets, boolean refresh) {
        this.tweets = tweets;
        this.refresh = refresh;
        prepend = false;
    }

    public TimelineUpdateMessage(List<Status> tweets, boolean refresh, boolean prepend) {
        this.tweets = tweets;
        this.refresh = refresh;
        this.prepend = prepend;
    }

    public List<Status> getTweets() {
        return tweets;
    }

    public void setTweets(List<Status> tweets) {
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
package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

import twitter4j.Status;

/**
 * Created by reldridge1 on 8/19/13.
 */
public class TweetDetailMessage implements Serializable {

    private Status status;

    public TweetDetailMessage(Status status) {
        this.status = status;
    }

    public TweetDetailMessage() {

    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

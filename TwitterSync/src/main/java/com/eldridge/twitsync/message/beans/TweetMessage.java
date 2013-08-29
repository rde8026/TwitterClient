package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by reldridge1 on 8/29/13.
 */
public class TweetMessage implements Serializable {

    private boolean success;
    private TwitterException twitterException;
    private Status status;

    public TweetMessage(boolean success, Status status) {
        this.success = success;
        this.status = status;
    }

    public TweetMessage(boolean success, TwitterException twitterException) {
        this.success = success;
        this.twitterException = twitterException;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public TwitterException getTwitterException() {
        return twitterException;
    }

    public void setTwitterException(TwitterException twitterException) {
        this.twitterException = twitterException;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

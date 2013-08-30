package com.eldridge.twitsync.message.beans;

import java.io.Serializable;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by reldridge1 on 8/30/13.
 */
public class MentionsMessage implements Serializable {

    private boolean success;
    private List<Status> mentions;
    private TwitterException twitterException;

    public MentionsMessage(boolean success, List<Status> mentions) {
        this.success = success;
        this.mentions = mentions;
    }

    public MentionsMessage(boolean success, TwitterException twitterException) {
        this.success = success;
        this.twitterException = twitterException;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Status> getMentions() {
        return mentions;
    }

    public void setMentions(List<Status> mentions) {
        this.mentions = mentions;
    }

    public TwitterException getTwitterException() {
        return twitterException;
    }

    public void setTwitterException(TwitterException twitterException) {
        this.twitterException = twitterException;
    }
}

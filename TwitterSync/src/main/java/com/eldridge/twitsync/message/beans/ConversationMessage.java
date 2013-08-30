package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by ryaneldridge on 8/29/13.
 */
public class ConversationMessage implements Serializable {

    private boolean success;
    private ResponseList<Status> conversation;
    private TwitterException twitterException;

    public ConversationMessage(boolean success, ResponseList<Status> conversation) {
        this.success = success;
        this.conversation = conversation;
    }

    public ConversationMessage(boolean success, TwitterException twitterException) {
        this.success = success;
        this.twitterException = twitterException;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResponseList<Status> getConversation() {
        return conversation;
    }

    public void setConversation(ResponseList<Status> conversation) {
        this.conversation = conversation;
    }

    public TwitterException getTwitterException() {
        return twitterException;
    }

    public void setTwitterException(TwitterException twitterException) {
        this.twitterException = twitterException;
    }
}

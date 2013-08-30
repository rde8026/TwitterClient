package com.eldridge.twitsync.message.beans;

import java.io.Serializable;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.TwitterException;

/**
 * Created by reldridge1 on 8/30/13.
 */
public class DirectMessagesMessage implements Serializable {

    private boolean success;
    private List<DirectMessage> messages;
    private TwitterException twitterException;

    public DirectMessagesMessage(boolean success, List<DirectMessage> messages) {
        this.success = success;
        this.messages = messages;
    }

    public DirectMessagesMessage(boolean success, TwitterException twitterException) {
        this.success = success;
        this.twitterException = twitterException;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<DirectMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DirectMessage> messages) {
        this.messages = messages;
    }

    public TwitterException getTwitterException() {
        return twitterException;
    }

    public void setTwitterException(TwitterException twitterException) {
        this.twitterException = twitterException;
    }
}

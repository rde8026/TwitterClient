package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

import twitter4j.User;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class TwitterUserMessage implements Serializable {

    private User user;

    public TwitterUserMessage(User user) {
        this.user = user;
    }

    public TwitterUserMessage() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

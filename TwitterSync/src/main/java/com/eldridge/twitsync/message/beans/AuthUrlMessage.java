package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class AuthUrlMessage implements Serializable {

    private String authorizationUrl;

    public AuthUrlMessage() {

    }

    public AuthUrlMessage(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

}

package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class AuthorizationErrorMessage extends ErrorMessage implements Serializable {

    public AuthorizationErrorMessage(int code, String message) {
        super(message, code);
    }

    public AuthorizationErrorMessage(String message, int code) {
        super(message, code);
    }

}

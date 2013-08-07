package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class ErrorMessage implements Serializable {

    private String message;
    private int code;

    public ErrorMessage() {

    }

    public ErrorMessage(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}

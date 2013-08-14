package com.eldridge.twitsync.rest.endpoints.payload;

import java.io.Serializable;

/**
 * Created by ryaneldridge on 8/13/13.
 */
public class StatusUpdatePayload implements Serializable {

    private String twitterId;
    private String messageId;
    private String deviceId;

    public StatusUpdatePayload(String twitterId, String messageId, String deviceId) {
        this.twitterId = twitterId;
        this.messageId = messageId;
        this.deviceId = deviceId;
    }

    public StatusUpdatePayload() {

    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

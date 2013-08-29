package com.eldridge.twitsync.beans;

import java.io.Serializable;

import twitter4j.URLEntity;

/**
 * Created by reldridge1 on 8/29/13.
 */
public class MediaUrlEntity implements Serializable {

    private URLEntity urlEntity;
    private boolean photo;

    public MediaUrlEntity() {

    }

    public MediaUrlEntity(URLEntity entity, boolean photo) {
        this.urlEntity = entity;
        this.photo = photo;
    }

    public URLEntity getUrlEntity() {
        return urlEntity;
    }

    public void setUrlEntity(URLEntity urlEntity) {
        this.urlEntity = urlEntity;
    }

    public boolean isPhoto() {
        return photo;
    }

    public void setPhoto(boolean photo) {
        this.photo = photo;
    }
}

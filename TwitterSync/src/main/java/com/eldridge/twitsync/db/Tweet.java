package com.eldridge.twitsync.db;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

/**
 * Created by reldridge1 on 8/6/13.
 */
@Table(name = "tweets")
public class Tweet extends Model implements Serializable {

    public Tweet() {
        super();
    }

    @Column(name = "tweetId", notNull = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public Long tweetId;

    @Column(name = "timestamp", notNull = true)
    public Long timestamp;

    @Column(name = "json", notNull = true)
    public String json;

}

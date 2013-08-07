package com.eldridge.twitsync.db;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by reldridge1 on 8/6/13.
 */
@Table(name = "tweets")
public class Tweet extends Model {

    public Tweet() {
        super();
    }

    @Column(name = "tweetId", notNull = true, unique = true, onUniqueConflict = Column.ConflictAction.ABORT)
    public Long tweetId;

    @Column(name = "timestamp", notNull = true)
    public Long timestamp;

    @Column(name = "json", notNull = true)
    public String json;

}

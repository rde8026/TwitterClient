package com.eldridge.twitsync.message.beans;

import java.io.Serializable;

/**
 * Created by ryaneldridge on 8/5/13.
 */
public class ScrollMessage implements Serializable {

    private boolean moveToTop;

    public ScrollMessage(boolean moveToTop) {
        this.moveToTop = moveToTop;
    }

    public boolean isMoveToTop() {
        return moveToTop;
    }

    public void setMoveToTop(boolean moveToTop) {
        this.moveToTop = moveToTop;
    }

}

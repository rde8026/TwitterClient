package com.eldridge.twitsync.controller;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class BusController {

    private static BusController instance;

    private static Bus mBus;

    public static BusController getInstance() {
        if (instance == null) {
            synchronized (BusController.class) {
                instance = new BusController();
                mBus = new Bus(ThreadEnforcer.ANY);
            }
        }
        return instance;
    }

    public void register(Object obj) {
        mBus.register(obj);
    }

    public void unRegister(Object obj) {
        mBus.unregister(obj);
    }

    public void postMessage(Object obj) {
        mBus.post(obj);
    }

}

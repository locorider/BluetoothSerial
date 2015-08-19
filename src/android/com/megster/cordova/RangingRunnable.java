package com.megster.cordova;

/**
 * Created by joser on 18.08.2015.
 */
public class RangingRunnable implements Runnable {

    private volatile boolean running = false;
    private final long pollDelay = 250; //250ms;

    @Override
    public void run() {
        while(running) {

        }
    }
}
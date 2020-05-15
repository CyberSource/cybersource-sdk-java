package com.cybersource.ws.client;

/**
 * To calculates time duration for internal method flows and total response time
 */
public class MyStopWatch {

    private long txStartTime = 0;
    private long methodStartTime = 0;

    /**
     * start the timer
     */
    public void start() {
        this.txStartTime = System.currentTimeMillis();
        this.methodStartTime = txStartTime;
    }

    /**
     * get total time taken for method execution
     *
     * @return long
     */
    public long getMethodElapsedTime() {
        long timeNow = System.currentTimeMillis();
        long elapsed = (timeNow - methodStartTime);
        methodStartTime = timeNow;
        return elapsed;
    }

    /**
     * Get total time taken for transaction to be completed
     *
     * @return long
     */
    public long getElapsedTimeForTransaction() {
        return ((System.currentTimeMillis() - txStartTime));
    }
}

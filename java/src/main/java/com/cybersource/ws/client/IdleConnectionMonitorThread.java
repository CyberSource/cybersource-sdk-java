package com.cybersource.ws.client;

import org.apache.http.conn.HttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

public  class IdleConnectionMonitorThread extends Thread {

    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;
    private long sleepTime;
    private long idleTime;

    public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr, long sleepTime, long idleTime) {
        super();
        this.connMgr = connMgr;
        this.sleepTime = sleepTime;
        this.idleTime = idleTime;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    long idleTime = getIdleTime();
                    wait(getSleepTime());
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(idleTime, TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException ex) {
            shutdown();
        }
    }

    public long getIdleTime() {
        return idleTime;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }

}

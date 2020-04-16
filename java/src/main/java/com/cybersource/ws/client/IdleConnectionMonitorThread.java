package com.cybersource.ws.client;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;
/**
 * Class creates daemon thread to monitor and kill the idle/stale/expired
 * connections in the connection pool.
 */
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
                    PoolingHttpClientConnectionManager poolConnMgr = (PoolingHttpClientConnectionManager) connMgr;
                    System.out.println("before closing expired and idl conn, stats is "+poolConnMgr.getTotalStats() + "Current Time: "+ java.time.LocalTime.now());
                    long startTimer = System.currentTimeMillis();
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(idleTime, TimeUnit.MILLISECONDS);
                    System.out.println("Took " + (System.currentTimeMillis() - startTimer) + "ms to close expired and idl conn, now stats is "+poolConnMgr.getTotalStats());
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

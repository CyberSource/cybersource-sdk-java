package com.cybersource.ws.client;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

/**
 * Creates daemon thread to monitor and kill the idle/stale/expired
 * connections in the connection pool.
 */
public class IdleConnectionMonitorThread extends Thread {

    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;
    private long sleepTime;
    private long idleTime;

    /**
     *
     * @param connMgr   - HttpClientConnectionManager
     * @param sleepTime - long
     * @param idleTime  - long
     */
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
                    //System.out.println("before closing expired and idl conn, stats is "+poolConnMgr.getTotalStats() + "Current Time: "+ java.time.LocalTime.now());
                    //long startTimer = System.currentTimeMillis();
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(idleTime, TimeUnit.MILLISECONDS);
                    //System.out.println("Took " + (System.currentTimeMillis() - startTimer) + "ms to close expired and idl conn, now stats is "+poolConnMgr.getTotalStats());
                }
            }
        } catch (InterruptedException ex) {
            shutdown();
        }
    }

    /**
     * Specifies the time duration in milliseconds that a connection can be idle before it is evicted from the pool.
     *
     * @return long
     */
    public long getIdleTime() {
        return idleTime;
    }

    /**
     * Specifies time duration in milliseconds between "sweeps" by the "idle connection" evictor thread.
     * This thread will check if any idle/expired/stale connections are available in pool and evict it.
     *
     * @return long
     */
    public long getSleepTime() {
        return sleepTime;
    }

    /**
     * shutdown the cleaner thread
     */
    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }

}

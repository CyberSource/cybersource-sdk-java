package com.cybersource.ws.client;

public class MyStopWatch {

	  private long txStartTime = 0;
	  private long methodStartTime = 0;
	  
	  public void start() {
		this.txStartTime = System.currentTimeMillis();
	    this.methodStartTime = txStartTime;
	  }

	  public long getMethodElapsedTime() {
		long timeNow = System.currentTimeMillis();
	    long elapsed  = (timeNow - methodStartTime);
	    methodStartTime = timeNow;
	    return elapsed;
	  }
	  
	  public long getElapsedTimeForTransaction() {
		  return ((System.currentTimeMillis() - txStartTime));
	  }
}

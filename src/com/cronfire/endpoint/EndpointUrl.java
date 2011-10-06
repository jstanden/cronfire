package com.cronfire.endpoint;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class EndpointUrl implements Delayed {
	private long delayUntil;
	
	private String url;
	private int cooldownSecs = 60;
	
	private long cumulativeHits = 0L;
	private long cumulativeRuntimeMs = 0L;
	
	private final Date dateTime;
	
	// [TODO] Can run parallel?
	// [TODO] Tags
	
	public EndpointUrl(String url) {
		this.url = url;
		this.dateTime = new Date();
	}
	
	public String getUrl() {
		return url;
	}

	@Override
	public int compareTo(Delayed delayed) {
		EndpointUrl endpoint = (EndpointUrl)delayed;
	      if (this.delayUntil < endpoint.delayUntil)
	         return -1;
	      if (this.delayUntil > endpoint.delayUntil)
	         return 1;
	      return this.dateTime.compareTo(endpoint.dateTime);		
	}

	public void delayBySecs(int secs) {
		this.delayUntil = System.currentTimeMillis() + (secs * 1000L);
	}
	
	public void logRuntime(long ms) {
		cumulativeHits++;
		cumulativeRuntimeMs += ms;
	}
	
	public double getAverageRuntime() {
		return cumulativeRuntimeMs/cumulativeHits;
	}
	
	@Override
	public long getDelay(TimeUnit timeUnit) {
		return timeUnit.convert(this.delayUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public int getCooldownSecs() {
		return cooldownSecs;
	}

	public void setCooldownSecs(int cooldownSecs) {
		this.cooldownSecs = cooldownSecs;
	}
	
}

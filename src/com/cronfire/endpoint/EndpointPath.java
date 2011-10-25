package com.cronfire.endpoint;

import java.util.concurrent.atomic.AtomicInteger;


public class EndpointPath {
	private String key;
	private String suffix;
	private String interval;
	private String tag;
	private Integer max = 0;
	private AtomicInteger runCounter = new AtomicInteger();
	
	public EndpointPath(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public String getInterval() {
		return this.interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public Integer getMax() {
		return max;
	}
	public void setMax(Integer max) {
		this.max = max;
	}
	
	public AtomicInteger getRunCounter() {
		return runCounter;
	}
	
}

package com.cronfire.settings;


public class EndpointPath {
	private String key;
	private String path;
	private String interval;
	private Integer max;
	
	public EndpointPath(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getInterval() {
		return this.interval;
	}
	
	public void setInterval(String interval) {
		this.interval = interval;
	}
	
	public Integer getMax() {
		return max;
	}
	public void setMax(Integer max) {
		this.max = max;
	}
	
}

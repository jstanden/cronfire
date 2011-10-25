package com.cronfire.endpoint;

import java.util.concurrent.DelayQueue;

public class EndpointHost {
	private String baseUrl;
	
	private DelayQueue<EndpointUrl> queue = new DelayQueue<EndpointUrl>();
	
	public EndpointHost(String url) {
		this.baseUrl = url;
	}
	
	public String getBaseUrl() {
		return this.baseUrl;
	}
	
	public DelayQueue<EndpointUrl> getQueue() {
		return queue;
	}
}

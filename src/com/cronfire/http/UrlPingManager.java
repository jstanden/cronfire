package com.cronfire.http;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.concurrent.DelayQueue;

import com.cronfire.CronFireSettings;
import com.cronfire.endpoint.EndpointPath;
import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.queue.CronFireQueue;

public class UrlPingManager {
	
	public static void pingUrl(final EndpointUrl endpoint) {
		final CronFireQueue queue = CronFireQueue.getInstance();

		new Thread(new Runnable() {
			public void run() {
				URLConnection connection = null;
				
				try {
					endpoint.setRunning(true);
					
					long startTime = System.currentTimeMillis();
					
					String url = endpoint.getUrl();
					
					connection = new URL(url).openConnection();
					((HttpURLConnection) connection).setInstanceFollowRedirects(true);
					connection.setConnectTimeout(300000); // 5 minutes
					connection.connect();
					
					// [TODO] Check status code - this is used to block until the connection finishes
					@SuppressWarnings("unused")
					int status = ((HttpURLConnection) connection).getResponseCode();
					
					endpoint.logRuntime(System.currentTimeMillis() - startTime);
					
					//SocketTimeoutException
					endpoint.setMissing(false);
					
				} catch(UnknownHostException e) {
					// [TODO] Log!!
					//System.out.println("Invalid URL in rotation: " + endpoint.getUrl());
					endpoint.setMissing(true);
					
					//CronFireSettings.getEndpoints().remove(endpoint.getUrl() + endpoint.getPath().getKey());
					
				} catch(SocketTimeoutException e) {
					e.printStackTrace();
					
				} catch(Exception e) {
					e.printStackTrace();
					
				} finally {
					endpoint.delayBySecs(endpoint.getNextIntervalAsSecs());
					endpoint.setRunning(false);
					
					try {
						((HttpURLConnection) connection).disconnect();
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					try {
						DelayQueue<EndpointUrl> hostQueue = endpoint.getHost().getQueue();
						
						// Add the endpoint back to the host's queue
						hostQueue.add(endpoint);
						
						// Schedule the next job from the host
						if(!hostQueue.isEmpty()) {
							EndpointUrl nextEndpoint = hostQueue.peek();
							hostQueue.remove(nextEndpoint);
							queue.getQueue().add(nextEndpoint);
						}
						
						EndpointPath path = endpoint.getPath();
						path.getRunCounter().decrementAndGet();
						
						try {
							CronFireSettings.getPathRunningCounts().get(path.getKey()).decrementAndGet();
						} catch(NullPointerException npe) {
							npe.printStackTrace();
						}
						
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

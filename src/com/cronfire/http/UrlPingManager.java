package com.cronfire.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import com.cronfire.CronFireSettings;
import com.cronfire.endpoint.EndpointPath;
import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.queue.CronFireQueue;

public class UrlPingManager {
	
	public static void pingUrl(final EndpointUrl endpoint) {
		final CronFireQueue queue = CronFireQueue.getInstance();

		new Thread(new Runnable() {
			public void run() {
				try {
					endpoint.setRunning(true);
					
					long startTime = System.currentTimeMillis();
					
					String url = endpoint.getUrl();
					
					URLConnection connection = new URL(url).openConnection();
					
					connection.connect();
					
					// [TODO] Check status code - this is used to block until the connection finishes
					@SuppressWarnings("unused")
					int status = ((HttpURLConnection) connection).getResponseCode();
					
					endpoint.logRuntime(System.currentTimeMillis() - startTime);
					
					// [TODO] Reschedule -- slow crons should be bumped in queue (keep an average)
					endpoint.delayBySecs(endpoint.getNextIntervalAsSecs());
					
				} catch(UnknownHostException e) {
					// [TODO] Don't requeue if the host is invalid
					//System.out.println("Invalid URL in rotation: " + endpoint.getUrl());
					
				} catch(Exception e) {
					e.printStackTrace();
					
				} finally {
					endpoint.setRunning(false);
					
					
					try {
						DelayQueue<EndpointUrl> hostQueue = endpoint.getHost().getQueue();
						
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
						} catch(Exception e) {
							e.printStackTrace();
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

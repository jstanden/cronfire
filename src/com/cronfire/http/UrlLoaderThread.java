package com.cronfire.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.queue.CronFireQueue;

public class UrlLoaderThread {
	public void pingUrl(final EndpointUrl endpoint) {
		new Thread(new Runnable() {
			public void run() {
				CronFireQueue queue = CronFireQueue.getInstance();
				try {
					long startTime = System.currentTimeMillis();
					
					String url = endpoint.getUrl();
					
					// [TODO] handle query params
					URLConnection connection = new URL(url).openConnection();
					
					//System.out.println("Starting: " + url);
					
					connection.connect();
					
					// [TODO] Check status code
					//int status = ((HttpURLConnection) connection).getResponseCode();
					
					// [TODO] Reschedule -- slow crons should be bumped in queue (keep an average)
					long elapsedTime = System.currentTimeMillis() - startTime;
					endpoint.logRuntime(elapsedTime);
					
					//System.out.println("Finished: " + url + " Status: " + status + " Elapsed: " + elapsedTime + "ms");

					endpoint.delayBySecs(endpoint.getCooldownSecs());
					queue.add(endpoint);
					
				} catch(UnknownHostException e) {
					// [TODO] Don't requeue if the host is invalid
					//System.out.println("Invalid URL in rotation: " + endpoint.getUrl());
					
					//endpoint.delayBySecs(3600);
					//queue.add(endpoint);
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}

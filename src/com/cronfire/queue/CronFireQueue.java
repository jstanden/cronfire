package com.cronfire.queue;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;

import com.cronfire.CronFireSettings;
import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.http.UrlPingManager;
import com.cronfire.load_manager.LoadManager;
import com.cronfire.settings.EndpointPath;

public class CronFireQueue {
	static private CronFireQueue instance;
	
	private boolean isPaused = false;
	private DelayQueue<EndpointUrl> queue = new DelayQueue<EndpointUrl>();
	private LoadManager loadManager = LoadManager.getInstance();

	static public CronFireQueue getInstance() {
		if(null == instance) {
			instance = new CronFireQueue();
		}
		return instance;
	}
	
	private CronFireQueue() { /* No instantiation */ }
	
	public void add(EndpointUrl endpoint) {
		if(!queue.contains(endpoint)) {
			queue.add(endpoint);
		}
	}
	
	public void remove(EndpointUrl endpoint) {
		if(queue.contains(endpoint)) {
			queue.remove(endpoint);
		}
	}
	
	public Iterator<EndpointUrl> iterator() {
		return queue.iterator();
	}
	
	public boolean isPaused() {
		return this.isPaused;
	}
	
	public void pause(boolean b) {
		isPaused = b;
	}
	
	public void empty() {
		queue.clear();
	}
	
	public void start() {
		new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						if(isPaused) {
							Thread.sleep(500L);
							continue;
						}

						double loadAvg = loadManager.getCurrentLoad();						
						
						// Check the thread cap
						int maxThreads = CronFireSettings.getSettingInt("max_http_threads", 10);
						int numThreads = Thread.activeCount() - 3; // Compensate for built-in
						
						if(numThreads >= maxThreads) {
							Thread.sleep(500L);
							continue;
						}
						
						// Check the current load
						if(loadAvg > CronFireSettings.getSettingDouble("loadavg_throttle", 5.0)) {
							Thread.sleep(500L);
							continue;
						}
						
						// Otherwise, process the next element when ready
						EndpointUrl endpoint = queue.take();

						int max = endpoint.getPath().getMax();
						
						if(max > 0) {						
							int count = CronFireSettings.getPathRunningCounts().get(endpoint.getPath().getKey()).get();
							
							//System.out.print(count + " / " + max + " " + endpoint.getPath().getKey() + " " + endpoint.getPath().getTag() + "... ");
							
							if(count >= max) {
								endpoint.delayBySecs(Math.max(5, Math.round(endpoint.getAverageRuntime() / 1000)));
								queue.add(endpoint);
								continue;
							}
						}
						
						EndpointPath path = endpoint.getPath();
						path.getRunCounter().incrementAndGet();
						try {
							CronFireSettings.getPathRunningCounts().get(path.getKey()).incrementAndGet();
						} catch(Exception e) {
							e.printStackTrace();
						}
						UrlPingManager.pingUrl(endpoint);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

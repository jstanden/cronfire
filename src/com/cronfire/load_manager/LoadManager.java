package com.cronfire.load_manager;

import org.hyperic.sigar.Sigar;

import com.cronfire.CronFireSettings;

public class LoadManager {
	static private LoadManager instance;
	
	Sigar sigar;
	double currentLoad = 0;
	
	private LoadManager() {
		sigar = new Sigar();
	}
	
	static public LoadManager getInstance() {
		if(null == instance) {
			instance = new LoadManager();
		}
		
		return instance;
	}
	
	public double getCurrentLoad() {
		return currentLoad;
	}
	
	public void start() {
		new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						double loads[] = sigar.getLoadAverage();
						currentLoad = loads[0];
						
						Thread.sleep(Integer.valueOf(CronFireSettings.getSetting("loadavg_poll_secs","0")) * 1000L);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

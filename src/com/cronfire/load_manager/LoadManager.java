package com.cronfire.load_manager;

import java.io.File;
import java.util.Scanner;

import org.hyperic.sigar.Sigar;

import com.cronfire.CronFireSettings;
import com.cronfire.commands.ReloadCommand;

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
						
						boolean hasNewConfig = false;
						
						// Check config file modified times
						try {
							long config_file_mtime = CronFireSettings.getSettingLong("config_file_mtime", 0L);
							String config_filename = CronFireSettings.getSetting("config_file", null);
							File config_file = new File(config_filename);
							if(config_file.lastModified() > config_file_mtime) {
								hasNewConfig = true;
								CronFireSettings.setSetting("config_file_mtime", Long.toString(config_file.lastModified()));
							} 
						} catch(Exception e) {
						}

						// Check URLs file modified times
						try {
							long urls_file_mtime = CronFireSettings.getSettingLong("urls_file_mtime", 0L);
							String urls_filename = CronFireSettings.getSetting("urls_file", null);
							File urls_file = new File(urls_filename);
							if(urls_file.lastModified() > urls_file_mtime) {
								hasNewConfig = true;
								CronFireSettings.setSetting("urls_file_mtime", Long.toString(urls_file.lastModified()));
							}
						} catch(Exception e) {
						}
						
						if(hasNewConfig) {
							new ReloadCommand().execute(new Scanner(""));
						}
						
						Thread.sleep(CronFireSettings.getSettingInt("loadavg_poll_secs", 30) * 1000L);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

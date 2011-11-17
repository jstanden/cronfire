package com.cronfire.commands;

import java.util.Scanner;

import com.cronfire.CronFireSettings;
import com.cronfire.queue.CronFireQueue;

public class ReloadCommand implements Command {
	CronFireQueue queue = CronFireQueue.getInstance();
	
	public void execute(Scanner scanner) {
		boolean isReloading = CronFireSettings.getSettingBoolean("_internal_is_reloading", false);
		
		// Don't reload if we're already reloading.
		if(isReloading)
			return;
		
		// Pause (save original state)
		boolean isPaused = queue.isPaused();
		queue.pause(true);
		CronFireSettings.setSetting("_internal_is_reloading", "true");
		
		// Wait for all jobs to finish
		int numThreads = Thread.activeCount() - 3; // Compensate for built-in
		int numWaits = 0;
		
		if(numThreads > 0) {
			System.out.println("Waiting for running jobs to finish...");
			while(numThreads > 0) {
				numThreads = Thread.activeCount() - 3;
				try {
					Thread.sleep(250);
				} catch(Exception e) {}
				
				if(numWaits > 1200) {
					System.out.println("Waiting too long for jobs to finish... aborting RELOAD");
					CronFireSettings.setSetting("_internal_is_reloading", "false");
					return;
				}
			}
			
			System.out.println("Reloading...");
		}
		
		// Reload config and URL files (merge changes)
		queue.empty();
		CronFireSettings.loadConfigFile(CronFireSettings.getSetting("config_file"));
		CronFireSettings.loadUrls(CronFireSettings.getSetting("urls_file"));
		
		// Unpause (or resume original state)
		queue.pause(isPaused);
		
		System.out.println("Reloaded...");
		CronFireSettings.setSetting("_internal_is_reloading", "false");
	}

}

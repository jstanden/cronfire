package com.cronfire.commands;

import java.util.Scanner;

import com.cronfire.CronFireSettings;
import com.cronfire.queue.CronFireQueue;

public class ReloadCommand implements Command {
	CronFireQueue queue = CronFireQueue.getInstance();
	
	public void execute(Scanner scanner) {
		// Reload config and URL files (merge changes)
		queue.empty();
		CronFireSettings.getPathRunningCounts().clear();
		CronFireSettings.loadConfigFile(CronFireSettings.getSetting("config_file"));
		CronFireSettings.loadUrls(CronFireSettings.getSetting("urls_file"));
		System.out.println("Reloaded...");
	}

}

package com.cronfire.commands;

import java.util.Iterator;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.cronfire.CronFireSettings;

public class StatsCommand implements Command {

	public void execute(Scanner scanner) {
		int maxThreads = CronFireSettings.getSettingInt("max_http_threads", 10);
		int numThreads = Thread.activeCount() - 3; // Compensate for built-in
		
		System.out.println("Threads: " + numThreads + " / " + maxThreads);
		
		for(Iterator<Entry<String,AtomicInteger>> i = CronFireSettings.getPathRunningCounts().entrySet().iterator(); i.hasNext(); ) {
			Entry<String,AtomicInteger> entry = i.next();
			String key = entry.getKey();
			AtomicInteger count = entry.getValue();
			System.out.println(" - " + key + " = " + count);
		}
	}

}

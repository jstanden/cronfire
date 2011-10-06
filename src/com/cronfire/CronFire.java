package com.cronfire;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.load_manager.LoadManager;
import com.cronfire.queue.CronFireQueue;

public class CronFire {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CronFireSettings.loadConfigFile("example.config.xml");
		CronFireSettings.loadUrls("example.urls.txt");
		
		LoadManager loadManager = LoadManager.getInstance();
		loadManager.start();

		CronFireQueue queue = CronFireQueue.getInstance();
		queue.start();
		
		// [TODO] Load up URL variations (cron.maint, cron.parser)
		// [TODO] Relative start times (e.g. cron.maint=midnight)
		// [TODO] Load up URLs with default reload time (cron.maint=3600, cron.pop3=60, cron.parser=60)
		// [TODO] POP3/Parser jobs with ?max_pop3=30, etc.
		
		String input = "";
		
		// [TODO] Use scanner for token processing w/ arguments
		while(!input.equalsIgnoreCase("quit")) {
			System.out.print("> ");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			try {
				input = in.readLine();
				
				if(input.equalsIgnoreCase("help")) {
					System.out.println("HELP");
					System.out.println("LIST");
					System.out.println("LOAD");
					System.out.println("STATS");
					System.out.println("RELOAD");
					System.out.println("RESUME");
					System.out.println("PAUSE");
					System.out.println("QUIT");
					
				} else if(input.equalsIgnoreCase("list")) {
					// [TODO] sort
					for(Iterator<EndpointUrl> i = queue.iterator(); i.hasNext(); ) {
						EndpointUrl endpoint = i.next();
						System.out.println("* " + endpoint.getUrl() + " [" + endpoint.getDelay(TimeUnit.SECONDS) + "s] " + endpoint.getAverageRuntime() + "ms (n=" + endpoint.getRunCount() + ")");
					}
					
					
				} else if(input.equalsIgnoreCase("load")) {
					LoadManager loadavg = LoadManager.getInstance();
					
					// Formatters
					NumberFormat loadFormatter;					
					loadFormatter = DecimalFormat.getNumberInstance();
					loadFormatter.setMaximumFractionDigits(2);
					
					System.out.println(
						"Current Load: " + loadFormatter.format(loadavg.getCurrentLoad()) 
						+ " (throttle: " + CronFireSettings.getSettingDouble("loadavg_throttle", 5.0) + ")"
					);
					
				} else if(input.equalsIgnoreCase("reload")) {
					// [TODO] Reload config and URL files (merge changes?)
					queue.empty();
					// [TODO] these URLs need to come from settings
					CronFireSettings.loadConfigFile("example.config.xml");
					CronFireSettings.loadUrls("example.urls.txt");
					System.out.println("Reloaded...");
					
				} else if(input.equalsIgnoreCase("pause") || input.equalsIgnoreCase("stop")) {
					queue.pause(true);
					System.out.println("Paused.");
					
				} else if(input.equalsIgnoreCase("resume") || input.equalsIgnoreCase("start")) {
					queue.pause(false);
					System.out.println("Resumed.");
					
				} else if(input.equalsIgnoreCase("stats")) {
					System.out.println("Threads: " + Thread.activeCount());
					
				} else if(input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
					
				} else {
					System.out.println("Unknown command: " + input);
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}

}

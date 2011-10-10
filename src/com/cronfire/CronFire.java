package com.cronfire;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map.Entry;
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
		
		// [TODO] Relative start times (e.g. cron.maint=midnight)
		
		String input = "";
		
		// [TODO] Use scanner for token processing w/ arguments
		while(!input.equalsIgnoreCase("quit") && !input.equalsIgnoreCase("exit")) {
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
					
					for(Iterator<Entry<String,EndpointUrl>> i = CronFireSettings.getEndpoints().entrySet().iterator(); i.hasNext(); ) {
						Entry<String,EndpointUrl> entry = i.next();
						EndpointUrl endpoint = entry.getValue();
						
						String url = endpoint.getUrl();
						
						// Strip query args // [TODO] Verbose
						if(-1 != url.lastIndexOf("?"))
							url = url.substring(0,url.lastIndexOf("?"));
						
						System.out.print("* " + url);
						
						if(endpoint.isRunning()) {
							System.out.print(" [running] ");
						} else if(queue.isPaused()) {
							System.out.print(" [paused] ");
						} else {
							long secs = endpoint.getDelay(TimeUnit.SECONDS);
							
							if(secs > 0) {
								System.out.print(" [" + secs + "s] "); 
							} else {
								System.out.print(" [waiting] ");
							}
						}
						
						System.out.print(endpoint.getAverageRuntime() + "ms (n=" + endpoint.getRunCount() + ")");
						System.out.println();
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
					// Reload config and URL files (merge changes)
					queue.empty();
					CronFireSettings.getPathRunningCounts().clear();
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
					int maxThreads = CronFireSettings.getSettingInt("max_http_threads", 10);
					int numThreads = Thread.activeCount() - 3; // Compensate for built-in
					
					System.out.println("Threads: " + numThreads + " / " + maxThreads);
					
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

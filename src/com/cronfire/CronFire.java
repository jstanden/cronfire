package com.cronfire;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.load_manager.LoadManager;
import com.cronfire.queue.CronFireQueue;

public class CronFire {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(2 != args.length) {
			System.err.println("Usage: cronfire.jar <config.xml> <hosts.txt>");
			System.exit(1);
		}
		
		CronFireSettings.loadConfigFile(args[0]);
		CronFireSettings.loadUrls(args[1]);
		
		LoadManager loadManager = LoadManager.getInstance();
		loadManager.start();

		CronFireQueue queue = CronFireQueue.getInstance();
		queue.start();
		
		// [TODO] Relative start times (e.g. cron.maint=midnight)

		// CLI
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Scanner scanner = new Scanner(in);
		String token = "";
		
		while(true) {
			System.out.print("> ");
			try {
				token = scanner.next();

				if(token.isEmpty()) {
					System.out.println("Empty");
					continue;
				}
				
				@SuppressWarnings("rawtypes")
				Class commandClass = null;
				
				try {
					commandClass = Class.forName("com.cronfire.commands." + WordUtils.capitalize(token) + "Command");
					Command command = (Command) commandClass.newInstance();
					Scanner command_args = new Scanner(scanner.nextLine().trim());
					command.execute(command_args);
					command_args.close();
					
				} catch(ClassNotFoundException cnfe) {
					System.out.println("Unknown command: " + token);
					token = "";
					scanner.nextLine();
					continue;
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}

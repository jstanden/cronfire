package com.cronfire.commands;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Scanner;

import com.cronfire.CronFireSettings;
import com.cronfire.load_manager.LoadManager;

public class LoadCommand implements Command {

	public void execute(Scanner scanner) {
		LoadManager loadavg = LoadManager.getInstance();
		
		// Formatters
		NumberFormat loadFormatter;					
		loadFormatter = DecimalFormat.getNumberInstance();
		loadFormatter.setMaximumFractionDigits(2);
		
		System.out.println(
			"Current Load: " + loadFormatter.format(loadavg.getCurrentLoad()) 
			+ " (throttle: " + CronFireSettings.getSettingDouble("loadavg_throttle", 5.0) + ")"
		);
	}

}

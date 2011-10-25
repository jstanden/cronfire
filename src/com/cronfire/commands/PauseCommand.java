package com.cronfire.commands;

import java.util.Scanner;

import com.cronfire.queue.CronFireQueue;

public class PauseCommand implements Command {
	CronFireQueue queue = CronFireQueue.getInstance();
	
	public void execute(Scanner scanner) {
		queue.pause(true);
		System.out.println("Paused.");
	}

}

package com.cronfire.commands;

import java.util.Scanner;

import com.cronfire.queue.CronFireQueue;

public class ResumeCommand implements Command {
	CronFireQueue queue = CronFireQueue.getInstance();
	
	public void execute(Scanner scanner) {
		// [TODO] Scheduled resume (secs)
		queue.pause(false);
		System.out.println("Resumed.");
	}

}

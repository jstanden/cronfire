package com.cronfire.commands;

import java.util.Scanner;

public class QuitCommand implements Command {

	public void execute(Scanner scanner) {
		System.out.println("Exiting...");
		System.exit(0);
	}

}

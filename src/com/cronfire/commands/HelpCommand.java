package com.cronfire.commands;

import java.util.Scanner;

public class HelpCommand implements Command {
	public void execute(Scanner scanner) {
		// [TODO] Load this list dynamically
		System.out.println("HELP");
		System.out.println("LIST");
		System.out.println("LOAD");
		System.out.println("STATS");
		System.out.println("RELOAD");
		System.out.println("RESUME");
		System.out.println("PAUSE");
		System.out.println("QUIT");
	}
}

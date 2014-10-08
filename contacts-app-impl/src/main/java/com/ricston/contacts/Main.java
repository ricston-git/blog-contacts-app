package com.ricston.contacts;

import java.util.Scanner;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * This is meant to (manually) test drive / explore the bunya-rest project
 * 
 */
public class Main {

	public final static String QUITTING_MSG = "So long!";
	public final static String QUIT_CMD_MSG = "'exit' or 'quit' to stop server";

	public static void main(String[] args) throws Exception {
		int port = 4433;
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		
		
		Server server = new Server(port);

		server.setHandler(context);
		
		ServletHolder jerseyServlet = context.addServlet(
				org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitParameter("javax.ws.rs.Application", "com.ricston.contacts.app.ContactsApp");
		jerseyServlet.setInitOrder(5);


		server.start();
		System.out.println("Server listening on port " + port);
		System.out.println(QUIT_CMD_MSG);
		Scanner scanner = new Scanner(System.in);
		
		String cmd;
		do {
			cmd = scanner.nextLine();
		} while(!isExitCmd(cmd));
		scanner.close();
		server.stop();
		System.out.println(QUITTING_MSG);
		System.exit(0);
	}

	protected static boolean isExitCmd(String cmd) {
		return "exit".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd);
	}

}

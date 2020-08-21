package org.atharvashah.butler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atharvashah.butler.utils.properties.PropertiesManager;

public class HttpServer {

	private static final Logger logger = LogManager.getLogger(HttpServer.class.getName());
	private boolean killswitch = false;

	/**
	 * Will open an http server on portnumber as set in butler.properties, defaults to 80
	 */
	public HttpServer() {
		this(-1);
	}

	/**
	 * Use this if you want to manually override portnumber from butler.properties. Useful when setting up multiple servers.
	 * @param portnumber portnumber to listen on. Overrides portnumber from butler.properties
	 */
	public HttpServer(int portnumber) {

		// init the threadpool
		int tpool_capacity = -1;
		try {
			tpool_capacity = Integer.parseInt(PropertiesManager.get("max_threads"));
		} catch (Exception e) {
			logger.debug("ensure that max_threads is a positive integer value. Setting max_threads to 100");
			tpool_capacity = 100;
		}
		ExecutorService executorService = Executors.newFixedThreadPool(tpool_capacity);
		logger.debug("Created a fixed thread pool of size " + tpool_capacity);

		try {

			// configure port number
			if (1 > portnumber) {
				portnumber = 80;
				try {
					portnumber = Integer.parseInt(PropertiesManager.get("portnumber"));
				} catch (NumberFormatException e) {
					logger.debug("Incorrectly formatted or missing portnumber ", e);
				}
			}
			logger.info("Listening on port " + portnumber);

			// start listening
			ServerSocket serverSocket = new ServerSocket(portnumber);
			Socket socket = null;
			while (!killswitch) { // TODO configure killswitch functionality
				socket = serverSocket.accept();
				executorService.submit(new ClientHandler(socket));
				// new Thread(new ClientHandler(socket)).start();
			}
			serverSocket.close();

		} catch (IOException e) {
			logger.log(Level.FATAL, "Terminated unexpectedly...", e);
		}
	}

}
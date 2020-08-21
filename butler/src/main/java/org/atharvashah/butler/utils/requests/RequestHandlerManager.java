package org.atharvashah.butler.utils.requests;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestHandlerManager {
	
	private static volatile RequestHandlerManager manager;
	private static Object lockObject = new Object();
	private static final Logger logger = LogManager.getLogger();

	private HashMap<String, RequestHandler> getHandlers;
	private HashMap<String, RequestHandler> postHandlers;

	private RequestHandlerManager() {
		logger.traceEntry("Initializing RequestHandlerManager...");
		getHandlers = new HashMap<String, RequestHandler>();
		postHandlers = new HashMap<String, RequestHandler>();
		logger.traceExit("RequestHandlerManager initialized successfully...");
	}

	public static RequestHandlerManager getInstance() {
		if (null == manager) {
			synchronized(lockObject) {
				if (null == manager) {
					manager = new RequestHandlerManager();
				}
			}
		}
		return manager;
	}

	/**
	 * Already assumes that get() and post() have been called. For internal use to lookup and call RequestHandlers
	 * @param requestType GET or POST currently supported
	 * @param path resource requested
	 * @param request
	 * @return -1 if unimplemented/bad request, 0 if wildcard (unimplemented GET), 1 if implemented
	 */
	public int processRequest(String requestType, String path, Request req, Response res) {
		logger.traceEntry();
		int retval = -1;
		if (requestType.equalsIgnoreCase("GET")) {
			RequestHandler h = getHandlers.get(path);
			if (null != h) {
				h.handleRequest(req, res);
				retval = 1;
			} else {
				// USE wildcard to handle request
				h = getHandlers.get("*");
				h.handleRequest(req, res);
				retval = 0;
			}
		} else if (requestType.equalsIgnoreCase("POST")) {
			RequestHandler h = postHandlers.get(path);
			if (null != h) {
				h.handleRequest(req, res);
				retval = 1;
			} else {
				// return the status code for an unimplemented request
				retval = -1;
			}
		}

		// unimplemented request type or no valid request type (eg telnet)
		logger.traceExit("Returning with status code " + retval);
		return retval;
	}

	/**
	 * Public facing API to register a GET RequestHandler 
	 * @param path the requested resource eg /test.html
	 * @param handler RequestHandler to handle the request
	 */
	public void get(String path, RequestHandler handler) {
		getHandlers.put(path, handler);
		logger.info("Registered RequestHandler for GET " + path + " " + handler.toString());
	}

	/**
	 * Public facing API to register a POST RequestHandler
	 * @param path the requested resource eg /form.html
	 * @param handler RequestHandler to handle the request
	 */
	public void post(String path, RequestHandler handler) {
		postHandlers.put(path, handler);
		logger.info("Registered RequestHandler for POST " + path + " " + handler.toString());
	}

}
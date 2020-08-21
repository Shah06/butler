package org.atharvashah.butler.utils.requests;

import java.util.HashMap;

/**
 * High level API for dealing with HTTP Requests
 */
public class Request {

	public HashMap<String, String> headers;	// All http headers as key, val
	public HashMap<String, String> params;		// URL params as key,val
	public String requestType;					// GET, POST, etc
	public String path;						// eg /test.html
	public String requestBody;					// body of the http request
	public String length;						// content-length for POST
	public String request;

	public Request(String request) {
		this.request = request;
		// ugly method, sloppily implemented, work on the byte[] request version asap
		String[] parts = request.split("\\r?\\n");
		String[] firstLineParts = parts[0].split(" ");
		requestType = firstLineParts[0];
		path = firstLineParts[1].split("\\?")[0];
		try {
			String[] paramsArr = firstLineParts[1].split("\\?")[1].split("&");
			for (String kv : paramsArr) {
				String[] arr = kv.split("=");
				params.put(arr[0], arr[1]);
			}

			// load headers
			for (int i=1; !(parts[i].equals("\n")); i++) {
				String[] kv = parts[i].split("=");
				headers.put(kv[0], kv[1]);
			}
		} catch (Exception e) {}

		// postreq body?? TODO fix this
		if (requestType.equalsIgnoreCase("POST")) this.requestBody = parts[parts.length-1];
	}

	public Request(byte[] request) {
		// TODO implement this
	}
	
}
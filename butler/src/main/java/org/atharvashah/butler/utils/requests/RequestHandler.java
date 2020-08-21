package org.atharvashah.butler.utils.requests;

public interface RequestHandler {

	// abstract void handleRequest(byte[] req, byte[] res);
	abstract void handleRequest(Request req, Response res);

}

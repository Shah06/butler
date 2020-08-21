package org.atharvashah.butler.utils.requests;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * High level API for constructing a response
 */
public class Response {

	private static final int RESPONSE_BUFLEN_BYTES = 8192;
	private static final Logger logger = LogManager.getLogger(Response.class.getName());

	private BufferedOutputStream buf_os;

	public Response(OutputStream os) {
		this.buf_os = new BufferedOutputStream(os, Response.RESPONSE_BUFLEN_BYTES);
	}

	private void writeAndFlush(byte[] data) {
		try {
			buf_os.write(data);
			buf_os.flush();
		} catch (IOException e) {
			logger.debug("Error writing to outputStream: ", e);
		}
	}

	public void write(byte[] data) {
		this.writeAndFlush(data);
	}

	// TODO add charset options
	public void write(String data) {
		this.writeAndFlush(data.getBytes());
	}

	
}
package org.atharvashah.butler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atharvashah.butler.utils.properties.PropertiesManager;
import org.atharvashah.butler.utils.requests.Request;
import org.atharvashah.butler.utils.requests.RequestHandlerManager;
import org.atharvashah.butler.utils.requests.Response;

public class ClientHandler implements Runnable {

	private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());
	
	private static int INPUT_BUFFLEN_BYTES;
	private static int HEADERLEN_BYTES;
	static {
		try {
			logger.trace("Loading INPUT_BUFFLEN_BYTES");
			INPUT_BUFFLEN_BYTES = Integer.parseInt(PropertiesManager.get("input_bufflen_bytes"));
		} catch (NumberFormatException e) {
			logger.trace("Invalid or missing value for property 'input_bufflen_bytes'. Must be an integer value", e);
			INPUT_BUFFLEN_BYTES = 1024;
		} finally {
			logger.trace("Set INPUT_BUFFLEN_BYTES to " + INPUT_BUFFLEN_BYTES);
		}
		
		try {
			logger.trace("Loading HEADERLEN_BYTES");
			HEADERLEN_BYTES = Integer.parseInt(PropertiesManager.get("headerlen_bytes"));
		} catch (NumberFormatException e) {
			logger.trace("Invalid or missing value for property 'input_bufflen_bytes'. Must be an integer value", e);
			HEADERLEN_BYTES = 4096;
		} finally {
			logger.trace("Set HEADERLEN_BYTES to " + HEADERLEN_BYTES);
		}
	}

	private Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		logger.traceEntry();
		InputStream is = null;
		BufferedReader bufferedReader = null;
		OutputStream os = null;
		try {

			logger.trace("Opening input and output streams...");
			is = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			os = socket.getOutputStream();

			// logger.trace("Writing input stream to byte[] buffer");
			// byte[] request = new byte[ClientHandler.HEADERLEN_BYTES];
			// {
			// 	int off = 0;
			// 	int read = 0;
			// 	while ( (-1 != read) ) {
			// 		off += read;
			// 		if (	
			// 			(off > 3) &&
			// 			(0x0D == request[off-4]) &&
			// 			(0x0A == request[off-3]) &&
			// 			(0x0D == request[off-2]) &&
			// 			(0x0A == request[off-1]) &&
			// 			(0x00 == request[off])
			// 		) break;
			// 		read = is.read(request, off, ClientHandler.HEADERLEN_BYTES-off);
			// 	}
			// 	logger.debug("Request:\n" + new String(request));
			// }

			// Simpler way to do this
			int processed = 0;
			{
				String line = bufferedReader.readLine();
				String[] request = line.split(" ");

				// eventually, write implementations for calling processRequest with byte[] request and response
				// RequestHandlerManager.getInstance().processRequest(request[0], request[1], req, res);

				// if POST
				if (request[0].equalsIgnoreCase("POST")) {
					String temp = line;
					int contentLength = 0;
					while (!temp.equals("")) {
						temp = bufferedReader.readLine();
						line += temp;
						if (temp.toLowerCase().startsWith("Content-Length".toLowerCase())) {
							try {
								contentLength = Integer.parseInt(temp.split(":")[1].trim());
							} catch (Exception e) {
								logger.debug("content-length must be a number", e);
							}
						}
					}

					// deal with post request body
					if (contentLength < 8196) {
						// read exactly contentLength more bytes
						logger.trace("Allocating " + contentLength + " chars for POST content body");
						char[] cbuf = new char[contentLength];
						bufferedReader.read(cbuf);
						line += ("\n" + new String(cbuf));
					}
					// deal with the body of the post request
				}

				logger.info("Request:\n" + line);

				try {
					Request req = new Request(line);
					Response res = new Response(os);
					processed = RequestHandlerManager.getInstance().processRequest(request[0], request[1], req, res);
				} catch (ArrayIndexOutOfBoundsException e) {
					logger.error(e.getStackTrace());
				}

			}

			// if unimplemented or bad request
			if (-1 == processed) {
				os.write((new String("401 Unauthorized\n\n")).getBytes());
				os.flush();
				logger.info("401 Unauthorized");
			}

			logger.trace("Closing input and output streams...");
			is.close();
			os.flush();
			os.close();

		} catch (Exception e) {
			logger.error("Something went wrong, attempting to close streams...", e);
			try {
				is.close();
			} catch (Exception t) {
				logger.error("Unable to close inputstream");
			}

			try {
				bufferedReader.close();
			} catch (Exception t) {
				logger.error("Unable to close bufferedreader");
			}

			try {
				os.close();
			} catch (Exception t) {
				logger.error("Unable to close outputstream");
			}
		}
		logger.traceExit();
	}
	
}
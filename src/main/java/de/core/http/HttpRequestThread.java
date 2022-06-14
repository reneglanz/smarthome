package de.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import de.core.http.handler.HttpRequestHandler;
import de.core.log.Logger;
import de.core.rt.Releasable;

public class HttpRequestThread implements Runnable {
	Socket socket;

	HttpServer httpServer;

	Logger log = Logger.createLogger("HttpRequestThread");

	String connectorId;

	public HttpRequestThread(Socket socket, HttpServer httpServer, String connectorId) {
		this.socket = socket;
		this.httpServer = httpServer;
		this.connectorId = connectorId;
	}

	public void run() {
		boolean keepAlive = false;
		try {
			long start = System.currentTimeMillis();
			if (this.socket instanceof SSLSocket) {
				((SSLSocket) this.socket).startHandshake();
				this.log.debug("Handshake took " + (System.currentTimeMillis() - start) + "msec");
			}
			this.log.debug("New Request from: " + this.socket.getRemoteSocketAddress().toString());
			HttpRequest request = HttpRequestParser.parse(this.socket.getInputStream());
			request.setConnectorId(this.connectorId);
			request.thread = this;
			HttpRequestHandler requestHandler = this.httpServer.getHttpRequestHandler(request);
			if (requestHandler != null) {
				HttpResponse resp = requestHandler.handleRequest(request);
				if (resp != null) {
					resp.prepareForSend();
					writeResponse(resp);
					keepAlive = requestHandler.keepAlive();
					if(resp instanceof Releasable) {
						((Releasable) resp).release();
					}
				}
			}
			this.log.debug("Request handled in " + (System.currentTimeMillis() - start) + "msec");
		} catch (Exception e) {
			this.log.error("Failed to handle reqest from : " + this.socket.getRemoteSocketAddress().toString(), e);
		} finally {
			try {
				if (!keepAlive)
					this.socket.close();
			} catch (IOException iOException) {
			}
		}
	}

	public void writeResponse(HttpResponse resp) throws IOException {
		OutputStream os = this.socket.getOutputStream();
		os.write(("HTTP/1.1 " + resp.getStatusCode() + " " + getReasonPhrase(resp.getStatusCode()) + "\r\n").getBytes());
		StringBuilder sb = new StringBuilder();
		for (HttpHeader header : resp.header) {
			sb.append(header.getName()).append(": ");
			if (header.value != null)
				sb.append(header.value);
			sb.append("\r\n");
		}
		sb.append("\r\n");
		os.write(sb.toString().getBytes("UTF-8"));
		
		HttpHeader hencoding=resp.getHeader("Transfer-Encoding");
		if(hencoding!=null&&"chunked".equals(hencoding.value)) {
			os=new ChunkedOutputStream(os, 16384);
		}
		try(InputStream is = resp.getContent();){
			if (is instanceof java.io.ByteArrayInputStream) {
				byte[] ba = new byte[is.available()];
				is.read(ba);
				os.write(ba);
			} else {
				int read = 0;
				byte[] ba = new byte[16384];
				while ((read = is.read(ba)) != -1)
					os.write(ba, 0, read);
			}
			os.flush();
		}
	}

	private String getReasonPhrase(int statuscode) {
		switch (statuscode) {
		case 101:
			return "Switching Protocols";
		case 200:
		case 206:
		case 304:
			return "OK";
		case 404:
			return "BAD REQUEST";
		}
		return "NOK";
	}

	public Socket getSocket() {
		return this.socket;
	}

	public HttpServer getHttpServer() {
		return this.httpServer;
	}
}

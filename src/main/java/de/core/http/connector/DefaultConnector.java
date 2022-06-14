package de.core.http.connector;

import de.core.CoreException;
import de.core.Env;
import de.core.http.HttpRequestThread;
import de.core.http.HttpServer;
import de.core.log.Logger;
import de.core.serialize.annotation.Element;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DefaultConnector implements Connector {
	public boolean stop = false;

	private HttpServer httpServer;

	private Logger log = Logger.createLogger("Connector");

	@Element(mandatory = true, defaultValue = "7000")
	protected int port = 7000;

	@Element(defaultValue = "20000")
	protected int soTimeout = 20000;

	@Element(defaultValue = "20000")
	protected int readTimeout = 20000;

	@Element
	protected String name = "undefined";

	public void run() {
		if (this.httpServer != null) {
			try {
				Env.put("http.connector." + name, InetAddress.getLocalHost().getHostName() + ":" + port);
			} catch (Throwable t) {
			}
			boolean createSocket = true;
			ServerSocket serverSocket = null;
			while (!this.stop) {
				try {
					if (createSocket) {
						serverSocket = createSocket();
						createSocket = false;
					}
					if (serverSocket != null) {
						Socket client = serverSocket.accept();
						client.setSoTimeout(this.readTimeout);
						this.httpServer.runHttpRequest(new HttpRequestThread(client, this.httpServer, this.name));
					} else {
						this.log.error("Failed to create ServerSocket");
						this.stop = true;
					}
				} catch (Throwable t) {
					this.log.error("Failed to handle connection", t);
					createSocket = true;
				} finally {
					if (serverSocket != null && createSocket)
						try {
							serverSocket.close();
						} catch (IOException iOException) {
						}
				}
			}
			this.log.error("Out of server loop - something unexpected happend");
		}
	}

	public void stop() {
		this.stop = true;
	}

	public ServerSocket createSocket() throws CoreException {
		try {
			return new ServerSocket(this.port);
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
			return null;
		}
	}

	public void init(HttpServer server) {
		this.httpServer = server;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "Connector "+name+"[" + this.port + "]";
	}

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		long duration = 10 * 60 * 1000;
		while (true) {
			long x = start + duration - System.currentTimeMillis();
			System.out.println(x);
			Thread.sleep(1000);
		}
	}

	@Override
	public int getPort() {
		return port;
	}

}

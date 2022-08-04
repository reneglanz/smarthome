package de.core.http;

import de.core.CoreException;
import de.core.http.connector.Connector;
import de.core.http.connector.SSLConnector;
import de.core.http.handler.HttpRequestHandler;
import de.core.log.Logger;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpServer implements Serializable {
	@Element(mandatory = true)
	protected static List<HttpRequestHandler> requestHandler = Collections.synchronizedList(new ArrayList<>());
	@Element
	List<Connector> connectors = new ArrayList<>();
	@Element(defaultValue = "4")
	protected int corePoolSize = 10;
	@Element(defaultValue = "8")
	protected int maxPoolSize = 20;
	@Element(defaultValue = "false")
	protected boolean logErrors = false;

	protected boolean run = true;
	protected ThreadPoolExecutor threadPool;
	protected Logger log = Logger.createLogger("HttpServer");

	public HttpServer() {
		init();
	}

	private void init() {
		this.threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, 5L, TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(100));
	}

	public void start() {
		if (this.connectors != null)
			this.connectors.forEach(connector -> {
				this.log.info("Start " + connector);
				connector.init(this);
				Thread t = new Thread((Runnable) connector);
				t.setName(connector.getName());
				t.start();
			});
	}

	protected HttpRequestHandler getHttpRequestHandler(HttpRequest request) {
		for (HttpRequestHandler handler : requestHandler) {
			if (handler.canHandleRequest(request))
				return handler;
		}
		return null;
	}

	public static void registerHttpRequestHandler(HttpRequestHandler handler) {
		requestHandler.add(handler);
	}

	public static void deregisterHttpRequestHandler(HttpRequestHandler handler) {
		requestHandler.remove(handler);
	}

	public void runHttpRequest(HttpRequestThread request) {
		this.threadPool.execute(request);
	}

	public void addConnector(Connector connector) {
		this.connectors.add(connector);
	}

	public String getConnectorUrl(String connectorName) throws CoreException {
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			for (Connector c : connectors) {
				if (c.getName().equals(connectorName)) {
					return (c instanceof SSLConnector ? "https" : "http") + ("://") + hostname + ":" + c.getPort();
				}
			}
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}

	public boolean logErrors() {
		return logErrors;
	}

}

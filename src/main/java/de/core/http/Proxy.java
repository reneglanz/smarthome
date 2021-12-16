package de.core.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.rt.Releasable;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Proxy extends Thread implements Serializable, Releasable {

	private class ProxyConnection implements Runnable {

		protected Socket socket;
		protected Socket remoteSocket;
		
		public ProxyConnection(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				HttpRequest req=HttpRequestParser.parse(socket.getInputStream());
				if("CONNECT".equals(req.getMethod())) {
					String domain=req.getRequestPath().substring(0,req.getRequestPath().lastIndexOf(":"));
					String port=req.getRequestPath().substring(req.getRequestPath().lastIndexOf(":")+1, req.getRequestPath().length());
					
					
				}
				
				
			} catch (Throwable t) {
				
			}
		}
		
	}
	
	@Element protected int port=3568;
	@Element(defaultValue = "4") protected int corePoolSize = 10;
	@Element(defaultValue = "8") protected int maxPoolSize = 20;
	protected ThreadPoolExecutor threadPool;

	protected ServerSocket socket;
	protected boolean stop=false;
	protected Logger logger=Logger.createLogger("Proxy");
	
	protected Proxy() {
		init();
	}
	
	private void init() {
		this.threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, 5L, TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(100));
	}
	
	public void run() {
		try {
			logger.info("Start Proxy on Port "+port);
			socket=new ServerSocket(port);
			while(!stop) {
				Socket clientSocket=socket.accept();
				threadPool.execute(new ProxyConnection(clientSocket));
			}
		} catch(Throwable t) {
			logger.error(t);
		}
		
	}

	@Override
	public void release() throws CoreException {
		// TODO Auto-generated method stub
		
	}
}

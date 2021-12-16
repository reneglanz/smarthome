package de.core.server;

import de.core.CoreException;
import de.core.Env;
import de.core.db.DBConnectionManager;
import de.core.ftp.FtpServer;
import de.core.http.HttpServer;
import de.core.http.Proxy;
import de.core.log.FileAppender;
import de.core.log.Logger;
import de.core.mqtt.MqttClient;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.server.admin.AdminServiceImpl;
import de.core.service.DefaultLoadConsumer;
import de.core.service.Loader;
import de.core.service.Service;
import de.core.service.Services;
import de.core.task.Scheduler;
import de.core.utils.DirectoryStreamFilter;
import java.io.File;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class Server implements Launchable, Serializable, Releasable, Service {
	private Scheduler scheduler;

	@Element
	HttpServer http;
	@Element
	Proxy proxy;
	@Element
	FtpServer ftp;
	@Element
	protected String base;
	@Element(defaultValue = "0")
	protected int rootLogLevel;

	@Element
	DBConnectionManager dbConnectionManager;

	public void launch() throws CoreException {
		Logger.appender.add(new FileAppender(Paths.get(this.base, new String[] { "data", "logs" })));
		Logger.setRootLogLevel(this.rootLogLevel);
		if (this.dbConnectionManager != null)
			Env.put(this.dbConnectionManager);
		try {
			this.scheduler = new Scheduler();
			Env.put(this.scheduler);
			Services.bind((Service) new AdminServiceImpl(this));
			DefaultLoadConsumer loadConsumer = new DefaultLoadConsumer();
			new Loader(Paths.get(this.base + File.separator + "config", new String[0]),
					new DirectoryStreamFilter("*resource.sjos")).load(loadConsumer);
			new Loader(Paths.get(this.base + File.separator + "config", new String[0]),
					new DirectoryStreamFilter("*service.sjos")).load(loadConsumer);
			new Loader(Paths.get(this.base + File.separator + "config", new String[0]),
					new DirectoryStreamFilter("*task.sjos")).load(loadConsumer);
			loadConsumer.launch();
			this.scheduler.start();
		} catch (Throwable t) {
			throw CoreException.throwCoreException(t);
		}
		this.http.start();
		if (this.ftp != null)
			this.ftp.launch();

		if (proxy != null) {
			proxy.start();
		}
	}

	public void release() throws CoreException {
		MqttClient mqtt = (MqttClient) Env.get(MqttClient.class);
		if (mqtt != null)
			mqtt.release();
	}

	public void shutdown() throws CoreException {
		(new Thread() {
			public void run() {
				try {
					sleep(5000L);
					Server.this.release();
					System.exit(0);
				} catch (Throwable throwable) {
				}
			}
		}).start();
	}
}

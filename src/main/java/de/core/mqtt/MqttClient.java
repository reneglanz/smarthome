package de.core.mqtt;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.rt.Resource;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Injectable
public class MqttClient implements Serializable, Launchable, Resource, Releasable {
	private Logger logger = Logger.createLogger("MqttClient");

	@Element
	String host;

	@Element
	int port;

	@Element
	String clientId;

	@Element
	ArrayList<MqttSubscriber> subscriber;

	private class ConnectThread extends Thread {
		MqttConnectOptions options;

		private ConnectThread(MqttConnectOptions options) {
			this.options = options;
		}

		public void run() {
			while (!MqttClient.this.client.isConnected()) {
				try {
					MqttClient.this.client.connect(this.options);
					sleep(10000L);
				} catch (Throwable t) {
					MqttClient.this.logger.error("Failed to connect to " + MqttClient.this.client.getServerURI());
					try {
						sleep(10000L);
					} catch (InterruptedException e) {}
				}
			}
			MqttClient.this.logger.info("Connected to " + MqttClient.this.client.getServerURI());
			if (MqttClient.this.subscriber != null)
				MqttClient.this.subscriber.forEach(sub -> {
					try {
						MqttClient.this.client.subscribe(sub.topic, sub.qos, sub);
						MqttClient.this.logger.info("Subscribed to " + sub.topic);
					} catch (MqttException e) {
						e.printStackTrace();
					}
				});
		}
	}

	@Element(defaultValue = "false")
	boolean cleanSession = false;

	protected SSLSocketFactory factory;

	IMqttClient client;

	public MqttClient(String host, int port, String clientId) {
		this.host = host;
		this.port = port;
		this.clientId = clientId;
	}

	public void publish(String topic, byte[] data) throws CoreException {
		MqttMessage message = new MqttMessage(data);
		try {
			this.client.publish(topic, message);
		} catch (Throwable t) {
			throw CoreException.throwCoreException(t);
		}
	}

	public void subscribe(MqttSubscriber subscriber) throws CoreException {
		try {
			if (this.client != null) {
				this.client.subscribe(subscriber.topic, subscriber.qos, subscriber);
				this.logger.info("Subscribed to " + subscriber.topic);
			} else {
				if (this.subscriber == null)
					this.subscriber = new ArrayList<>();
				this.subscriber.add(subscriber);
			}
		} catch (MqttException e) {
			throw CoreException.throwCoreException(e);
		}
	}

	public void unsubscribe(MqttSubscriber subscriber) throws CoreException {
		try {
			this.client.unsubscribe(subscriber.topic);
		} catch (MqttException e) {
			throw CoreException.throwCoreException(e);
		}
	}

	public void finish() {
		if (this.clientId == null)
			try {
				this.clientId = Inet4Address.getLocalHost().getHostName() + "-" + System.currentTimeMillis();
			} catch (UnknownHostException unknownHostException) {
			}
	}

	public void setSocketFactory(SSLSocketFactory factory) {
		this.factory = factory;
	}

	public void launch() throws CoreException {
		try {
			String url = ((this.factory != null) ? "ssl" : "tcp") + "://" + this.host + ":" + this.port;
			this.client = (IMqttClient) new org.eclipse.paho.client.mqttv3.MqttClient(url, this.clientId);
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(true);
			options.setConnectionTimeout(10);
			options.setAutomaticReconnect(true);
			options.setCleanSession(this.cleanSession);
			if (this.factory != null)
				options.setSocketFactory(this.factory);
			(new ConnectThread(options)).start();
		} catch (Throwable t) {
			throw CoreException.throwCoreException(t);
		}
	}

	public void release() throws CoreException {
		if (this.client != null)
			try {
				this.client.disconnect();
			} catch (MqttException e) {
				throw CoreException.throwCoreException(e);
			}
	}

	protected MqttClient() {
	}
}

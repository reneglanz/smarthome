package de.shd.device;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.Env;
import de.core.data.Data;
import de.core.mqtt.MqttClient;
import de.core.mqtt.MqttSubscriber;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.store.DBStore;
import de.shd.device.data.TextData;

public abstract class MqttDevice extends AbstractDevice implements Launchable, Releasable {
	@Element String subscribeTopic;
	@Element String publishTopic;
	@Injectable MqttClient mqttClient;
	@Element long lastupdate;
	@Element DBStore store;
	@Element String exportFormat;

	MqttSubscriber subscriber;
	TextData exportData;

	public void launch() throws CoreException {
		if (this.store != null) {
			this.store.create();
			Env.put(this.store.getName(), this.store);
		}
		initSubscriber();
	}

	protected void initSubscriber() {
		if (this.subscribeTopic != null) {
			this.subscriber = new MqttSubscriber(this.subscribeTopic) {
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					if (MqttDevice.this.exportData == null) {
						MqttDevice.this.exportData = new TextData(formatData(new String(message.getPayload())));
					} else {
						MqttDevice.this.exportData.setValue(formatData(new String(message.getPayload())));
					}
					if (MqttDevice.this.store != null)
						MqttDevice.this.store.add(MqttDevice.this.store.parseData(message.getPayload()));
					MqttDevice.this.export();
				}
			};
			try {
				if (this.mqttClient != null)
					this.mqttClient.subscribe(this.subscriber);
			} catch (CoreException coreException) {
			}
		}
	}

	protected String formatData(String data) {
		if (this.exportFormat != null && this.exportFormat.length() > 0) {
			if(exportFormat.contains("%d")) {
				return String.format(exportFormat, Integer.parseInt(data));
			} else if(exportFormat.contains("%f")||exportFormat.matches("(.*)(%\\.)(\\d)(f)(.*)")) {
				return String.format(exportFormat, Float.parseFloat(data));
			} else {
				return String.format(exportFormat, data);
			}
		} else {
			return data;
		}
	}

	public void release() throws CoreException {
		if (this.subscriber != null) {
			this.mqttClient.unsubscribe(this.subscriber);
			if (this.store != null)
				Env.remove(this.store.getName());
		}
	}

	public ExportData createExportData() {
		return new ExportData(getDeviceHandle(), name, (Data) this.exportData);
	}

	public void mqttPublish(String publishTopic, String msg) throws CoreException {
		mqttClient.publish(publishTopic, msg.getBytes());
	}
}

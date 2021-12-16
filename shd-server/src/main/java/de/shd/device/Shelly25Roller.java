package de.shd.device;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.data.Data;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.annotation.Element;
import de.shd.device.data.TextData;

public class Shelly25Roller extends MqttDevice implements Shutter {
	@Element
	protected String shellyId;

	public enum RollerStatus {
		OPEN, CLOSE, STOP;

		public String toString() {
			return name().toLowerCase();
		}
	}

	public static class RollerData implements Data {
		@Element(defaultValue = "0")
		public int pos = 0;
		@Element(defaultValue = "STOP")
		public Shelly25Roller.RollerStatus status = Shelly25Roller.RollerStatus.STOP;
	}

	protected RollerData exportData = new RollerData();

	public void finish() {
		this.publishTopic = "shellies/" + this.shellyId + "/roller/0/command";
	}

	protected void initSubscriber() {
		this.subscriber = new MqttSubscriber("shellies/" + this.shellyId + "/roller/#") {
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				if (topic.endsWith("/0")) {
					String payload = new String(message.getPayload());
					RollerStatus old = Shelly25Roller.this.exportData.status;
					if (payload.equals("stop")) {
						Shelly25Roller.this.exportData.status = Shelly25Roller.RollerStatus.STOP;
					} else if (payload.equals("open")) {
						Shelly25Roller.this.exportData.status = Shelly25Roller.RollerStatus.OPEN;
					} else if (payload.equals("close")) {
						Shelly25Roller.this.exportData.status = Shelly25Roller.RollerStatus.CLOSE;
					}
					if (old != Shelly25Roller.this.exportData.status) {
						Shelly25Roller.this.export();
					}
				} else if (topic.endsWith("/pos")) {
					Shelly25Roller.this.exportData.pos = Integer.parseInt(new String(message.getPayload()));
					if (Shelly25Roller.this.exportData.status == RollerStatus.STOP) {
						Shelly25Roller.this.export();
					}
				}
			}
		};
		try {
			if (this.mqttClient != null)
				this.mqttClient.subscribe(this.subscriber);
		} catch (CoreException coreException) {
		}
	}

	public ExportData createExportData() {
		TextData txtData = null;
		if (this.exportData != null) {
			if (this.exportData.pos == 0) {
				txtData = new TextData("geschlossen");
			} else if (this.exportData.pos == 100) {
				txtData = new TextData("offen");
			} else {
				txtData = new TextData(this.exportData.pos + "% offen");
			}
		} else {
			txtData = new TextData("bitte warten");
		}
		return new ExportData(getDeviceHandle(), name, txtData);
	}

	public void open() throws CoreException {
		mqttPublish(publishTopic, RollerStatus.OPEN.toString());
	}

	public void close() throws CoreException {
		mqttPublish(publishTopic, RollerStatus.CLOSE.toString());
	}

	public void setRange(int pos) throws CoreException {
		mqttPublish(publishTopic + "/pos", "" + pos);
		this.exportData.pos = pos;
	}

	public void stop() throws CoreException {
		mqttPublish(publishTopic, RollerStatus.STOP.toString());
	}

	@Override
	public int getRange() throws CoreException {
		return this.exportData.pos;
	}

}

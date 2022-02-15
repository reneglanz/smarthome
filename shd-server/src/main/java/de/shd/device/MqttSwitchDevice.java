package de.shd.device;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.data.Data;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.annotation.Element;
import de.shd.device.data.SwitchData;

public class MqttSwitchDevice extends MqttDevice implements Switch {
	@Element
	Map<String, String> stateMap;
	@Element(defaultValue = "UNKNOWN")
	Switch.State state = Switch.State.UNKNOWN;

	public Switch.State mapState(String external) {
		if (this.stateMap != null) {
			String tmp = this.stateMap.get(external);
			if (tmp != null)
				try {
					return Switch.State.valueOf(tmp);
				} catch (Throwable t) {
					return Switch.State.UNKNOWN;
				}
			return Switch.State.UNKNOWN;
		}
		return Switch.State.valueOf(external);
	}

	public String mapState(Switch.State state) {
		if (this.stateMap != null)
			for (Map.Entry<String, String> entry : this.stateMap.entrySet()) {
				if (((String) entry.getValue()).equals(state.toString()))
					return entry.getKey();
			}
		return state.toString();
	}

	protected void initSubscriber() {
		if (this.subscribeTopic != null) {
			this.subscriber = new MqttSubscriber(this.subscribeTopic) {
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String data = new String(message.getPayload());
					MqttSwitchDevice.this.state = MqttSwitchDevice.this.mapState(data);
					if (MqttSwitchDevice.this.store != null)
						MqttSwitchDevice.this.store.add(MqttSwitchDevice.this.store.parseData(message.getPayload()));
					MqttSwitchDevice.this.export();
				}
			};
			try {
				if (this.mqttClient != null)
					this.mqttClient.subscribe(this.subscriber);
			} catch (CoreException coreException) {
			}
		}
	}

	public Switch.State toggle() throws CoreException {
		try {
			if (this.state == Switch.State.ON) {
				this.state = Switch.State.OFF;
			} else {
				this.state = Switch.State.ON;
			}
			mqttPublish(publishTopic, mapState(this.state));
			export();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.state;
	}

	public ExportData createExportData() {
		return new ExportData(getDeviceId(), name, (Data) new SwitchData(this.state));
	}

	public Switch.State getState() throws CoreException {
		return this.state;
	}

	public Switch.State setState(Switch.State state) throws CoreException {
		if (this.state == state)
			return state;
		this.state = state;
		mqttPublish(publishTopic, mapState(state));
		return this.state;
	}
}

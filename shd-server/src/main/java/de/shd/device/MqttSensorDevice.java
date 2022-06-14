package de.shd.device;

public class MqttSensorDevice extends MqttDevice implements Sensor<String> {

	@Override
	public String readValue() {
		return exportData!=null?exportData.getValue():"";
	}}

package de.shd.automation.collector;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.service.Services;
import de.shd.automation.Data;
import de.shd.device.Sensor;

public class SensorCollector implements Serializable, Collector {
	@Element protected String provider;
	@Element protected String service;
	@Element protected String resultKey;

	protected Sensor<?> sensor=null;
	
	@Override
	public void collect(Data data) {
		try {
			if(sensor==null) {
				sensor=Services.get(provider, service, Sensor.class);
			}
			Object result=sensor.readValue();
			if(result!=null) {
				data.set(resultKey, result);
			}
		} catch (CoreException e) {
			data.getLogger().error("Error", e);
		}
	}
}

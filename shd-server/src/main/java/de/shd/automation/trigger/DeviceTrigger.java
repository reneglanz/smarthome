package de.shd.automation.trigger;

import de.core.serialize.annotation.Element;

public class DeviceTrigger extends AbstractTrigger {

	@Element protected String deviceId;

	public String getDeviceId() {
		return deviceId;
	}

}

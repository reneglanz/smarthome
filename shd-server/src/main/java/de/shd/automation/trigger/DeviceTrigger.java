package de.shd.automation.trigger;

import de.core.handle.NameHandle;
import de.core.serialize.annotation.Element;

public class DeviceTrigger extends AbstractTrigger {

	@Element protected NameHandle deviceId;

	public NameHandle getDeviceId() {
		return deviceId;
	}

}

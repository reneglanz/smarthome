package de.shd.device;

import de.core.CoreException;
import de.core.serialize.annotation.Element;

public class Shelly1 extends MqttSwitchDevice {

	@Element String shellyId;

	@Override
	public void launch() throws CoreException {
		this.publishTopic="shellies/"+shellyId+"/relay/0";
		this.subscribeTopic="shellies/"+shellyId+"/relay/0/command";
		super.launch();
	}
}

package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;
import de.shd.device.Switch;
import de.shd.device.Switch.State;

public class SwitchData implements Data {
	@Element protected Switch.State value;

	public SwitchData(State state) {
		this.value = state;
	}
}

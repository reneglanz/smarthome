package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;
import de.shd.device.State;

public class SwitchData implements Data {
	@Element protected State value;

	public SwitchData(State state) {
		this.value = state;
	}
}

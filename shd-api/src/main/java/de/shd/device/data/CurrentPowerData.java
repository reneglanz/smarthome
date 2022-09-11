package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class CurrentPowerData implements Data {
	@Element protected int value;
	
	protected CurrentPowerData() {}

	public CurrentPowerData(int value) {
		super();
		this.value = value;
	}
	
	
}


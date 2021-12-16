package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class BrightnessData implements Data {
	@Element int value;

	public BrightnessData(int value) {
		super();
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}

package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class TextData implements Data {
	@Element String value;

	public TextData(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
